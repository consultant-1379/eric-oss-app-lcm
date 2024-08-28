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

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_EXECUTOR_SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { AppLcmApplication.class, MonitoringHelmJobImpl.class })
class MonitoringHelmJobImplTest {
    @Autowired
    private MonitoringHelmJobImpl helmJob;
    @MockBean
    private AppInstanceRepository appInstancerepository;
    @MockBean
    private HelmOrchestratorClient helmOrchestratorClient;

    @Test
    void givenInstanceAndArtefactWithPendingStatus_WhenHelmJobBackProcessingOperationState_ThenInstanceAndArtefactHealthIsPending(){
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("PROCESSING", "PENDING", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
    }

    @Test
    void givenInstanceAndArtefactWithInstantiatedStatus_WhenHelmJobBackFailedOperationState_ThenInstanceAndArtefactHealthIsFailed() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("FAILED", "ROLLBACK", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
    }

    @Test
    void givenInstanceAndArtefactWithPendingStatus_WhenHelmJobBackCompletedOperationStateAndInstantiateOperationType_ThenInstanceAndArtefactHealthIsInstantiated(){
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("COMPLETED", "INSTANTIATE", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.INSTANTIATED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.INSTANTIATED);
    }

    @Test
    void givenInstanceAndArtefactWithInstantiatedStatus_WhenHelmJobBackCompletedOperationStateAndOperationTypeRollback_ThenInstanceAndArtefactHealthIsFailed() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("COMPLETED", "ROLLBACK", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
    }

    @Test
    void givenInstanceAndArtefactWithInstantiatedStatus_WhenHelmJobBackCompletedOperationStateAndOperationTypeTerminate_ThenInstanceAndArtefactHealthIsTerminated() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("COMPLETED", "TERMINATE", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.TERMINATED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.TERMINATED);
    }

    @Test
    void givenInstanceWithInstantiatedStatusAndArtefactWithPendingStatus_WhenHelmJobBackInvalidOperationState_ThenInstanceAndArtefactHealthIsPending() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.INSTANTIATED, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("INVALID", "PENDING", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenReturn(new ResponseEntity<>("Status Message", HttpStatus.OK));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
    }

    @Test
    void givenInstanceWithInstantiatedStatusAndArtefactWithPendingStatus_WhenOperationLogsException_ThenInstanceAndArtefactHealthIsPending() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.INSTANTIATED, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("INVALID", "PENDING", HttpStatus.OK));
        when(helmOrchestratorClient.getOperationLogs(anyString()))
            .thenThrow(new HelmOrchestratorException(HELM_EXECUTOR_SERVICE_UNAVAILABLE, "Test", "Test"));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
    }

    @Test
    void givenInstanceWithPendingStatusAndArtefactWithInstantiatedStatus_WhenHelmJobBackBadRequest_ThenInstanceAndArtefactHealthIsPending() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenReturn(createOperationDtoResponse("INVALID", "PENDING", HttpStatus.BAD_REQUEST));

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
    }

    @Test
    void givenInstanceAndArtefactWithInstantiatedStatus_WhenHelmJobServiceUnavailable_ThenInstanceAndArtefactHealthIsPending() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.PENDING, null);


        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);
        when(helmOrchestratorClient.getOperation(anyString()))
            .thenThrow(new HelmOrchestratorException(HELM_EXECUTOR_SERVICE_UNAVAILABLE, "test", "test"));

        helmJob.execute();
        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.PENDING);
    }

    @Test
    void givenInstanceWithInstantiatedStatusAndArtefactWithFailedStatus_WhenHelmJobRun_ThenInstanceAndArtefactHealthIsFailed() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.INSTANTIATED, HealthStatus.FAILED, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
    }

    @Test
    void givenInstanceWithPendingStatusAndArtefactWithTerminatedStatus_WhenHelmJobRun_ThenInstanceAndArtefactHealthIsPending() {
        List<AppInstance> objectUnderTest = getAppInstance(HealthStatus.PENDING, HealthStatus.TERMINATED, null);

        when(appInstancerepository.findAll(any(Specification.class)))
            .thenReturn(objectUnderTest);

        helmJob.execute();

        assertThat(objectUnderTest.get(0).getHealthStatus()).isEqualTo(HealthStatus.TERMINATED);
        assertThat(objectUnderTest.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.TERMINATED);
    }

    private List<AppInstance> getAppInstance(HealthStatus instanceHealthStatus, HealthStatus artefact1HealthStatus, HealthStatus artefact2HealthStatus) {
        AppInstance instance = AppInstance.builder().id(2L).appOnBoardingAppId(1L).build();
        instance.setHealthStatus(instanceHealthStatus);
        ArtifactInstance artifact = ArtifactInstance.builder().id(1L).appOnBoardingArtifactId(1L).operationId("1").build();
        artifact.setHealthStatus(artefact1HealthStatus);
        if(artefact2HealthStatus != null){
            ArtifactInstance secondArtifact = ArtifactInstance.builder().id(2L).appOnBoardingArtifactId(1L).build();
            artifact.setHealthStatus(artefact1HealthStatus);
            instance.setArtifactInstances(List.of(artifact, secondArtifact));
        }else{
            instance.setArtifactInstances(List.of(artifact));
        }

        return Arrays.asList(instance);
    }

    private ResponseEntity<OperationDto> createOperationDtoResponse(final String operationState, final String operationType, HttpStatus httpStatus) {
        final OperationDto operationDto = new OperationDto();
        operationDto.setState(operationState);
        operationDto.setType(operationType);
        return new ResponseEntity<OperationDto>(operationDto, httpStatus);
    }
}
