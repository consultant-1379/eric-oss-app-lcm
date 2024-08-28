/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import static com.ericsson.oss.ae.acm.clients.acmr.dto.AcTypeState.COMMISSIONED;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEINITIALIZE_APP_DEPRIMING_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEINITIALIZE_APP_TIMEOUT_ERROR;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionDefinition;
import com.ericsson.oss.ae.acm.clients.acmr.dto.NodeTemplateState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Class for the DeinitializeAppMonotoringJob.
 */
@Slf4j
@Component
public class DeinitializeAppMonitoringJob extends MonitoringJob {
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AppEventRepository appEventRepository;
    @Autowired
    private AcmService acmService;
    /**
     * Deinitializing the monitoring job's thread pool with
     * deinitializeAppUseCase.timeout.in.milliseconds, as the default timeout set from application.yaml
     */
    @Autowired
    public DeinitializeAppMonitoringJob(
        @Qualifier("threadPoolTaskExecutorForDeInitializeAppMonitoringJob")
        final ThreadPoolTaskExecutor threadPoolTaskExecutor,
        @Value("${deinitializeAppUseCase.timeout.in.milliseconds}")
        final Long timeout) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    /**
     * Setting the scheduler with the initial delay before the execution the monitoring job with deinitializeAppMonitoringJob.delayRate.in.milliseconds
     * and the delay for the fix rate task with deinitializeAppMonitoringJob.schedulingRate.in.milliseconds from application.yaml
     */
    @Override
    @Scheduled(initialDelayString = "${deinitializeAppMonitoringJob.delayRate.in.milliseconds}",
        fixedRateString = "${deinitializeAppMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    /**
     * Adding the app for polling based on its AppStatus as DEINITIALIZING
     */
    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appRepository.findAllByStatus(AppStatus.DEINITIALIZING));
    }

    /**
     * The tasks for the monitoring job when certain conditions are met
     */
    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final App app = (App) databaseEntity;
        boolean isPollingCompleted = false;
        AutomationCompositionDefinition acDefinition = null;
        log.debug("Deinitalizing App Monitoring Job Has Been Triggered for App Id : {}" , app.getId());
        try {
            acDefinition = this.acmService.getAutomationCompositionType(app.getCompositionId());
        } catch (final RestRequestFailedException ex) {
            Metrics.counter("app.lcm.deinitialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("Request failure when calling ACM-R to get Automation Composition Type for compositionId {}. Reason: {}", app.getCompositionId(), ex.getErrorDetails(), ex);
        }
        if (acDefinition !=null && acDefinition.getState() == COMMISSIONED) {
            //Save the app with state DEINITIALIZED
            app.setStatus(AppStatus.DEINITIALIZED);
            this.appRepository.save(app);
            isPollingCompleted = true;
            log.info("App with App Id " + app.getId() + " has been DEINITIALIZED" + " set to state: DEINITIALIZED ");
        } else if (acDefinition !=null && (acDefinition.getStateChangeResult().equals(StateChangeResult.TIMEOUT) || acDefinition.getStateChangeResult().equals(StateChangeResult.FAILED))) {
            final StateChangeResult currentStateChangeResult = acDefinition.getStateChangeResult();
            //GET NodeTemplateState
            final Map<String, NodeTemplateState> nodeTemplateStates = acDefinition.getElementStateMap();
            //Aggregated Message
            final StringBuilder message = new StringBuilder();
            //Populate the message
            for (Map.Entry<String,NodeTemplateState> entry : nodeTemplateStates.entrySet()) {
                if (entry.getValue().getMessage() !=null) {
                    message.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");
                }
            }
            //Save the app with state DEINITIALIZE_ERROR and save an app error event with the error messages from ACM-r
            app.setStatus(AppStatus.DEINITIALIZE_ERROR);
            this.appRepository.save(app);
            AppEvent appEvent;
            if (currentStateChangeResult.equals(StateChangeResult.TIMEOUT)){
                appEvent = AppEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEINITIALIZE_APP_TIMEOUT_ERROR.getErrorTitle())
                    .detail(DEINITIALIZE_APP_TIMEOUT_ERROR.getErrorMessage())
                    .app(app)
                    .build();
                log.error("App with App Id " + app.getId() + " is set to state: DEINITIALIZE_ERROR, because it has been TIMEOUT by ACM-R");
            }
            else {
                appEvent = AppEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEINITIALIZE_APP_DEPRIMING_COMPOSITION_TYPE_ERROR.getErrorTitle())
                    .detail(DEINITIALIZE_APP_DEPRIMING_COMPOSITION_TYPE_ERROR.getErrorMessage() + " Error message from ACM-R: " + message)
                    .app(app)
                    .build();
                log.error("App with App Id " + app.getId() + " is set to state: DEINITIALIZE_ERROR, because it has been FAILED by ACM-R");
            }
            this.appEventRepository.save(appEvent);
            isPollingCompleted = true;
            Metrics.counter("app.lcm.deinitialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
        } else if (this.hasPollAttemptTimedOut(app)) {
            //Save the app with state DEINITIALIZE_ERROR and save an app error event
            app.setStatus(AppStatus.DEINITIALIZE_ERROR);
            this.appRepository.save(app);
            this.appEventRepository.save(
                AppEvent.builder()
                    .type(EventType.ERROR)
                    .title(DEINITIALIZE_APP_TIMEOUT_ERROR.getErrorTitle())
                    .detail(DEINITIALIZE_APP_TIMEOUT_ERROR.getErrorMessage())
                    .app(app)
                    .build()
            );
            isPollingCompleted = true;
            Metrics.counter("app.lcm.deinitialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("App with App Id " + app.getId() + " has TIMEOUT" + " set to state: DEINITIALIZE_ERROR ");
        }

        return isPollingCompleted;
    }
}