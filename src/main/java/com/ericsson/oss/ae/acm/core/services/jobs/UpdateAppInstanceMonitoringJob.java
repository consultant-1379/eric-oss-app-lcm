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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UPDATE_APP_INSTANCE_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UPDATE_APP_INSTANCE_TIMEOUT_ERROR;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Class for the UpdateAppInstanceMonitoringJob.
 */
@Slf4j
@Component
public class UpdateAppInstanceMonitoringJob extends MonitoringJob {
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AcmService acmService;

    @Autowired
    public UpdateAppInstanceMonitoringJob(
            @Qualifier("threadPoolTaskExecutorForUpdateAppInstanceMonitoringJob")
            ThreadPoolTaskExecutor threadPoolTaskExecutor,
            @Value("${updateAppInstanceUseCase.timeout.in.milliseconds}")
            Long timeout) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    /**
     * Setting the scheduler with the initial delay before the execution of the monitoring job with updateAppMonitoringJob.delayRate.in.milliseconds
     * and the delay for the fix rate task with updateAppMonitoringJob.schedulingRate.in.milliseconds from application.yaml
     */
    @Override
    @Scheduled(initialDelayString = "${updateAppInstanceMonitoringJob.delayRate.in.milliseconds}",
            fixedRateString = "${updateAppInstanceMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appInstancesRepository.findAllByStatus(AppInstanceStatus.UPDATING));
    }

    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final AppInstances appInstance = (AppInstances) databaseEntity;
        final App app = appInstance.getApp();
        boolean isPollingCompleted = false;
        AutomationCompositionInstance automationCompositionInstance = null;
        try {
            automationCompositionInstance = this.acmService
                    .getAutomationCompositionInstance(app.getCompositionId(), appInstance.getCompositionInstanceId());
        } catch (final RestRequestFailedException ex) {
            Metrics.counter("app.lcm.update.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("Request failure when calling ACM-R to Get the AC instance details for appInstanceId {}. Reason: {}", appInstance.getId(), ex.getErrorDetails(), ex);
        }

        if (automationCompositionInstance != null && automationCompositionInstance.getDeployState() == DeployState.DEPLOYED) {
            processDeployed(appInstance);
            isPollingCompleted = true;
            log.info("App with App Instance Id " + appInstance.getId() + " has been UPDATING" + " set to state: UPDATED ");
        } else if (automationCompositionInstance != null && (automationCompositionInstance.getStateChangeResult().equals(StateChangeResult.TIMEOUT) || automationCompositionInstance.getStateChangeResult().equals(StateChangeResult.FAILED))) {
            final StateChangeResult currentStateChangeResult = automationCompositionInstance.getStateChangeResult();
            processAcInstanceUpdateTimeoutOrFailed(appInstance, automationCompositionInstance, currentStateChangeResult);
            isPollingCompleted = true;
            Metrics.counter("app.lcm.update.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.info("App Instance with App Instance Id " + appInstance.getId() +
                    ((currentStateChangeResult.equals(StateChangeResult.TIMEOUT)) ? " has been TIMEOUT by ACM-R" : " has been FAILED by ACM-R"));
        } else if (this.hasPollAttemptTimedOut(appInstance)) {
            processMonitoringJobTimeout(appInstance);
            isPollingCompleted = true;
            Metrics.counter("app.lcm.update.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.info("App Instance with App Instance Id " + appInstance.getId() + " has reached Monitoring Job TIMEOUT");
        }
        return isPollingCompleted;
    }

    private void processDeployed(final AppInstances appInstance) {
        appInstance.setStatus(AppInstanceStatus.DEPLOYED);
        appInstancesRepository.save(appInstance);
    }

    private void processAcInstanceUpdateTimeoutOrFailed(final AppInstances appInstance, final AutomationCompositionInstance automationCompositionInstance, final StateChangeResult currentStateChangeResult) {
        //GET elements
        final Map<UUID, AutomationCompositionElement> elements = automationCompositionInstance.getElements();
        //Aggregated Message
        final StringBuilder message = new StringBuilder();
        //Populate the message
        for (final Map.Entry<UUID, AutomationCompositionElement> entry : elements.entrySet()) {
            message.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");
        }
        // save the app instance with status UPDATE_ERROR and save an app instance error event with the error messages from ACM-R
        appInstance.setStatus(AppInstanceStatus.UPDATE_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? UPDATE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle() : UPDATE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorTitle()))
                        .detail((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? UPDATE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage() : UPDATE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorMessage()) + " Error message from ACM-R: " + message)
                        .appInstance(appInstance)
                        .build()
        );
    }

    private void processMonitoringJobTimeout(final AppInstances appInstance) {
        appInstance.setStatus(AppInstanceStatus.UPDATE_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title(UPDATE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                        .detail(UPDATE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                        .appInstance(appInstance)
                        .build()
        );
    }
}