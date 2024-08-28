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

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionDefinition;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.NodeTemplateState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UNDEPLOY_APP_INSTANCE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR;

/**
 * Class for the UndeployAppInstanceMonitoringJob.
 */
@Slf4j
@Component
public class UndeployAppInstanceMonitoringJob extends MonitoringJob {
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AcmService acmService;

    /**
     * Initializing the monitoring job's thread pool with
     * undeployAppInstanceUseCase.timeout.in.milliseconds, as the default timeout set from application.yaml
     */
    @Autowired
    public UndeployAppInstanceMonitoringJob(
            @Qualifier("threadPoolTaskExecutorForUndeployAppInstanceMonitoringJob") final ThreadPoolTaskExecutor threadPoolTaskExecutor,
            @Value("${undeployAppInstanceUseCase.timeout.in.milliseconds}") final Long timeout) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    /**
     * Setting the scheduler with the initial delay before the execution the monitoring job with undeployAppInstanceMonitoringJob.delayRate.in.milliseconds
     * and the delay for the fix rate task with undeployAppInstanceMonitoringJob.schedulingRate.in.milliseconds from application.yaml
     */
    @Override
    @Scheduled(initialDelayString = "${undeployAppInstanceMonitoringJob.delayRate.in.milliseconds}",
            fixedRateString = "${undeployAppInstanceMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    /**
     * Adding the app instances for polling based on its AppInstanceStatus as UNDEPLOYING
     */
    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appInstancesRepository.findAllByStatus(AppInstanceStatus.UNDEPLOYING));
    }

    /**
     * The tasks for the monitoring job when certain conditions are met.
     */
    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final AppInstances appInstance = (AppInstances) databaseEntity;
        final App app = appInstance.getApp();

        boolean isPollingCompleted = false;

        final AutomationCompositionInstance automationCompositionInstance = this.acmService
                .getAutomationCompositionInstance(app.getCompositionId(), appInstance.getCompositionInstanceId());
        final StateChangeResult currentStateChangeResult = automationCompositionInstance.getStateChangeResult();

        if (automationCompositionInstance.getDeployState() == DeployState.UNDEPLOYED) {
            processUndeployed(appInstance);
            isPollingCompleted = true;
            log.info("App Instance with App Instance Id " + appInstance.getId() + " has been UNDEPLOYED");
        } else if (automationCompositionInstance.getDeployState() == DeployState.UNDEPLOYING) {
            if (currentStateChangeResult.equals(StateChangeResult.TIMEOUT) || currentStateChangeResult.equals(StateChangeResult.FAILED)) {
                processAcInstanceUndeployTimeoutOrFailed(appInstance, automationCompositionInstance, currentStateChangeResult);
                isPollingCompleted = true;
                Metrics.counter("app.lcm.undeploy.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
                log.info("App Instance with App Instance Id " + appInstance.getId() +
                        ((currentStateChangeResult.equals(StateChangeResult.TIMEOUT)) ? " has been TIMEOUT by ACM-R" : " has been FAILED by ACM-R"));
            } else if (this.hasPollAttemptTimedOut(appInstance)) {
                processMonitoringJobTimeout(appInstance);
                isPollingCompleted = true;
                Metrics.counter("app.lcm.undeploy.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
                log.info("App Instance with App Instance Id " + appInstance.getId() + " has reached Monitoring Job TIMEOUT");
            }
        }
        return isPollingCompleted;
    }

    private void processUndeployed(final AppInstances appInstance) {
        appInstance.setStatus(AppInstanceStatus.UNDEPLOYED);
        appInstancesRepository.save(appInstance);
    }

    private void processAcInstanceUndeployTimeoutOrFailed(final AppInstances appInstance, final AutomationCompositionInstance automationCompositionInstance, final StateChangeResult currentStateChangeResult) {
        //GET elements
        final Map<UUID, AutomationCompositionElement> elements = automationCompositionInstance.getElements();
        //Aggregated Message
        final StringBuilder message = new StringBuilder();
        //Populate the message
        for (final Map.Entry<UUID, AutomationCompositionElement> entry : elements.entrySet()) {
            message.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");
        }
        // save the app instance with status UNDEPLOY_ERROR and save an app instance error event with the error messages from ACM-R
        appInstance.setStatus(AppInstanceStatus.UNDEPLOY_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle() : UNDEPLOY_APP_INSTANCE_ERROR.getErrorTitle()))
                        .detail((currentStateChangeResult.equals(StateChangeResult.TIMEOUT) ? UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage() : UNDEPLOY_APP_INSTANCE_ERROR.getErrorMessage()) + " Error message from ACM-R: " + message)
                        .appInstance(appInstance)
                        .build()
        );
    }

    private void processMonitoringJobTimeout(final AppInstances appInstance) {
        appInstance.setStatus(AppInstanceStatus.UNDEPLOY_ERROR);
        appInstancesRepository.save(appInstance);
        appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                        .detail(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                        .appInstance(appInstance)
                        .build()
        );
    }
}