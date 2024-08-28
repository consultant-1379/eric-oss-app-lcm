/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.ae.presentation.services.artifactinstance;

import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.EnvironmentHolder;
import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Sql(scripts = { "file:src/test/resources/sql/monitoring/monitoring_data.sql" })
public class ArtifactInstanceServiceTest {

    @Autowired
    private ArtifactInstanceService objectUnderTest;

    @MockBean
    private ArtifactInstanceRepository artifactInstanceRepository;

    @MockBean
    private HelmOrchestratorClient helmOrchestratorClient;

    @SpyBean
    private EnvironmentHolder environmentHolder;


    @Test
    public void givenATerminateRequestForArtifactInstance_WhenWorkloadInstanceNotFoundResponse_ThenHealthStatusIsSetToFailed()
            throws URISyntaxException {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").build();

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any())).thenReturn(createResponse(HttpStatus.NOT_FOUND, ""));
        objectUnderTest.terminateArtifactInstance(actualArtifactInstance);

        verify(artifactInstanceRepository, times(1)).save(actualArtifactInstance);

        assertEquals(HealthStatus.FAILED, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenATerminateRequestForArtifactInstance_WhenHelmOrchestratorReturnsAcceptedResponse_ThenHealthStatusIsSetToPending()
            throws URISyntaxException {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").build();

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any()))
                .thenReturn(createResponse(HttpStatus.ACCEPTED, "http://localhost/cnwlcm/v1/operations/test-operation-id"));

        objectUnderTest.terminateArtifactInstance(actualArtifactInstance);

        verify(artifactInstanceRepository, times(1)).save(actualArtifactInstance);
        assertThat(actualArtifactInstance.getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(actualArtifactInstance.getOperationId()).isEqualTo("test-operation-id");
    }

    @Test
    public void givenADeleteRequestForArtifactInstance_WhenBadResponseRetrievedFromHelmOrchestrator_ThenHealthStatusIsSetToFailed()
            throws URISyntaxException {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").build();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(actualArtifactInstance.getWorkloadInstanceId()))
                .thenReturn(createResponse(HttpStatus.BAD_REQUEST, ""));
        objectUnderTest.deleteArtifactInstance(actualArtifactInstance);

        verify(artifactInstanceRepository, times(1)).save(actualArtifactInstance);

        assertEquals(HealthStatus.FAILED, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenADeleteRequestForArtifactInstance_WhenHelmOrchestratorReturnsAcceptedResponse_ThenHealthStatusIsSetToDeleted()
            throws URISyntaxException {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").build();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(actualArtifactInstance.getWorkloadInstanceId()))
                .thenReturn(createResponse(HttpStatus.NO_CONTENT, "http://localhost/cnwlcm/v1/operations/test-operation-id"));

        objectUnderTest.deleteArtifactInstance(actualArtifactInstance);

        verify(artifactInstanceRepository, times(1)).save(actualArtifactInstance);
        assertEquals(HealthStatus.DELETED, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenASaveRequestForArtifactInstance_WhenArtifactInstanceSuccessfullySavedByJPARepository_ThenArtifactInstanceDtoObjectSuccessfullyRetrievedFromRepository() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("000-000")
                .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();

        when(artifactInstanceRepository.save(actualArtifactInstance)).thenReturn(actualArtifactInstance);
        when(artifactInstanceRepository.findByAppInstanceIdAndId(anyLong(), anyLong())).thenReturn(java.util.Optional.of(actualArtifactInstance));

        objectUnderTest.saveArtifactInstance(actualArtifactInstance);
        final ArtifactInstanceDto expectedArtifactInstanceDto = objectUnderTest.getArtifactInstance(actualArtifactInstance.getAppInstance().getId(),
                actualArtifactInstance.getId());

        assertThat(actualArtifactInstance.getId()).isEqualTo(expectedArtifactInstanceDto.getArtifactInstanceId());
        assertThat(actualArtifactInstance.getHealthStatus().toString()).isEqualTo(expectedArtifactInstanceDto.getHealthStatus());
    }

    @Test
    public void givenAGetAllArtifactInstancesByAppInstanceIdRequest_WhenRepositoryContainsArtifactInstances_ThenReturnArtifactInstancesDtoObject() {
        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(1L)).thenReturn(java.util.Optional
                .of(List.of(ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build(),
                        ArtifactInstance.builder().id(3L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build())));

        final ArtifactInstancesDto actualArtifactInstancesDto = objectUnderTest.getAllArtifactInstances(1L);

        assertThat(actualArtifactInstancesDto.getArtifactInstances().get(0).getArtifactInstanceId()).isEqualTo(2L);
        assertThat(actualArtifactInstancesDto.getArtifactInstances().get(1).getArtifactInstanceId()).isEqualTo(3L);
    }

    @Test
    public void givenV2GetAllArtifactInstancesByAppInstanceIdRequest_WhenRepositoryContainsArtifactInstances_ThenReturnArtifactInstancesDtoObject() {
        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(1L)).thenReturn(java.util.Optional
                .of(List.of(ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build(),
                        ArtifactInstance.builder().id(3L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build())));

        final ArtifactInstancesDto actualArtifactInstancesDto = objectUnderTest.getAllArtifactInstances(1L, Version.V2);

        assertThat(actualArtifactInstancesDto.getArtifactInstances().get(0).getArtifactInstanceId()).isEqualTo(2L);
        assertThat(actualArtifactInstancesDto.getArtifactInstances().get(1).getArtifactInstanceId()).isEqualTo(3L);
    }

    @Test
    public void givenAGetAllArtifactInstancesByAppInstanceIdRequest_WhenRepositoryContainsNoArtifactInstancesForThisAppId_ThenThrowResourceNotFoundException() {
        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.getAllArtifactInstances(1L);
        });

        assertThat(actualResourceNotFoundException.getMessage()).isEqualTo("Artifact Instances belonging to appInstanceId = 1 were not found");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(ARTIFACT_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenV2GetAllArtifactInstancesByAppInstanceIdRequest_WhenRepositoryContainsNoArtifactInstancesForThisAppId_ThenReturnEmptyArtifactInstanceList() {
        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(1L)).thenReturn(Optional.empty());

        final ArtifactInstancesDto actualArtifactInstancesDto = objectUnderTest.getAllArtifactInstances(1L, Version.V2);
        assertThat(actualArtifactInstancesDto).isNotNull();
    }

    @Test
    public void givenAGetRequestForArtifactInstanceByAppInstanceIdAndArtifactInstanceId_WhenArtifactInstanceExistsInRepository_ThenReturnArtifactInstanceDtoObject() {
        when(artifactInstanceRepository.findByAppInstanceIdAndId(1L, 2L)).thenReturn(java.util.Optional
                .of(ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build()));

        final ArtifactInstanceDto actualArtifactInstanceDto = objectUnderTest.getArtifactInstance(1L, 2L);

        assertThat(actualArtifactInstanceDto.getArtifactInstanceId()).isEqualTo(2L);
        assertThat(actualArtifactInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
    }

    @Test
    public void givenV2GetRequestForArtifactInstanceByAppInstanceIdAndArtifactInstanceId_WhenArtifactInstanceExistsInRepository_ThenReturnArtifactInstanceDtoObject() {
        when(artifactInstanceRepository.findByAppInstanceIdAndId(1L, 2L)).thenReturn(java.util.Optional
                .of(ArtifactInstance.builder().id(2L).workloadInstanceId("000-000").appInstance(AppInstance.builder().build()).build()));

        final ArtifactInstanceDto actualArtifactInstanceDto = objectUnderTest.getArtifactInstance(1L, 2L, Version.V2);

        assertThat(actualArtifactInstanceDto.getArtifactInstanceId()).isEqualTo(2L);
        assertThat(actualArtifactInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
    }

    @Test
    public void givenAGetRequestForArtifactInstanceByAppInstanceIdAndArtifactInstanceId_WhenArtifactInstanceDoesNotExistInRepository_ThenThrowResourceNotFoundException() {
        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.getArtifactInstance(1L, 2L);
        });

        assertThat(actualResourceNotFoundException.getMessage())
                .isEqualTo("Artifact Instance with artifactInstanceId = 2 belonging to appInstanceId = 1 was not found");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(ARTIFACT_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenV2GetRequestForArtifactInstanceByAppInstanceIdAndArtifactInstanceId_WhenArtifactInstanceDoesNotExistInRepository_ThenReturnNull() {
        when(artifactInstanceRepository.findByAppInstanceIdAndId(1L, 2L)).thenReturn(Optional.empty());

        final ArtifactInstanceDto actualArtifactInstanceDto = objectUnderTest.getArtifactInstance(1L, 2L, Version.V2);
        assertThat(actualArtifactInstanceDto).isNull();
    }

    @Test
    public void givenAUpdateRequestForArtifactInstance_WhenHelmOrchestratorReturnsAcceptedResponse_ThenHealthStatusIsSetToPending() throws URISyntaxException {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://localhost/cnwlcm/v1/operations/test-operation-id"));
        ResponseEntity<WorkloadInstanceDto> wiDto = new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(helmOrchestratorClient.updateApp(any(), any(), any())).thenReturn(wiDto);
        objectUnderTest.updateArtifactInstance(actualArtifactInstance, new ByteArrayResource(new byte[0]),
            new AppInstancePutRequestDto().appInstanceId(2L).appOnBoardingAppId(2L));

        assertEquals(HealthStatus.PENDING, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenAUpdateRequestForArtifactInstance_WhenHealthStatusIsFailed_ThenHealthStatusIsStillFailed() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.FAILED);

        objectUnderTest.updateArtifactInstance(actualArtifactInstance, new ByteArrayResource(new byte[0]),
            new AppInstancePutRequestDto().appInstanceId(2L).appOnBoardingAppId(2L));

        assertEquals(HealthStatus.FAILED, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenWorkLoadInstanceDoesNotExistInHelmOrchestrator_WhenCallUpdateAppInstanceEndpoint_ThenHealthStatusRemainsInstantiated() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(helmOrchestratorClient.updateApp(any(), any(), any())).thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NOT_FOUND));
        objectUnderTest.updateArtifactInstance(actualArtifactInstance, new ByteArrayResource(new byte[0]),
            new AppInstancePutRequestDto().appInstanceId(2L).appOnBoardingAppId(2L));

        assertEquals(HealthStatus.INSTANTIATED, actualArtifactInstance.getHealthStatus());
    }

    @Test
    public void givenIncorrectRequestToHelmOrchestrator_WhenCallUpdateAppInstanceEndpoint_ThenHealthStatusRemainsInstantiated() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(helmOrchestratorClient.updateApp(any(), any(), any())).thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST));
        objectUnderTest.updateArtifactInstance(actualArtifactInstance, new ByteArrayResource(new byte[0]),
            new AppInstancePutRequestDto().appInstanceId(2L).appOnBoardingAppId(2L));

        assertEquals(HealthStatus.INSTANTIATED, actualArtifactInstance.getHealthStatus());
    }

    private ResponseEntity<Void> createResponse(final HttpStatus httpStatus, final String locationHeader) throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(locationHeader));
        return new ResponseEntity<>(headers, httpStatus);
    }
}
