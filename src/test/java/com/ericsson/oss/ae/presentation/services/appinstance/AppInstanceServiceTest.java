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

package com.ericsson.oss.ae.presentation.services.appinstance;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_URL;
import static com.ericsson.oss.ae.constants.AppLcmConstants.FAILED_TO_DELETE_MESSAGE;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_CONTACT_ADMIN;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_DELETING_ERROR;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_DISABLE;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_IS_NOT;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_METHOD_DELETION;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_METHOD_INSTANTIATION;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_MODE_DISABLED;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_REQUESTED_APP;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_THE_APP;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_CNWLCM;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_WORKLOAD_INSTANCES;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_WORKLOAD_INSTANCE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_LCM_PARTIAL_DELETE_FAILURE;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ONBOARDING_APP_NOT_FOUND;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_DELETE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_DISABLED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_ENABLED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_UPDATE_DELETING_FAILED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_CREATE_SECRET;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_DELETE;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_RETRIEVE_ARTIFACT;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_TERMINATE_APP;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_UPDATE_APP;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_ORCHESTRATOR_DELETE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.ID_FROM_CLIENT_NOT_FOUND;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstanceListRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClient;
import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.Permission;
import com.ericsson.oss.ae.clients.apponboarding.mapper.ArtifactMapper;
import com.ericsson.oss.ae.clients.apponboarding.model.Artifact;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.EnvironmentHolder;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.clients.keycloak.dto.RoleDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.CredentialEvent;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmServiceException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnBoardingDeleteException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnBoardingModeException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnboardingAppNotExistException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToDeleteException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToTerminateException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToUpdateException;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.presentation.exceptions.KeycloakException;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.presentation.exceptions.UnableToRetrieveArtifactException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.ae.repositories.CredentialEventRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils;
import com.ericsson.oss.ae.utils.json.JsonUtils;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;

@SpringBootTest
class AppInstanceServiceTest {
    @Autowired
    private AppInstanceService objectUnderTest;

    @Autowired
    private UrlGenerator urlGenerator;

    @MockBean
    private AppInstanceRepository repository;

    @MockBean
    private ArtifactInstanceRepository artifactInstanceRepository;

    @MockBean
    private ArtifactMapper artifactMapper;

    @MockBean
    private CredentialEventRepository credentialEventRepository;

    @MockBean
    private AppOnboardingClient appOnboardingClient;

    @MockBean
    private HelmOrchestratorClient helmOrchestratorClient;

    @SpyBean
    private EnvironmentHolder environmentHolder;

    @MockBean
    private KeycloakClient keycloakClient;

    @Captor
    private ArgumentCaptor<List<AppInstance>> appInstanceCaptorList;

    @Test
    public void givenAppExistInOnBoardingAsDisabled_WhenTryToInstantiate_ThenThrowAppOnBoardingDisabledException() throws IOException {
        final Long appId = 5L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode(APP_ONBOARDING_MODE_DISABLED);
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));

        final AppOnBoardingModeException actualAppOnBoardingDisabled = assertThrows(AppOnBoardingModeException.class,
                                                                                    () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(appId)));

        assertThat(actualAppOnBoardingDisabled.getAppLcmError()).isEqualTo(APP_ON_BOARDING_DISABLED);
        assertThat(actualAppOnBoardingDisabled.getMessage()).isEqualTo("Requested app " + appId
                                                                           + " is not enabled. Please contact app admin to enable the app for app " + APP_ONBOARDING_METHOD_INSTANTIATION);
    }

    @Test
    void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiate_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {

        final Long appId = 5L;
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseWithKeyclockClientSuccess.json", AppDto.class);
        Permission permission =  new Permission();
        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        testApp.setPermissions(permissionList);
        testApp.setMode("ENABLED");
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        runKeycloakMocks();
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
            .create(new AppInstancePostRequestDto().appId(appId).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(appId);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAppExistInOnBoardingWithModeNull_WhenTryToInstantiate_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {

        final Long appId = 5L;
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        mockKeycloakCall();
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
            .create(new AppInstancePostRequestDto().appId(appId).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(appId);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    void givenNoRolesPresent_WhenCreateAndPostInstantiateAppRequest_DeleteKeyclockClient() throws IOException, URISyntaxException {
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseWithKeyclockClientSuccess.json", AppDto.class);
        RoleDto roleDto = new RoleDto();
        roleDto.setName("user");
        testApp.setRoles(Arrays.asList(roleDto));
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("clientId");

        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenThrow(new HelmOrchestratorException(HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, "Error", "https://instantiate"));
        when(credentialEventRepository.findByAppOnBoardingAppId(1L)).thenReturn(credentialEvent);
        runKeycloakMocks();

        assertThrows(KeycloakException.class, ()->
                objectUnderTest.create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test"))));

        verify(keycloakClient, times(1)).deleteClientByClientId(any());
    }

    @Test
    void givenHelmOrchestratorException_WhenCreateAndPostInstantiateAppRequest_ThenThrowHelmExecutorException() throws IOException, URISyntaxException {
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseWithKeyclockClientSuccess.json", AppDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("clientId");

        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenThrow(new HelmOrchestratorException(HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, "Error", "https://instantiate"));
        when(credentialEventRepository.findByAppOnBoardingAppId(1L)).thenReturn(credentialEvent);
        runKeycloakMocks();

        assertThrows(HelmOrchestratorException.class, ()->
                objectUnderTest.create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test"))));

        verify(credentialEventRepository, times(1)).save(any());
    }

    @Test
    public void givenAppInstanceAlreadyInTerminatedState_WhenCallTerminateAppInstanceByIdEndpoint_ThenThrowFailureToTerminateException() {
        when(repository.findById(1L))
            .thenReturn(Optional.of(AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.TERMINATED).build()));

        final FailureToTerminateException actualFailureToTerminateException = assertThrows(FailureToTerminateException.class,
                                                                                           () -> objectUnderTest.terminate(1L));

        assertThat(actualFailureToTerminateException.getMessage()).isEqualTo("App Instance should be instantiated (or Failed) to terminate it.");
        assertThat(actualFailureToTerminateException.getAppLcmError()).isEqualTo(FAILURE_TO_TERMINATE_APP);
    }

    @Test
    public void givenAppInstanceDoesNotExistInRepository_WhenCallTerminateAppInstanceByIdEndpoint_ThenThrowResourceNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                () -> objectUnderTest.terminate(1L));

        assertThat(actualResourceNotFoundException.getMessage()).isEqualTo("Could not find App Instance with ID '1'");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenAppInstanceWithHealthStatusInstantiated_WhenCallTerminateAppInstanceByIdEndpoint_ThenAppInstanceHealthStatusUpdatedToTerminated()
        throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("workLoadInstanceId").build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED)
                .artifactInstances(Collections.singletonList(artifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        headers.setLocation(new URI(urlGenerator.generateOperationsByIdUrl("operationId")));

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any())).thenReturn(new ResponseEntity<>(headers, HttpStatus.ACCEPTED));

        objectUnderTest.terminate(1L);

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }
    @Test
    public void givenAppInstanceWithHealthStatusInstantiatedAndCredentialRepoEmpty_WhenCallTerminateAppInstanceByIdEndpoint_ThenAppInstanceHealthStatusUpdatedToTerminatedAndKeycloakNotDeleted()
            throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("workLoadInstanceId").build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED)
                .artifactInstances(Collections.singletonList(artifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        headers.setLocation(new URI(urlGenerator.generateOperationsByIdUrl("operationId")));

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any())).thenReturn(new ResponseEntity<>(headers, HttpStatus.ACCEPTED));
        when(credentialEventRepository.findByAppOnBoardingAppId(1L)).thenReturn(null);

        objectUnderTest.terminate(1L);

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }
    @Test
    public void givenAppInstanceWithHealthStatusInstantiatedAndClientIdNull_WhenCallTerminateAppInstanceByIdEndpoint_ThenAppInstanceHealthStatusUpdatedToTerminatedAndKeycloakNotDeleted()
            throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("workLoadInstanceId").build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId(null);

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED)
                .artifactInstances(Collections.singletonList(artifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        headers.setLocation(new URI(urlGenerator.generateOperationsByIdUrl("operationId")));

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any())).thenReturn(new ResponseEntity<>(headers, HttpStatus.ACCEPTED));
        when(credentialEventRepository.findByAppOnBoardingAppId(1L)).thenReturn(credentialEvent);

        objectUnderTest.terminate(1L);

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }

    @Test
    public void givenAppInstanceWithHealthStatusInstantiatedAndCredentialEvent_WhenCallTerminateAppInstanceByIdEndpoint_ThenAppInstanceHealthStatusUpdatedToTerminatedAndKeycloakDeleted()
            throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("workLoadInstanceId").build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        CredentialEvent credentialEvent = new CredentialEvent();
        String clientId = "rapp_1234";
        String scope = "custom_scope_1234";
        credentialEvent.setClientId(clientId);
        credentialEvent.setClientScope(scope);
        ClientDto[] clientDtoList = new ClientDto[1];
        ClientDto clientDto = new ClientDto();
        clientDto.setClientId(clientId);
        clientDto.setName(scope);
        clientDto.setId("1L");
        clientDtoList[0] = clientDto;

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED)
                .artifactInstances(Collections.singletonList(artifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        headers.setLocation(new URI(urlGenerator.generateOperationsByIdUrl("operationId")));

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));

        when(helmOrchestratorClient.terminateAppInstanceById(any(), any())).thenReturn(new ResponseEntity<>(headers, HttpStatus.ACCEPTED));
        when(credentialEventRepository.findByAppOnBoardingAppId(any())).thenReturn(credentialEvent);
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity<>(clientDtoList, HttpStatus.ACCEPTED));
        when(keycloakClient.deleteClient(any())).thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        objectUnderTest.terminate(1L);

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }

    @Test
    public void givenAValidPostRequestDto_WhenCreateAppInstanceAndAppDoesExistInRepository_ThenReturnAppInstanceDtoObject()
        throws URISyntaxException, IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        mockKeycloakCall();
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAValidPutRequestDtoObject_WhenUpdateAppInstance_ThenAppInstanceAppOnBoardingAppIdIsUpdated() throws IOException,
        URISyntaxException {
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        ResponseEntity<AppDto> appResponse = new ResponseEntity<>(testApp, HttpStatus.OK);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000")
            .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(2L).id(1L).targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(appResponse);
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.updateApp(any(), any(), any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstanceId"), headers, HttpStatus.ACCEPTED));
        when(artifactInstanceRepository.save(actualArtifactInstance)).thenReturn(actualArtifactInstance);
        when(artifactInstanceRepository.findByAppInstanceIdAndId(anyLong(), anyLong())).thenReturn(java.util.Optional.of(actualArtifactInstance));

        AppInstanceDto actualAppInstanceDto =
            objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(3L));

        assertEquals(3L, actualAppInstanceDto.getAppOnBoardingAppId());
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
    }

    @Test
    public void givenAValidPutRequestDtoObjectWithAdditionalParameters_WhenUpdateAppInstance_ThenAppInstanceAppOnBoardingAppIdIsUpdated() throws IOException,
            URISyntaxException {
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        ResponseEntity<AppDto> appResponse = new ResponseEntity<>(testApp, HttpStatus.OK);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000")
                .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(2L).id(1L).targetStatus(TargetStatus.INSTANTIATED)
                .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(appResponse);
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.updateApp(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstanceId"), headers, HttpStatus.ACCEPTED));
        when(artifactInstanceRepository.save(actualArtifactInstance)).thenReturn(actualArtifactInstance);
        when(artifactInstanceRepository.findByAppInstanceIdAndId(anyLong(), anyLong())).thenReturn(java.util.Optional.of(actualArtifactInstance));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(CredentialEvent.builder().appOnBoardingAppId(2L).clientId("IdClient").clientSecret("SecretClient").build());
        Map<String, Object> additionalParameters = new HashMap<>();
        AppInstancePutRequestDto appInstancePutRequestDto = new AppInstancePutRequestDto();
        appInstancePutRequestDto.setAppInstanceId(1L);
        appInstancePutRequestDto.setAppOnBoardingAppId(3L);
        additionalParameters.put("replicaCount",3);
        additionalParameters.put(KeycloakConstants.CLIENT_ID_NAME,"IdClient");
        additionalParameters.put(KeycloakConstants.CLIENT_SECRET_NAME,"SecretClient");
        additionalParameters.put(KeycloakConstants.KEYCLOAK_URL_NAME,"http://eric-sec-access-mgmt-http:8080");
        appInstancePutRequestDto.setAdditionalParameters(additionalParameters);
        AppInstanceDto actualAppInstanceDto =
                objectUnderTest.updateAppInstance(appInstancePutRequestDto);

        assertEquals(3L, actualAppInstanceDto.getAppOnBoardingAppId());
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).isEqualTo("{\"replicaCount\":3,\"baseUrl\":\"http://eric-sec-access-mgmt-http:8080\",\"clientId\":\"IdClient\",\"clientSecret\":\"SecretClient\"}");
    }

    @Test
    public void givenAValidPutRequestDtoObject_WhenUpdateAppInstanceAndArtifactNotFoundInOnboarding_ThenAppOnboardingAppNotExistExceptionIsThrownWithCorrectValues() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000")
            .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(1L).id(2L).targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        final String message = "App with ID " + 1L + " " + "does not exist or hasn't been fully onboarded";
        when(appOnboardingClient.getAppById(anyLong())).thenThrow(new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, message, APP_INSTANCES_URL));

        AppOnboardingAppNotExistException exception = assertThrows(AppOnboardingAppNotExistException.class,
            () -> objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(2L)));

        assertThat(exception.getMessage()).isEqualTo("App with ID 1 does not exist or hasn't been fully onboarded");
    }

    @Test
    public void givenAValidPutRequestDtoObject_WhenUpdateAppInstanceAndAppInstanceCannotBeFoundInRepository_ThenResourceNotFoundExceptionIsThrownWithCorrectValues() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(2L)));

        assertThat(exception.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Could not find App Instance with ID '1'");
    }

    @Test
    public void givenAValidPostRequestDto_WhenCreateAppInstanceAndAppDoesNotExistInAppOnboarding_ThenThrowAppOnboardingAppNotExistException() {
        setUpContextForCreateMethodTest(new ByteArrayResource(new byte[0]), new HttpHeaders(), new ArrayList<>(), HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        final String message = "App with ID " + 5L + " " + "does not exist or hasn't been fully onboarded";
        when(appOnboardingClient.getAppById(anyLong())).thenThrow(new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, message, APP_INSTANCES_URL));

        final AppOnboardingAppNotExistException actualAppOnboardingAppNotExistException = assertThrows(AppOnboardingAppNotExistException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(5L)));

        assertThat(actualAppOnboardingAppNotExistException.getMessage()).isEqualTo("App with ID 5 does not exist or hasn't been fully onboarded");
        assertThat(actualAppOnboardingAppNotExistException.getAppLcmError()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
    }

    @Test
    public void givenAValidPostRequestDto_WhenCreateAppInstanceAndAppDoesNotHaveArtifacts_ThenThrowAppOnboardingArtifactRetrievalException() {

        final AppDto testApp = new AppDto();
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));

        final UnableToRetrieveArtifactException actualAppOnboardingArtifactRetrievalException = assertThrows(UnableToRetrieveArtifactException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(5L)));

        assertThat(actualAppOnboardingArtifactRetrievalException.getMessage()).isEqualTo("Artifact of type HELM could not be found");
        assertThat(actualAppOnboardingArtifactRetrievalException.getAppLcmError()).isEqualTo(FAILURE_TO_RETRIEVE_ARTIFACT);
    }

    @Test
    public void givenAValidPostRequestDto_WhenCreatingAppInstanceAndHelmFileIsEmpty_ThenThrowUnableToRetrieveArtifactException() {
        final AppDto testApp = new AppDto();
        testApp.setId(1L);
        final ArtifactDto expectedArtifactDto = new ArtifactDto().id(1L).type("HELM");
        testApp.setArtifacts(List.of(expectedArtifactDto));
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] {}), HttpStatus.OK));
        final UnableToRetrieveArtifactException actualAppOnboardingArtifactRetrievalException = assertThrows(UnableToRetrieveArtifactException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(5L)));

        assertThat(actualAppOnboardingArtifactRetrievalException.getMessage()).isEqualTo("Artifact File is Empty");
        assertThat(actualAppOnboardingArtifactRetrievalException.getAppLcmError()).isEqualTo(FAILURE_TO_RETRIEVE_ARTIFACT);
    }

    @Test
    public void givenAppInstanceDoesNotExistInAppLCM_WhenAValidCallToGetAppInstanceIsMade_ThenThrowResourceNotFoundException() {
        when(repository.findById(22L)).thenReturn(Optional.empty());

        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.getAppInstance(22L);
        });

        assertThat(actualResourceNotFoundException.getMessage()).isEqualTo("App Instance by appInstanceId = 22 was not found");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenV2AppInstanceDoesNotExistInAppLCM_WhenAValidCallToGetAppInstanceIsMade_ThenReturnNull() {
        when(repository.findById(22L)).thenReturn(Optional.empty());

        final AppInstanceDto actualAppInstanceDto = objectUnderTest.getAppInstance(3L, Version.V2);
        assertThat(actualAppInstanceDto).isNull();
    }

    @Test
    public void givenAppInstanceDoesExistInRepository_WhenAValidCallToGetAppInstanceIsMade_ThenReturnAppInstanceDtoObject() {
        final AppInstance actualAppInstance = AppInstance.builder().id(3L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.PENDING);

        when(repository.findById(3L)).thenReturn(Optional.of(actualAppInstance));

        final AppInstanceDto actualAppInstanceDto = objectUnderTest.getAppInstance(3L);

        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getId()).isEqualTo(3L);
    }

    @Test
    public void givenV2AppInstanceDoesExistInRepository_WhenAValidCallToGetAppInstanceIsMade_ThenReturnAppInstanceDtoObject() {
        final AppInstance actualAppInstance = AppInstance.builder().id(3L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.PENDING);

        when(repository.findById(3L)).thenReturn(Optional.of(actualAppInstance));

        final AppInstanceDto actualAppInstanceDto = objectUnderTest.getAppInstance(3L, Version.V2);

        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getId()).isEqualTo(3L);
    }

    @Test
    public void givenAppInstancesDoNotExistInRepository_WhenAValidCallToGetAllAppInstancesIsMade_ThenThrowResourceNotFoundException() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.getAllAppInstances(null);
        });

        assertThat(actualResourceNotFoundException.getMessage()).isEqualTo("App Instances not found");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenV2AppInstancesDoNotExistInRepository_WhenAValidCallToGetAllAppInstancesIsMade_ThenReturnAppInstancesDtoObject() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        final AppInstancesDto actualAppInstancesDto = objectUnderTest.getAllAppInstances(3L, Version.V2);
        assertThat(actualAppInstancesDto).isNotNull();
    }

    @Test
    public void givenAppInstancesDoNotExistInRepository_WhenAValidCallToGetAllAppInstancesIsMadeWithSpecificId_ThenThrowResourceNotFoundException() {
        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);
        when(repository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(appInstance1)));
        when(repository.findByAppOnBoardingAppId(any())).thenReturn(new ArrayList<>());

        final ResourceNotFoundException actualResourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.getAllAppInstances(2L);
        });

        assertThat(actualResourceNotFoundException.getMessage()).isEqualTo("App Instances not found");
        assertThat(actualResourceNotFoundException.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
    }

    @Test
    public void givenAppInstancesDoExistInRepository_WhenAValidCallToGetAllAppInstancesIsMade_ThenReturnAppInstancesDtoObject() {
        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);
        final AppInstance appInstance2 = AppInstance.builder().id(3L).appOnBoardingAppId(4L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance2.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(appInstance1, appInstance2)));

        final AppInstancesDto actualAppInstancesDto = objectUnderTest.getAllAppInstances(null);
        assertThat(actualAppInstancesDto.getAppInstances().get(0).getId()).isEqualTo(1L);
        assertThat(actualAppInstancesDto.getAppInstances().get(1).getId()).isEqualTo(3L);
    }

    @Test
    public void givenAppInstancesDoExistInRepository_WhenAValidCallToGetAllAppInstancesByIdIsMade_ThenReturnAppInstancesDtoObject() {
        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);
        final AppInstance appInstance2 = AppInstance.builder().id(3L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance2.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(appInstance1, appInstance2)));
        when(repository.findByAppOnBoardingAppId(any())).thenReturn(new ArrayList<>(Arrays.asList(appInstance1, appInstance2)));
        final AppInstancesDto actualAppInstancesDto = objectUnderTest.getAllAppInstances(2L);
        assertThat(actualAppInstancesDto.getAppInstances().get(0).getId()).isEqualTo(1L);
        assertThat(actualAppInstancesDto.getAppInstances().get(1).getId()).isEqualTo(3L);


    }

    @Test
    public void givenV2AppInstancesDoExistInRepository_WhenAValidCallToGetAllAppInstancesByIdIsMade_ThenReturnAppInstancesDtoObject() {
        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);
        final AppInstance appInstance2 = AppInstance.builder().id(3L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance2.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(appInstance1, appInstance2)));
        when(repository.findByAppOnBoardingAppId(any())).thenReturn(new ArrayList<>(Arrays.asList(appInstance1, appInstance2)));
        final AppInstancesDto actualAppInstancesDto = objectUnderTest.getAllAppInstances(2L, Version.V2);
        assertThat(actualAppInstancesDto.getAppInstances().get(0).getId()).isEqualTo(1L);
        assertThat(actualAppInstancesDto.getAppInstances().get(1).getId()).isEqualTo(3L);
    }

    @Test
    public void givenAppInstancesDoExistInRepository_WhenAValidCallToGetAllAppInstancesByIdIsMadeWithMultipleIds_ThenReturnAppInstancesDtoObject() {
        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(appInstance1)));
        when(repository.findByAppOnBoardingAppId(any())).thenReturn(new ArrayList<>(Arrays.asList(appInstance1)));
        final AppInstancesDto actualAppInstancesDto = objectUnderTest.getAllAppInstances(2L);
        assertThat(actualAppInstancesDto.getAppInstances().get(0).getId()).isEqualTo(1L);

    }

    @Test
    public void givenAppDoesNotExistInOnboarding_WhenCallUpdateAppInstanceEndpoint_ThenReturnHttpStatus400AndThrowException() {
        setUpContextForCreateMethodTest(new ByteArrayResource(new byte[0]), new HttpHeaders(), new ArrayList<>(), HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        final String message = "App with ID " + 5L + " " + "does not exist or hasn't been fully onboarded";
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000")
            .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(2L).id(1L).targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(appOnboardingClient.getAppById(anyLong())).thenThrow(new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, message, APP_INSTANCES_URL));

        final AppOnboardingAppNotExistException actualAppOnboardingAppNotExistException = assertThrows(AppOnboardingAppNotExistException.class,
            () ->  objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(2L)));

        assertThat(actualAppOnboardingAppNotExistException.getMessage()).isEqualTo("App with ID 5 does not exist or hasn't been fully onboarded");
        assertThat(actualAppOnboardingAppNotExistException.getAppLcmError()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
    }

    @Test
    public void givenAppInstanceThatIsNotInstantiated_WhenUpdateAppInstance_ThenFailureToUpdateExceptionIsThrown() {
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(1L).workloadInstanceId("000-000")
            .appInstance(AppInstance.builder().id(1L).appOnBoardingAppId(2L).build()).build();
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(2L).id(1L).targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.FAILED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));

        final FailureToUpdateException actualFailureToUpdateException = assertThrows(FailureToUpdateException.class,
            () ->  objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(3L)));

        assertThat(actualFailureToUpdateException.getMessage()).isEqualTo("App Instance Id 1 should be instantiated to update it.");
        assertThat(actualFailureToUpdateException.getAppLcmError()).isEqualTo(FAILURE_TO_UPDATE_APP);
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusPending_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("auth");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);

        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("noScopeFound");
        clientDtoNotOk.setName("noScopeFound");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(credentialEvent);
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity<>(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");

    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndClientIdFound_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        Permission permission3 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");
        permission.setResource("mongodb");
        permission.setScope("auth");
        permission3.setResource("bdr");
        permission3.setScope("policyName");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        permissionList.add(permission3);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("noCustomScopeFound");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        clientDtoNotOk.setName("noCustomScopeFound");
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(credentialEvent);
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn(credentialEvent.getClientId());
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndClientIdFoundAndClientScopeNotFound_ThenReturnAppInstanceDtoObject()
            throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoNotOk.setName("test");
        clientDtoOk.setName("test");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(null);
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn(credentialEvent.getClientId());
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }





    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndClientIdFound_ThenReturnAppInstanceDtoObjectAndSavesCredential()
        throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(null);
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusFailed_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);

        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.FAILED);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(credentialEvent);
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
                .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusCreated_ThenReturnAppInstanceDtoObject()
        throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);

        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.CREATED);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(credentialEvent);
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
            .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    @Test
    public void givenAValidPostRequestDto_WhenCreateAppInstanceAndAppDoesExistInRepositoryAndIemFlowNotRequired_ThenReturnAppInstanceDtoObject()
        throws URISyntaxException, IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(new ArrayList<>());
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        mockKeycloakCall();
        final AppInstanceDto actualAppInstanceDto = objectUnderTest
            .create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test")));

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getAdditionalParameters()).contains("namespace", "test");
    }

    private void mockKeycloakCall() {
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setClientId("rapp_4_12345");
        ClientDto[] clientDtoList = new ClientDto[1];
        clientDtoList[0] = clientDtoOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(keycloakClient.createSecret(anyString())).thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        when(keycloakClient.createClientId("4")).thenReturn("rapp_4_12345");
    }

    //Delete app instance tests
    @Test
    void givenValidAppInstanceListWithAppIdWithTerminatedHealthStatus_WhenDeleteAppInstances_ThenAppInstancesAreDeleted(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());

        verify(repository, times(1)).deleteAll(anyList());
        verify(repository).deleteAll(appInstanceCaptorList.capture());
        List<AppInstance> captured = appInstanceCaptorList.getValue();
        equals(captured.size() == 1);
        equals(captured.get(0).getId().equals(appInstanceId));
        equals(captured.get(0).getAppOnBoardingAppId().equals(appId));
    }

    @Test
    void givenValidAppInstanceListWithAppIdWithFailedHealthStatusAndNoHelmWorkloadFound_WhenDeleteAppInstances_ThenAppInstancesAreDeleted(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());

        verify(repository, times(1)).deleteAll(anyList());
        verify(repository).deleteAll(appInstanceCaptorList.capture());
        List<AppInstance> captured = appInstanceCaptorList.getValue();
        equals(captured.size() == 1);
        equals(captured.get(0).getId().equals(appInstanceId));
        equals(captured.get(0).getAppOnBoardingAppId().equals(appId));
    }

    @Test
    void givenValidAllAppInstanceListWithCorruptedConnectionToDB_WhenDeleteAppInstances_ThenResourceNotFoundExceptionThrown(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final List<AppInstance> instances = new ArrayList<>(Arrays.asList(
            getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED)));

        when(repository.findAll(any(Specification.class)))
            .thenReturn(instances);
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError()).isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode()).isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode());
            assertThat(e.getErrorData().get(0).getFailureMessage()).isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage());
        }
    }

    @Test
    void givenInvalidAllAppInstanceList_WhenDeleteAppInstances_ThenResourceNotFoundExceptionThrown(){
        final Long appId = 1L;
        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (ResourceNotFoundException e){
            assertThat(e.getAppLcmError()).isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND);
            assertThat(e.getMessage()).isEqualTo(FAILED_TO_DELETE_MESSAGE + appId);
        }
    }

    @Test
    void givenAppInstanceListWithInstantiatedHealthStatus_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownForNotTerminatedOrFailed(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.INSTANTIATED))));

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode());
        }
    }

    @Test
    void givenOneValidInstanceAndOneInvalidInstanceInList_WhenDeleteAppInstances_ThenPartialDeleteExceptionThrownForNotTerminatedOrFailed(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.PENDING),
                                                      getAppInstance(appId, 1L, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(AppLcmError.APP_LCM_PARTIAL_DELETE_FAILURE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(APP_LCM_PARTIAL_DELETE_FAILURE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode());
        }
    }

    @Test
    void givenInvalidArtifactInstancesWithValidAppInstances_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownForHelmOrchestratorException(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final String url = urlGenerator.generateWorkloadInstanceByIdUrl(workloadId.toString());
        final AppLcmError error = HELM_ORCHESTRATOR_DELETE_ERROR;
        HelmOrchestratorException helmOrchestratorException = new HelmOrchestratorException(
            error, "Error message from Helm Executor. ", url);

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(helmOrchestratorException);
        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo("Error message from Helm Executor. " + HELM_WORKLOAD_INSTANCE_ERROR + workloadId);
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode());
        }
    }

    @Test
    void givenInvalidArtifactInstancesWithValidAppInstances_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownForHttpClientErrorException(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(httpClientErrorException);

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage() + " " + HELM_WORKLOAD_INSTANCE_ERROR + workloadId);
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode());
        }
    }

    @Test
    void givenInvalidArtifactInstancesWithValidAppInstances_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownForOtherException(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(NullPointerException.class);

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage() + " " + HELM_WORKLOAD_INSTANCE_ERROR + workloadId);
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode());
        }
    }

    /**
     * App Mode DISABLED
     * App Health Status FAILED
     * Expected behaviour: Running without any exceptions
     *
     * Negative test case from App Staging (Delete Failed App Instance by Operator)
     */
    @Test
    void givenAppWithAppHealthStatusFailed_WhenDeleteAppInstances_ThenFailedAppInstancesAreDeleted() throws IOException {

        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;

        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final AppInstance failedAppInstance = getAppInstance(appId, appInstanceId, workloadId, HealthStatus.FAILED);

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(failedAppInstance)));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());

        verify(repository, times(1)).findAll(any(Specification.class));
        verify(helmOrchestratorClient, times(1)).deleteWorkloadInstanceId(anyString());
    }

    /**
     * App Deletion tests
     */
    @Test
    void givenValidArtifactInstancesWithValidAppInstances_WhenDeleteApp_ThenInstancesAreInDeletingHealthAndAppDeletedTarget()
        throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED));
        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(appInstanceListTest);
        when(appOnboardingClient.updateStatusForDeletion(appId))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        objectUnderTest.deleteApp(appId);

        assertEquals(HealthStatus.DELETING, appInstanceListTest.get(0).getHealthStatus());
        assertEquals(TargetStatus.APP_DELETED, appInstanceListTest.get(0).getTargetStatus());
        verify(repository, times(1)).saveAll(appInstanceListTest);
        verify(repository).saveAll(appInstanceCaptorList.capture());
        List<AppInstance> capturedList = appInstanceCaptorList.getValue();
        equals(capturedList.get(0).getId().equals(appInstanceId));
        equals(capturedList.get(0).getAppOnBoardingAppId().equals(appId));
        equals(capturedList.get(0).getHealthStatus().equals(HealthStatus.DELETED));
        equals(capturedList.get(0).getTargetStatus().equals(TargetStatus.DELETED));
    }

    @Test
    void givenValidAppWithStatusDeleting_WhenDeleteApp_ThenInstancesAreInDeletingHealthAndAppDeletedTarget()
        throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        testApp.setStatus("DELETING");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED));
        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(appInstanceListTest);

        objectUnderTest.deleteApp(appId);

        assertEquals(HealthStatus.DELETING, appInstanceListTest.get(0).getHealthStatus());
        assertEquals(TargetStatus.APP_DELETED, appInstanceListTest.get(0).getTargetStatus());
        verify(repository, times(1)).saveAll(appInstanceListTest);
        verify(repository).saveAll(appInstanceCaptorList.capture());
        List<AppInstance> capturedList = appInstanceCaptorList.getValue();
        equals(capturedList.get(0).getId().equals(appInstanceId));
        equals(capturedList.get(0).getAppOnBoardingAppId().equals(appId));
        equals(capturedList.get(0).getHealthStatus().equals(HealthStatus.DELETED));
        equals(capturedList.get(0).getTargetStatus().equals(TargetStatus.APP_DELETED));
    }

    @Test
    void givenValidAppIdWithoutInstances_WhenDeleteApp_ThenDeleteOnBoardingResourcesOnly()
        throws IOException {
        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList());
        when(appOnboardingClient.updateStatusForDeletion(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        objectUnderTest.deleteApp(appId);

        verify(repository, times(1)).findAll(any(Specification.class));
        verify(appOnboardingClient, times(1)).getAppById(appId);
        verify(appOnboardingClient, times(1)).updateStatusForDeletion(appId);
        verify(appOnboardingClient, times(1)).deletePackage(appId);
        verifyNoMoreInteractions(appOnboardingClient);
    }

    @Test
    void givenValidRequestWithFailedToDeleteAppOnOnBoarding_WhenDeleteApp_ThenThrowAppOnBoardingDeleteException() throws IOException {
        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList());
        when(appOnboardingClient.updateStatusForDeletion(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (AppOnBoardingDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(APP_ON_BOARDING_DELETE_ERROR);
            assertThat(e.getMessage())
                .isEqualTo(APP_ONBOARDING_DELETING_ERROR+appId);
            assertThat(e.getAppLcmError().getErrorCode())
                .isEqualTo(APP_ON_BOARDING_DELETE_ERROR.getErrorCode());
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(APP_ON_BOARDING_DELETE_ERROR.getErrorMessage());
        }
    }

    @Test
    void givenValidRequestWithUnavailableService_WhenDeleteApp_ThenThrowAppLcmServiceException() throws IOException {
        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList());
        when(appOnboardingClient.updateStatusForDeletion(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenThrow(new ResourceAccessException("Unavailable"));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (AppLcmServiceException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE);
            assertThat(e.getMessage())
                .isEqualTo(APP_ONBOARDING_DELETING_ERROR + appId);
            assertThat(e.getAppLcmError().getErrorCode())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorCode());
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorMessage());
        }
    }

    @Test
    void givenValidRequestWithRestClientException_WhenDeleteApp_ThenThrowAppOnBoardingDeleteException() throws IOException {
        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList());
        when(appOnboardingClient.updateStatusForDeletion(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenThrow(new RestClientException("Rest exception"));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (AppOnBoardingDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_DELETE_ERROR);
            assertThat(e.getMessage())
                .isEqualTo(APP_ONBOARDING_DELETING_ERROR + appId);
            assertThat(e.getAppLcmError().getErrorCode())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_DELETE_ERROR.getErrorCode());
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(AppLcmError.APP_ON_BOARDING_DELETE_ERROR.getErrorMessage());
        }
    }

    @Test
    void givenValidRequestWithFailedToUpdateOnBoarding_WhenDeleteApp_ThenThrowAppOnBoardingNotFoundException() throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED));
        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(appInstanceListTest);
        when(appOnboardingClient.updateStatusForDeletion(appId))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (AppOnBoardingDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
            assertThat(e.getMessage())
                .isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage());
            assertThat(e.getAppLcmError().getErrorCode())
                .isEqualTo(APP_ONBOARDING_APP_NOT_FOUND.getErrorCode());
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(APP_ONBOARDING_APP_NOT_FOUND.getErrorMessage());
        }
    }

    @Test
    void givenValidRequestWithFailedToUpdateOnBoarding_WhenDeleteApp_ThenThrowAppOnBoardingUpdateFailedException() throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED));
        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(appInstanceListTest);
        when(appOnboardingClient.updateStatusForDeletion(appId))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (AppOnBoardingDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED);
            assertThat(e.getMessage())
                .isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage());
            assertThat(e.getAppLcmError().getErrorCode())
                .isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorCode());
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage());
        }
    }

    @Test
    void givenAppEnabled_WhenDeleteApp_ThenThrowAppOnBoardingModeException() throws IOException {
        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("ENABLED");

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));

        final AppOnBoardingModeException actualException =
            assertThrows(AppOnBoardingModeException.class,
                         () -> objectUnderTest.deleteApp(appId));

        assertThat(actualException.getAppLcmError())
            .isEqualTo(APP_ON_BOARDING_ENABLED);
        assertThat(actualException.getMessage())
            .isEqualTo(APP_ONBOARDING_REQUESTED_APP + appId + APP_ONBOARDING_IS_NOT + APP_ONBOARDING_MODE_DISABLED + ". " +
                           APP_ONBOARDING_CONTACT_ADMIN + APP_ONBOARDING_DISABLE + APP_ONBOARDING_THE_APP +APP_ONBOARDING_METHOD_DELETION);
    }

    @Test
    void givenOneValidAndOneInvalidArtifactInstancesWithValidAppInstancesList_WhenDeleteApp_ThenThrowFailureToDelete() throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        AppInstance invalidAppInstanceToTest = getAppInstance(appId, 1L, workloadId, HealthStatus.INSTANTIATED);
        AppInstance validAppInstanceToTest = getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED);

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(invalidAppInstanceToTest, validAppInstanceToTest)));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        try{
            objectUnderTest.deleteApp(appId);
        }catch (FailureToDeleteException e){
            assertEquals(FAILURE_TO_DELETE, e.getAppLcmError());
            assertEquals(0, e.getTotalSuccessfulDeletion());
            assertEquals(false, e.getErrorData().isEmpty());
            assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), e.getErrorData().get(0).getAppLcmErrorCode());
            assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), e.getErrorData().get(0).getFailureMessage());
            assertEquals(HealthStatus.INSTANTIATED, invalidAppInstanceToTest.getHealthStatus());
            assertEquals(HealthStatus.INSTANTIATED, invalidAppInstanceToTest.getArtifactInstances().get(0).getHealthStatus());
            assertEquals(HealthStatus.TERMINATED, validAppInstanceToTest.getHealthStatus());
            assertEquals(HealthStatus.TERMINATED, validAppInstanceToTest.getArtifactInstances().get(0).getHealthStatus());
        }
    }

    @Test
    void givenInstanceWithKafkaResources_WhenUpdateAppInstance_ThenCredentialEvenUpdateAppOnBoardingId() throws IOException, URISyntaxException {
        final long oldAppOnBoardingId = 1;
        final long newAppOnBoardingId = 2;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        ResponseEntity<AppDto> appResponse = new ResponseEntity<>(testApp, HttpStatus.OK);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder()
            .id(1L)
            .workloadInstanceId("000-000")
            .appOnBoardingArtifactId(oldAppOnBoardingId)
            .build();
        final AppInstance actualAppInstance = AppInstance.builder()
            .id(1L)
            .appOnBoardingAppId(oldAppOnBoardingId)
            .targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance))
            .build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.CREATED);
        credentialEvent.setId(1L);
        credentialEvent.setAppOnBoardingAppId(oldAppOnBoardingId);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(appResponse);
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
            .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[] { 1, 2, 3, 4 }), HttpStatus.OK));
        when(helmOrchestratorClient.updateApp(any(), any(), any()))
            .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstanceId"), headers, HttpStatus.ACCEPTED));
        when(artifactInstanceRepository.save(actualArtifactInstance)).thenReturn(actualArtifactInstance);
        when(artifactInstanceRepository.findByAppInstanceIdAndId(anyLong(), anyLong())).thenReturn(java.util.Optional.of(actualArtifactInstance));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(credentialEvent);

        AppInstanceDto actualAppInstanceDto =
            objectUnderTest.updateAppInstance(new AppInstancePutRequestDto().appInstanceId(1L).appOnBoardingAppId(newAppOnBoardingId));

        assertEquals(newAppOnBoardingId, actualAppInstanceDto.getAppOnBoardingAppId());
        assertEquals(newAppOnBoardingId, actualArtifactInstance.getAppOnBoardingArtifactId());
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertEquals(newAppOnBoardingId, credentialEvent.getAppOnBoardingAppId());
    }

    @Test
    void givenEmptyAppInstanceList_WhenDeleteAppInstancesResources_ThenReturnFalse() {
        final List<AppInstance> appInstanceList = new ArrayList<>();

        boolean result = objectUnderTest.deleteAppInstancesResources(1L, appInstanceList);

        assertFalse(result);
    }

    @Test
    void givenValidAppInstanceList_WhenDeleteAppInstancesResources_ThenReturnTrue() {
        final List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(getAppInstance(1L, 2L, 123L, HealthStatus.TERMINATED));

        when(helmOrchestratorClient.deleteWorkloadInstanceId(any()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        boolean result = objectUnderTest.deleteAppInstancesResources(1L, appInstanceList);

        assertTrue(result);
        assertThat(appInstanceList.get(0).getHealthStatus()).isEqualTo(HealthStatus.DELETED);
        assertThat(appInstanceList.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.DELETED);
    }

    @Test
    void givenInValidAppInstanceList_WhenDeleteAppInstancesResources_ThenReturnFalse() {
        final List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(getAppInstance(1L, 2L, 123L, HealthStatus.INSTANTIATED));

        when(helmOrchestratorClient.deleteWorkloadInstanceId(any()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NOT_FOUND));

        boolean result = objectUnderTest.deleteAppInstancesResources(1L, appInstanceList);

        assertFalse(result);
        assertThat(appInstanceList.get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
        assertThat(appInstanceList.get(0).getArtifactInstances().get(0).getHealthStatus()).isEqualTo(HealthStatus.FAILED);
    }

    @Test
    public void givenAppInstanceListWithPendingHealthStatus_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownForNotTerminatedOrFailed(){
        final Long appId = 1L;
        final Long appInstanceId = 1L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.PENDING))));

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode());
        }
    }

    @Test
    public void givenAppInstanceListWithFailedHealthStatus_WhenDeleteAppInstances_ThenFailureToDeleteExceptionThrownIfAny(){
        final Long appId = 1L;
        final Long appInstanceId = 1L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.FAILED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
        .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        try{
            objectUnderTest.deleteAppInstances(appId, new AppInstanceListRequestDto());
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(FAILURE_TO_DELETE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
        }
    }

    @Test
    void givenInstantiatedInstanceAndFailedInstanceAndOneInvalidInstanceIdInList_WhenDeleteAppInstances_ThenPartialDeleteExceptionThrownForNotTerminatedOrFailed(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long failedAppInstanceId = 3L;
        final Long invalidAppInstanceId = 12345L;
        final Long workloadId = 3L;

        when(repository.findAll(any(Specification.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.INSTANTIATED),
            		getAppInstance(appId, failedAppInstanceId, workloadId, HealthStatus.FAILED))));
        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        AppInstanceListRequestDto appInstanceListRequestDto = new AppInstanceListRequestDto();
        appInstanceListRequestDto.addAppInstanceIdItem(appInstanceId);
        appInstanceListRequestDto.addAppInstanceIdItem(failedAppInstanceId);
        appInstanceListRequestDto.addAppInstanceIdItem(invalidAppInstanceId);

        try{
            objectUnderTest.deleteAppInstances(appId, appInstanceListRequestDto);
        }catch (FailureToDeleteException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(AppLcmError.APP_LCM_PARTIAL_DELETE_FAILURE);
            assertThat(e.getAppLcmError().getErrorMessage())
                .isEqualTo(APP_LCM_PARTIAL_DELETE_FAILURE.getErrorMessage());
            assertEquals(1L, e.getTotalSuccessfulDeletion());
            assertThat(e.getErrorData().get(0).getAppInstanceId())
                .isEqualTo(appInstanceId);
            assertThat(e.getErrorData().get(0).getFailureMessage())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage());
            assertThat(e.getErrorData().get(0).getAppLcmErrorCode())
                .isEqualTo(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode());
            assertThat(e.getErrorData().get(1).getFailureMessage())
            .isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage());
            assertThat(e.getErrorData().get(1).getAppLcmErrorCode())
            .isEqualTo(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode());
        }
    }

    @Test
    void givenOneInvalidAppInstanceIdWithAndItsDisabledApp_WhenDeleteAppInstance_ThenThrowFailureToDelete() throws IOException {
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long invalidAppInstanceId = 12345L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");

        when(repository.findAll(any(Specification.class)))
        .thenReturn(new ArrayList<>(Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.TERMINATED))));

        when(appOnboardingClient.getAppById(anyLong()))
            .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));

        when(helmOrchestratorClient.deleteWorkloadInstanceId(workloadId.toString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));

        AppInstanceListRequestDto appInstanceListRequestDto = new AppInstanceListRequestDto();
        appInstanceListRequestDto.addAppInstanceIdItem(invalidAppInstanceId);

        try{
            objectUnderTest.deleteAppInstances(appId,appInstanceListRequestDto);
        }catch (FailureToDeleteException e){
            assertEquals(1L, e.getTotalSuccessfulDeletion());
            assertEquals(false, e.getErrorData().isEmpty());
            assertEquals(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode(), e.getErrorData().get(0).getAppLcmErrorCode());
            assertEquals(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage(), e.getErrorData().get(0).getFailureMessage());
        }
    }

    /**
     *
     * @param appId
     * @param appInstanceId
     * @param workloadId
     * @param healthStatus
     * @return
     */
    private AppInstance getAppInstance(Long appId, Long appInstanceId, Long workloadId,
                                       HealthStatus healthStatus) {
        String workloadStringId = workloadId != null?
            workloadId.toString() : null;
        final ArtifactInstance artifactInstance = ArtifactInstance.builder()
            .id(2L).workloadInstanceId(workloadStringId).build();
        artifactInstance.setHealthStatus(healthStatus);
        final AppInstance appInstance = AppInstance.builder().id(appInstanceId).appOnBoardingAppId(appId).build();
        appInstance.setHealthStatus(healthStatus);
        appInstance.setArtifactInstances(Collections.singletonList(artifactInstance));
        return appInstance;
    }

    /**
     *
     * @param values
     * @param headers
     * @param artifactList
     * @param appOnboardingGetAppResponse
     * @param appOnboardingGetAppArtifactFileResponse
     * @param helmOrchestratorInstantiateAppresponse
     */
    private void setUpContextForCreateMethodTest(final ByteArrayResource values, final HttpHeaders headers, final List<Artifact> artifactList,
                                                 final HttpStatus appOnboardingGetAppResponse,
                                                 final HttpStatus appOnboardingGetAppArtifactFileResponse,
                                                 final HttpStatus helmOrchestratorInstantiateAppresponse) {
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(appOnboardingGetAppResponse));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(values, appOnboardingGetAppArtifactFileResponse));
        when(helmOrchestratorClient.instantiateApp(any())).thenReturn(new ResponseEntity<>(
                new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, helmOrchestratorInstantiateAppresponse));
        when(artifactMapper.mapAsList(anyList(), eq(Artifact.class))).thenReturn(artifactList);
    }

    private Object convertStringToObject(final String file, final Class<?> type) throws IOException {
        final String jsonString = ResourceLoaderUtils.getClasspathResourceAsString(file);
        return new ObjectMapper().readValue(jsonString, type);
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndClientIdNotOK_ThenThrowKeycloakException()
            throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenThrow(new KeycloakException(AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT, "", ""));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(null);
        final KeycloakException actualAppOnBoardingDisabled = assertThrows(KeycloakException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test"))));

        assertThat(actualAppOnBoardingDisabled.getAppLcmError()).isEqualTo(FAILURE_TO_RETRIEVE_ID_FROM_CLIENT);
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndCreateSecretNotOK_ThenThrowKeycloakException()
            throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        when(keycloakClient.createSecret(anyString())).thenThrow(new KeycloakException(FAILURE_TO_CREATE_SECRET, "", ""));
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(null);
        final KeycloakException actualAppOnBoardingDisabled = assertThrows(KeycloakException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test"))));

        assertThat(actualAppOnBoardingDisabled.getAppLcmError()).isEqualTo(FAILURE_TO_CREATE_SECRET);
    }

    @Test
    public void givenAppExistInOnBoardingAsEnabled_WhenTryToInstantiateAndIemFlowRequiredAndCredentialStatusNotEnabledAndClientIdNotFound_ThenThrowResourceNotFoundException()
            throws IOException, URISyntaxException {
        Permission permission =  new Permission();
        Permission permission2 =  new Permission();
        permission2.setResource("kafka");
        permission2.setScope("customScopeTest");

        permission.setResource("mongodb");
        permission.setScope("auth");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        permissionList.add(permission2);
        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtoList = new ClientDto[2];
        clientDtoList[0] = clientDtoOk;
        clientDtoList[1] = clientDtoNotOk;
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        CredentialEvent credentialEvent = new CredentialEvent();
        credentialEvent.setClientId("rAppId-1652438905");
        credentialEvent.setHealthStatus(HealthStatus.PENDING);
        credentialEvent.setId(1L);
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI("http://eric-lcm-helm-executor:8888" + HELM_ORCHESTRATOR_CNWLCM + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES));
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setPermissions(permissionList);
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        when(appOnboardingClient.getAppById(anyLong())).thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.getAppArtifactFile(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{1, 2, 3, 4}), HttpStatus.OK));
        when(helmOrchestratorClient.instantiateApp(any()))
                .thenReturn(new ResponseEntity<>(new WorkloadInstanceDto().workloadInstanceId("workloadInstance"), headers, HttpStatus.ACCEPTED));
        when(keycloakClient.getAuthenticationToken(any(), any())).thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        when(keycloakClient.createClient(any())).thenReturn(new ResponseEntity(HttpStatus.OK));
        when(keycloakClient.getClients()).thenReturn(new ResponseEntity(clientDtoList,HttpStatus.OK));
        when(keycloakClient.createClientId(anyString())).thenReturn("1234");
        when(credentialEventRepository.findByAppOnBoardingAppId(anyLong())).thenReturn(null);
        final KeycloakException actualAppOnBoardingDisabled = assertThrows(KeycloakException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(1L).additionalParameters(Map.of("namespace", "test"))));

        assertThat(actualAppOnBoardingDisabled.getAppLcmError()).isEqualTo(ID_FROM_CLIENT_NOT_FOUND);
    }

    /**
     * App Mode DISABLED
     * Instance Health Status PENDING
     * Expected values: appLcmErrorCode - 1020
     *
     * Negative test case from App STG (Delete an App Instance in PENDING & Instantiated status)
     */
    @Test
    void givenValidAppWithAppInstanceStatusPending_WhenDeleteApp_ThenThrowAppLcmServiceException()
            throws IOException {

        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, appInstanceId, workloadId, HealthStatus.PENDING));

        when(appOnboardingClient.getAppById(anyLong()))
                .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(appInstanceListTest);

        FailureToDeleteException exception = assertThrows(FailureToDeleteException.class,
                () -> objectUnderTest.deleteApp(appId));

        assertThat(exception.getAppLcmError()).isEqualTo(FAILURE_TO_DELETE);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
        assertThat(exception.getAppLcmError().getErrorCode()).isEqualTo(FAILURE_TO_DELETE.getErrorCode());

    }

    /**
     * App Mode ENABLED
     * Expected values: appLcmErrorCode - 1002
     *
     * Negative test case from App STG (Delete API on invalid App ID)
     */
    @Test
    void givenInvalidAppId_WhenDeleteApp_ThenThrowAppOnboardingAppNotExistException() throws IOException {

        final Long appId = 1L;
        final String message = "App with ID " + 1L + " " + "does not exist or hasn't been fully onboarded";

        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("ENABLED");

        when(appOnboardingClient.getAppById(anyLong()))
                .thenThrow(new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, message, APP_INSTANCES_URL));

        AppOnboardingAppNotExistException exception = assertThrows(AppOnboardingAppNotExistException.class,
                () -> objectUnderTest.deleteApp(appId));

        assertThat(exception.getAppLcmError()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND.getErrorMessage());
        assertThat(exception.getAppLcmError().getErrorCode()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND.getErrorCode());

    }

    /**
     * App Mode DISABLED
     * First Instance Health Status INSTANTIATED
     * Second Instance Health Status FAILED
     * Expected values: appLcmErrorCode - 1020
     *
     * Negative test case from App STG (Delete App with 2 Instances in INSTANTIATED and FAILED health status)
     */
    @Test
    void givenValidAppWithAppInstancesStatusInstantiatedAndFailed_WhenDeleteApp_ThenThrowAppLcmServiceException()
            throws IOException {

        final Long appId = 1L;
        final Long firstAppInstanceId = 2L;
        final Long secondAppInstanceId = 3L;
        final Long firstWorkloadId = 3L;
        final Long secondWorkloadId = 4L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        final List<AppInstance> appInstanceListTest = Arrays.asList(getAppInstance(appId, firstAppInstanceId, firstWorkloadId, HealthStatus.INSTANTIATED),
                getAppInstance(appId, secondAppInstanceId, secondWorkloadId, HealthStatus.INSTANTIATED));

        appInstanceListTest.get(1).setHealthStatus(HealthStatus.FAILED);

        when(appOnboardingClient.getAppById(anyLong()))
                .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(appInstanceListTest);

        FailureToDeleteException exception = assertThrows(FailureToDeleteException.class,
                () -> objectUnderTest.deleteApp(appId));

        assertThat(exception.getAppLcmError()).isEqualTo(FAILURE_TO_DELETE);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(FAILURE_TO_DELETE.getErrorMessage());
        assertThat(exception.getAppLcmError().getErrorCode()).isEqualTo(FAILURE_TO_DELETE.getErrorCode());

    }

    /**
     * App Mode DISABLED
     * App Health Status FAILED
     * Expected values: appLcmErrorCode - 1021
     *
     * Negative test case from App STG (Delete a Disabled App with Valid App ID in FAILED health state)
     */
    @Test
    void givenAppWithAppHealthStatusFailed_WhenDeleteApp_ThenThrowAppOnboardDeleteException()
            throws IOException {

        final Long appId = 1L;

        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);
        testApp.setMode("DISABLED");
        testApp.setStatus(HealthStatus.FAILED.toString());

        when(appOnboardingClient.getAppById(anyLong()))
                .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(appOnboardingClient.updateStatusForDeletion(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        AppOnBoardingDeleteException exception = assertThrows(AppOnBoardingDeleteException.class,
                () -> objectUnderTest.deleteApp(appId));

        assertThat(exception.getAppLcmError()).isEqualTo(AppLcmError.APP_ON_BOARDING_UPDATE_DELETING_FAILED);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage());
        assertThat(exception.getAppLcmError().getErrorCode()).isEqualTo(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorCode());

    }

    /**
     * Instance Health Status INSTANTIATED
     * Expected values: appLcmErrorCode - 1020
     *
     * Negative test case from App STG (Delete an App Instance in PENDING & Instantiated status)
     */
    @Test
    void givenValidAppWithAppInstanceStatusInstantiated_WhenInstantiateApp_ThenThrowAppLcmServiceException()
            throws IOException {

        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);

        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(1L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.INSTANTIATED);

        when(appOnboardingClient.getAppById(anyLong()))
                .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findByAppOnBoardingAppId(any()))
                .thenReturn(new ArrayList<>(Arrays.asList(appInstance1)));

        AppLcmException exception = assertThrows(AppLcmException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(appId)));

        assertThat(exception.getAppLcmError()).isEqualTo(BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST.getErrorMessage());
    }

    /**
     * Instance Health Status INSTANTIATED
     * Expected values: appLcmErrorCode - 1020
     *
     * Negative test case from App STG (Delete an App Instance in PENDING & Instantiated status)
     */
    @Test
    void givenValidAppWithAppInstanceStatusFailed_WhenInstantiateApp_ThenThrowAppLcmServiceException()
            throws IOException {

        final Long appId = 1L;
        final AppDto testApp = (AppDto) convertStringToObject("expectedresponses/AppOnboardingAppResponseSuccess.json", AppDto.class);

        final AppInstance appInstance1 = AppInstance.builder().id(1L).appOnBoardingAppId(1L).targetStatus(TargetStatus.INSTANTIATED).build();
        appInstance1.setHealthStatus(HealthStatus.FAILED);

        when(appOnboardingClient.getAppById(anyLong()))
                .thenReturn(new ResponseEntity<>(testApp, HttpStatus.OK));
        when(repository.findByAppOnBoardingAppId(any()))
                .thenReturn(new ArrayList<>(Arrays.asList(appInstance1)));

        AppLcmException exception = assertThrows(AppLcmException.class,
                () -> objectUnderTest.create(new AppInstancePostRequestDto().appId(appId)));

        assertThat(exception.getAppLcmError()).isEqualTo(BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST);
        assertThat(exception.getAppLcmError().getErrorMessage()).isEqualTo(BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST.getErrorMessage());
    }

    private void runKeycloakMocks(){
        when(keycloakClient.createClientId(anyString()))
                .thenReturn("rAppId-1652438905");
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("234230947809");
        when(keycloakClient.createSecret(anyString()))
                .thenReturn(new ResponseEntity(credentialDto,HttpStatus.OK));
        TokenDto tokendto = (TokenDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/TokenDto.json",TokenDto.class);
        when(keycloakClient.getAuthenticationToken(any(), any()))
                .thenReturn(new ResponseEntity(tokendto,HttpStatus.OK));
        ClientRoleDTO clientRoleDTO = new ClientRoleDTO();
        clientRoleDTO.setName("admin");
        clientRoleDTO.setId("123");
        ClientRoleDTO[] clientRoleDTOS = new ClientRoleDTO[1];
        clientRoleDTOS[0] = clientRoleDTO;
        when(keycloakClient.extractRoles())
                .thenReturn(new ResponseEntity<>(clientRoleDTOS, HttpStatus.OK));

        ClientDto clientDtoNotOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonNotOk.json",ClientDto.class);
        ClientDto clientDtoOk = (ClientDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",ClientDto.class);
        clientDtoOk.setId("1L");
        clientDtoOk.setName("customScopeTest");
        ClientDto[] clientDtos = new ClientDto[2];
        clientDtos[0] = clientDtoNotOk;
        clientDtos[1] = clientDtoOk;
        when(keycloakClient.getClients())
                .thenReturn(new ResponseEntity<>(clientDtos, HttpStatus.OK));
        final ServiceAccountDto serviceAccountDto = new ServiceAccountDto();

        serviceAccountDto.setId("serviceId");
        when(keycloakClient.getServiceAccount(anyString()))
                .thenReturn(new ResponseEntity<>(serviceAccountDto, HttpStatus.OK));

        //when(keycloakClient.createClientId(anyString())).thenReturn("rAppId-1652438905");
        when(keycloakClient.associateRoles(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    }

}
