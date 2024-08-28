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

import static com.ericsson.oss.ae.acm.clients.acmr.dto.AcTypeState.PRIMED;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.INITIALIZE_APP_TIMEOUT_ERROR;

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

import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionDefinition;
import com.ericsson.oss.ae.acm.clients.acmr.dto.NodeTemplateState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Class for the InitializeAppMonitoringJob.
 */
@Slf4j
@Component
public class InitializeAppMonitoringJob extends MonitoringJob {
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AppEventRepository appEventRepository;
    @Autowired
    private AcmService acmService;

    /**
     * Initializing the monitoring job's thread pool with
     * initializeAppUseCase.timeout.in.milliseconds, as the default timeout set from application.yaml
     */
    @Autowired
    public InitializeAppMonitoringJob(
        @Qualifier("threadPoolTaskExecutorForInitializeAppMonitoringJob") final ThreadPoolTaskExecutor threadPoolTaskExecutor,
        @Value("${initializeAppUseCase.timeout.in.milliseconds}") final Long timeout
    ) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.timeout = timeout;
    }

    /**
     * Setting the scheduler with the initial delay before the execution the monitoring job with initializeAppMonitoringJob.delayRate.in.milliseconds
     * and the delay for the fix rate task with initializeAppMonitoringJob.schedulingRate.in.milliseconds from application.yaml
     */
    @Override
    @Scheduled(initialDelayString = "${initializeAppMonitoringJob.delayRate.in.milliseconds}",
        fixedRateString = "${initializeAppMonitoringJob.schedulingRate.in.milliseconds}")
    public void execute() {
        super.execute();
    }

    /**
     * Adding the app for polling based on its AppStatus as INITIALIZING
     */
    @Override
    protected List<Object> getDatabaseEntitiesForPolling() {
        return new ArrayList<>(this.appRepository.findAllByStatus(AppStatus.INITIALIZING));
    }

    /**
     * The tasks for the monitoring job when certain conditions are met.
     */
    @Override
    protected boolean pollAcmForState(final Object databaseEntity) {
        final App app = (App) databaseEntity;
        boolean isPollingCompleted = false;
        AutomationCompositionDefinition acDefinition = null;
        log.debug("Initalizing App Monitoring Job Has Been Triggered for App Id : {}" , app.getId());
        try {
            acDefinition = this.acmService.getAutomationCompositionType(app.getCompositionId());
        } catch (final RestRequestFailedException ex) {
            Metrics.counter("app.lcm.initialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.error("Request failure when calling ACM-R to get Automation Composition Type for compositionId {}. Reason: {}", app.getCompositionId(), ex.getErrorDetails(), ex);
        }
        if (acDefinition !=null && acDefinition.getState() == PRIMED) {
            // save the app with state INITIALIZED
            app.setStatus(AppStatus.INITIALIZED);
            this.appRepository.save(app);
            isPollingCompleted = true;
            log.info("App with App ID " + app.getId() + " has been INITIALIZED");
        } else if (acDefinition !=null && (acDefinition.getStateChangeResult().equals(StateChangeResult.TIMEOUT) || acDefinition.getStateChangeResult().equals(StateChangeResult.FAILED))) {
            final StateChangeResult currentStateChangeResult = acDefinition.getStateChangeResult();
            //GET NodeTemplateState
            final Map<String, NodeTemplateState> nodeTemplateStates = acDefinition.getElementStateMap();
            //Aggregated Message
            final StringBuilder message = new StringBuilder();
            //Populate the message
            for (Map.Entry<String, NodeTemplateState> entry : nodeTemplateStates.entrySet()) {
                if (entry.getValue().getMessage() !=null) {
                    message.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");
                }
            }
            // save the app with state INITIALIZE_ERROR and save an app error event with the error messages from ACM-r
            app.setStatus(AppStatus.INITIALIZE_ERROR);
            this.appRepository.save(app);
            this.appEventRepository.save(
                AppEvent.builder()
                    .type(EventType.ERROR)
                    .title(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorTitle())
                    .detail(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorMessage() + " Error message from ACM-R: " + message)
                    .app(app)
                    .build()
            );
            isPollingCompleted = true;
            Metrics.counter("app.lcm.initialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.info("App with App ID " + app.getId() +
                ((currentStateChangeResult.equals(StateChangeResult.TIMEOUT)) ? " has been TIMEOUT by ACM-R" : " has been FAILED by ACM-R"));
        } else if (this.hasPollAttemptTimedOut(app)) {
            // save the app with state INITIALIZE_ERROR and save an app error event
            app.setStatus(AppStatus.INITIALIZE_ERROR);
            this.appRepository.save(app);
            this.appEventRepository.save(
                AppEvent.builder()
                    .type(EventType.ERROR)
                    .title(INITIALIZE_APP_TIMEOUT_ERROR.getErrorTitle())
                    .detail(INITIALIZE_APP_TIMEOUT_ERROR.getErrorMessage())
                    .app(app)
                    .build()
            );
            isPollingCompleted = true;
            Metrics.counter("app.lcm.initialize.failures.count", "LastFailedTimestamp", Instant.now().toString()).increment();
            log.info("App with App ID " + app.getId() + " has TIMEOUT");
        }

        return isPollingCompleted;
    }
}
