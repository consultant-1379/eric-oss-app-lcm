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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEPLOY_APP_INSTANCE_TIMEOUT_ERROR;

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

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for the DeployAppInstanceMonitoringJob.
 */
@Slf4j
@Component
public class DeployAppInstanceMonitoringJob extends MonitoringJob {
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AcmService acmService;

    @Autowired
    public DeployAppInstanceMonitoringJob(
            @Qualifier("threadPoolTaskExecutorForDeployAppInstanceMonitoringJob")
            ThreadPoolTaskExecutor threadPoolTaskExecutor,
            @Value("${deployAppInstanceUseCase.timeout.in.milliseconds}")
            Long timeout
    ) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    /**
     * Setting the scheduler with the initial delay before the execution of the monitoring job with deployAppMonitoringJob.delayRate.in.milliseconds
     * and the delay for the fix rate task with deployAppMonitoringJob.schedulingRate.in.milliseconds from application.yaml
     */
    @Override
    @Scheduled(initialDelayString = "${deployAppInstanceMonitoringJob.delayRate.in.milliseconds}",
            fixedRateString = "${deployAppInstanceMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appInstancesRepository.findAllByStatus(AppInstanceStatus.DEPLOYING));
    }

    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final AppInstances appInstance = (AppInstances) databaseEntity;
        boolean isPollingCompleted = false;
        AutomationCompositionInstance acInstance = null;
        log.debug("Deploy App instance Monitoring Job Has Been Triggered for App Id : {}" , appInstance.getId());
        try {
            acInstance = this.acmService.getAutomationCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        } catch (final RestRequestFailedException ex) {
            Metrics.counter("app.lcm.deploy.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("Request failure when calling ACM-R to Get the AC instance details for appInstanceId {}. Reason: {}", appInstance.getId(), ex.getErrorDetails(), ex);
        }
        if (acInstance!=null && acInstance.getDeployState() == DeployState.DEPLOYED) {
            //Save the app with state DEPLOYED
            appInstance.setStatus(AppInstanceStatus.DEPLOYED);
            this.appInstancesRepository.save(appInstance);
            isPollingCompleted = true;
            log.info("App with App Id " + appInstance.getId() + " has been DEPLOYING" + " set to state: DEPLOYED ");
        } else if (acInstance!=null && (acInstance.getStateChangeResult().equals(StateChangeResult.TIMEOUT) || acInstance.getStateChangeResult().equals(StateChangeResult.FAILED))) {
            final StateChangeResult currentStateChangeResult = acInstance.getStateChangeResult();
            //GET NodeTemplateState
            final Map<UUID, AutomationCompositionElement> automationCompositionElements = acInstance.getElements();
            //Aggregated Message
            final StringBuilder message = new StringBuilder();
            //Populate the message
            automationCompositionElements.keySet().stream().forEach(elementKey ->{
                final AutomationCompositionElement element = automationCompositionElements.get(elementKey);
                if (element.getMessage() !=null) {
                    message.append(elementKey).append(": ").append(element.getMessage()).append("\n");
                }
            });
            //Save the app with state DEPLOY_ERROR and save an app error event with the error messages from ACM-r
            appInstance.setStatus(AppInstanceStatus.DEPLOY_ERROR);
            this.appInstancesRepository.save(appInstance);
            AppInstanceEvent appInstanceEvent;
            if (currentStateChangeResult.equals(StateChangeResult.TIMEOUT)) {
                appInstanceEvent = AppInstanceEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                    .detail(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                    .appInstance(appInstance)
                    .build();
                log.error("App with App Id " + appInstance.getId() + " is set to state: DEPLOY_ERROR, because it has been TIMEOUT by ACM-R");
            } else {
                appInstanceEvent = AppInstanceEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorTitle())
                    .detail(DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorMessage() + " Error message from ACM-R: " + message)
                    .appInstance(appInstance)
                    .build();
                log.error("App with App Id " + appInstance.getId() + " is set to state: DEPLOY_ERROR, because it has been FAILED by ACM-R");
            }
        this.appInstanceEventRepository.save(appInstanceEvent);
        isPollingCompleted = true;
        Metrics.counter("app.lcm.deploy.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
        } else if (this.hasPollAttemptTimedOut(appInstance)) {
            //Save the app with state DEPLOY_ERROR and save an app error event
            appInstance.setStatus(AppInstanceStatus.DEPLOY_ERROR);
            this.appInstancesRepository.save(appInstance);
            this.appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                    .detail(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                    .appInstance(appInstance)
                    .build()
            );
            isPollingCompleted = true;
            Metrics.counter("app.lcm.deploy.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("App with App Id " + appInstance.getId() + " has TIMEOUT" + " set to state: DEPLOY_ERROR ");
        }
        return isPollingCompleted;
    }
}
