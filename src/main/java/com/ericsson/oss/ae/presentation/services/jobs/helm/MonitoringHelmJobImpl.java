/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.presentation.services.jobs.helm;

import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.OperationDtoMapper;
import com.ericsson.oss.ae.clients.helmorchestrator.model.Operation;
import com.ericsson.oss.ae.constants.helmorchestrator.OperationState;
import com.ericsson.oss.ae.constants.helmorchestrator.OperationType;
import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.presentation.services.AppInstanceJpaSpecification;
import com.ericsson.oss.ae.presentation.services.jobs.MonitoringJob;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static com.ericsson.oss.ae.constants.AppLcmConstants.DOT_AND_SPACE;
import static com.ericsson.oss.ae.model.entity.HealthStatus.*;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_EXECUTOR_SERVICE_UNAVAILABLE;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonitoringHelmJobImpl implements MonitoringJob {

    private final ArtifactInstanceRepository artifactInstancerepository;
    private final AppInstanceRepository appInstancerepository;
    @Autowired
    private final AppInstanceJpaSpecification appInstanceJpaSpecification;
    @Autowired
    private HelmOrchestratorClient helmService;

    private final OperationDtoMapper operationDtoMapper;

    @Override
    @Scheduled(initialDelayString = "${monitoringJob.initialDelay.in.milliseconds}", fixedRateString = "${monitoringJob.fixedRate.in.milliseconds}")
    public void execute() {
        log.debug("Monitoring Helm job started");
        appInstancerepository.findAll(getLiveInstances()).forEach(appInstance -> {
            log.debug("Check all artifacts for instance id {} ", appInstance.getId());
            boolean isNotHealthy;
            for (final ArtifactInstance artifactInstance : appInstance.getArtifactInstances()) {
                if (Objects.equals(PENDING, artifactInstance.getHealthStatus())) {
                    final HealthStatus  healthStatus = getStatusFromHelmExecutor(appInstance, artifactInstance);
                    //check if artefact is INSTANTIATED
                    isNotHealthy = !healthStatus.equals(INSTANTIATED);
                }else{
                    updateInstanceHealthStatus(appInstance, artifactInstance.getHealthStatus(), artifactInstance.getId());
                    isNotHealthy = true;
                }
                //if artefact instance is not Instantiated then skip loop
                //as I presume that if any artefact is NOT instantiated then the Instance can NOT be in Instantiated health status
                if(isNotHealthy){
                    break;
                }
            }
            appInstance.setUpdatedTimestamp(ZonedDateTime.now(ZoneOffset.UTC));
            appInstancerepository.save(appInstance);
        });
    }

    private void updateInstanceHealthStatus(AppInstance appInstance, HealthStatus healthStatus, Long artifactId) {
        log.debug("Update instance health status without calling HFE as Artifact healthStatus is: {} for Artifact Id: {}", healthStatus, artifactId);
        if(healthStatus.equals(HealthStatus.TERMINATED)){
            appInstance.setHealthStatus(healthStatus);
        }else {
            appInstance.setHealthStatus(HealthStatus.FAILED);
        }
    }

    protected HealthStatus getStatusFromHelmExecutor(final AppInstance appInstance, final ArtifactInstance artifactInstance) {
        log.debug("Check helm status on HFE for AppId: {}, Artifact Id: {} and artifact operation Id: {}",
                  appInstance.getId(), artifactInstance.getId(), artifactInstance.getOperationId());
        HealthStatus healthStatus = PENDING;
        try{
            final ResponseEntity<OperationDto> operationResponse =
                helmService.getOperation(artifactInstance.getOperationId());
            if (operationResponse.getStatusCode().equals(HttpStatus.OK)) {
                final Operation operation = operationDtoMapper.map(operationResponse.getBody(), Operation.class);
                log.debug("HFE operation state: {}, operation type: {}", operation.getState(), operation.getType());
                healthStatus = getDeducedHealthStatusFromHelmOperation(
                    OperationState.valueOf(operation.getState()), OperationType.valueOf(operation.getType()), appInstance);
                final String operationLogsStatusMessage = getOperationLogs(artifactInstance);
                artifactInstance.setStatusMessage(operationLogsStatusMessage);
            }else {
                log.info("HFE Operation http status for artifact operation Id: {} is {}",
                         artifactInstance.getOperationId(), operationResponse.getStatusCode());
            }
        }catch (final HelmOrchestratorException helmOrchestratorException){
            StringBuilder errorMessage = new StringBuilder(appInstance.toString());
            log.error(helmOrchestratorException.getMessage() + ". " +errorMessage);
            if(helmOrchestratorException.getCause() instanceof final ResourceAccessException resourceAccessException) {
                errorMessage.append(DOT_AND_SPACE).append(resourceAccessException.getMessage());
                errorMessage.append(DOT_AND_SPACE).append(HELM_EXECUTOR_SERVICE_UNAVAILABLE.getErrorMessage());
                log.error(errorMessage.toString(), helmOrchestratorException);
            }else if(helmOrchestratorException.getCause() instanceof HttpClientErrorException){
                final HttpClientErrorException exception = (HttpClientErrorException) helmOrchestratorException.getCause();
                errorMessage.append(DOT_AND_SPACE).append(exception.getMessage());
                if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    //if HFE have not found the record, then set instance and artefact to FAILED
                    healthStatus = FAILED;
                    errorMessage.append(DOT_AND_SPACE).append("Instance set to FAILED");
                }
                log.error(errorMessage.toString(), helmOrchestratorException);
            }
        }
        appInstance.setHealthStatus(healthStatus);
        artifactInstance.setHealthStatus(healthStatus);
        artifactInstance.setUpdatedTimestamp(ZonedDateTime.now(ZoneOffset.UTC));
        log.info("App-Lcm health status for instance: {} and artifact operation Id: {} is {}",
                 appInstance.getId(), artifactInstance.getOperationId(), healthStatus);
        return healthStatus;
    }

    private String getOperationLogs(ArtifactInstance artifactInstance) {
        log.debug("Get Operation Logs for Artifact Instance Operation Id: {}", artifactInstance.getOperationId());
        try {
            final ResponseEntity<String> statusMessage = helmService.getOperationLogs(artifactInstance.getOperationId());
            if (statusMessage.getStatusCode().equals(HttpStatus.OK)) {
                String operationStatusMessage = statusMessage.getBody();
                String debugMessage = operationStatusMessage != null ? operationStatusMessage : "No operation status message";
                log.info("Retrieved logs: {}", debugMessage);
                return operationStatusMessage;
            }
        }catch (final HelmOrchestratorException helmOrchestratorException){
            log.error("Retrieved logs unsuccessful. {}", helmOrchestratorException.getCause(), helmOrchestratorException);
        }
        return "";
    }

    private HealthStatus getDeducedHealthStatusFromHelmOperation(final OperationState operationState, final OperationType operationType, final AppInstance appInstance) {
        log.debug("Deduce Health Status for OperationState: {} and OperationType: {}", operationState, operationType);
        switch (operationState) {
            case FAILED:
                log.debug("App instance with id [{}] operation FAILED", appInstance.getId());
                return HealthStatus.FAILED;
            case PROCESSING:
                return HealthStatus.PENDING;
            case COMPLETED:
                switch (operationType) {
                    case INSTANTIATE:
                    case UPDATE:
                    case REINSTANTIATE:
                        log.debug("App instance with id [{}] INSTANTIATED", appInstance.getId());
                        return HealthStatus.INSTANTIATED;
                    case ROLLBACK:
                        log.debug("App instance with id [{}] operation FAILED", appInstance.getId());
                        return HealthStatus.FAILED;
                    case TERMINATE:
                        log.debug("App instance with id [{}] TERMINATED", appInstance.getId());
                        return HealthStatus.TERMINATED;
                    default:
                        return HealthStatus.PENDING;
                }
            default:
                break;
        }
        return HealthStatus.PENDING;
    }

    private Specification<AppInstance> getLiveInstances() {
        log.debug("Get live instances:Instantiated or Pending from DB");
        AppInstanceFilter filter = new AppInstanceFilter();
        filter.setHealthStatus(List.of(PENDING));
        return appInstanceJpaSpecification.getAppInstanceRequest(filter);
    }
}
