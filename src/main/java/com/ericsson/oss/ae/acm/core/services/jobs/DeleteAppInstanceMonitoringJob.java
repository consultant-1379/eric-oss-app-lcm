/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.ae.acm.core.services.jobs;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DELETE_APP_INSTANCE_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DELETE_APP_INSTANCE_TIMEOUT_ERROR;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositions;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Class for the DeleteAppInstanceMonitoringJob which monitors the Delete App instance use case
 * Deleted is thrown if successful and if not gives a description of the error that occurred.
 */
@Slf4j
@Component
public class DeleteAppInstanceMonitoringJob extends MonitoringJob {
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AcmService acmService;
    @Autowired
    private KeycloakHandler keycloakHandler;

    @Autowired
    public DeleteAppInstanceMonitoringJob(
            @Qualifier("threadPoolTaskExecutorForDeleteAppInstanceMonitoringJob") ThreadPoolTaskExecutor threadPoolTaskExecutor,
            @Value("${deleteAppInstanceUseCase.timeout.in.milliseconds}") Long timeout
    ) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    @Override
    @Scheduled(initialDelayString = "${deleteAppInstanceMonitoringJob.delayRate.in.milliseconds}",
            fixedRateString = "${deleteAppInstanceMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appInstancesRepository.findAllByStatus(AppInstanceStatus.DELETING));
    }

    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final AppInstances appInstance = (AppInstances) databaseEntity;
        boolean isPollingCompleted = false;
        log.debug("Delete App Instance monitoring job has been triggered for App Instance Id : {}", appInstance.getId());
        final AutomationCompositions automationCompositions = getAllAutomationCompositionInstances(appInstance.getApp().getCompositionId());
        if (automationCompositions != null) {
            final List<AutomationCompositionInstance> automationCompositionList = automationCompositions.getAutomationCompositionList();
            final Optional<AutomationCompositionInstance> automationCompositionInstanceOptional = automationCompositionList.stream().filter(automationCompositionInstance ->
                    automationCompositionInstance.getInstanceId().equals(appInstance.getCompositionInstanceId())).findFirst();
            if (!automationCompositionInstanceOptional.isPresent()) {
                log.info("AC Instance not found in ACM-R. Proceeding to DELETE App instance record...");
                return removeClientCredentialsAndAppInstance(appInstance);
            } else if (automationCompositionInstanceOptional.get().getStateChangeResult().equals(StateChangeResult.TIMEOUT) || automationCompositionInstanceOptional.get().getStateChangeResult().equals(StateChangeResult.FAILED)) {
                final AutomationCompositionInstance automationCompositionInstance = automationCompositionInstanceOptional.get();
                final StateChangeResult currentStateChangeResult = automationCompositionInstance.getStateChangeResult();
                processAcInstanceDeleteTimeoutOrFailed(appInstance, automationCompositionInstance, currentStateChangeResult);
                isPollingCompleted = true;
                Metrics.counter("app.lcm.delete.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
                log.info("App Instance with App Instance ID: " + appInstance.getId() +
                        ((currentStateChangeResult.equals(StateChangeResult.TIMEOUT)) ? " has been TIMEOUT by ACM-R" : " has been FAILED by ACM-R"));
            } else if (hasPollAttemptTimedOut(appInstance)) {
                processMonitoringJobTimeout(appInstance);
                isPollingCompleted = true;
                Metrics.counter("app.lcm.delete.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
                log.info("App Instance with App Instance ID: {} has reached Monitoring Job TIMEOUT", appInstance.getId());
            }
        }
        return isPollingCompleted;
    }

    private AutomationCompositions getAllAutomationCompositionInstances(final UUID compositionId) {
        try {
            return acmService.getAllAutomationCompositionInstancesForCompositionId(compositionId);
        } catch (RestRequestFailedException ex) {
            log.error("Error when getting Automation Composition instances for compositionId {} in ACM-R", compositionId, ex);
        }
        return null;
    }

    private boolean removeClientCredentialsAndAppInstance(final AppInstances appInstance) {
        try {
            final List<ClientCredential> clientCredentials = appInstance.getClientCredentials();
            for (ClientCredential clientCredential : clientCredentials) {
                keycloakHandler.rollBackCreatedKeycloakCredentials(clientCredential);
            }
            appInstancesRepository.delete(appInstance);
            log.info("App Instance with ID: {} has been DELETED", appInstance.getId());
            return true;
        } catch (AppLcmException exception) {
            log.error("Keycloak exception while rolling back credentials", exception);
            return false;
        }
    }

    private void processAcInstanceDeleteTimeoutOrFailed(final AppInstances appInstance, final AutomationCompositionInstance automationCompositionInstance, final StateChangeResult currentStateChangeResult) {
        // Get Elements
        final Map<UUID, AutomationCompositionElement> elements = automationCompositionInstance.getElements();
        // Aggregated message
        final StringBuilder message = new StringBuilder();
        // Populate the message
        for (Map.Entry<UUID, AutomationCompositionElement> entry : elements.entrySet()) {
            if (entry.getValue().getMessage() != null) {
                message.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");
            }
        }
        // save the App instance with status DELETE_ERROR and save an App instance error event with error messages from ACM-R
        appInstance.setStatus(AppInstanceStatus.DELETE_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle() : DELETE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorTitle()))
                        .detail((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage() : DELETE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorMessage()) + " Error message from ACM-R: " + message)
                        .appInstance(appInstance)
                        .build()
        );
    }

    private void processMonitoringJobTimeout(final AppInstances appInstance) {
        appInstance.setStatus(AppInstanceStatus.DELETE_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                        .detail(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                        .appInstance(appInstance)
                        .build()
        );
    }
}
