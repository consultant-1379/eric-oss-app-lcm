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

package com.ericsson.oss.ae.presentation.controllers;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.EnvironmentHolder;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.presentation.exceptions.UnableToRetrieveArtifactException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_URL;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, AppInstanceController.class })
public class AppInstanceApiControllerTest {
    @Autowired
    private UrlGenerator urlGenerator;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mvc;
    @Autowired
    private RestTemplate restTemplate;
    @MockBean
    private AppInstanceRepository repository;
    @MockBean
    private ArtifactInstanceRepository artifactInstanceRepository;
    private MockRestServiceServer mockServer;
    @SpyBean
    private EnvironmentHolder  environmentHolder;

    @SpyBean
    private KeycloakClient keycloakClient;

    @BeforeAll
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
    }

    @Test
    public void givenAppAndArtifactsExistInOnboarding_WhenCallPostAppInstanceEndpoint_ThenReturnHttpStatus201() throws Exception {
        final HttpHeaders mockResponseHeaders = new HttpHeaders();
        String id = "12345";
        mockResponseHeaders.set("Location", "locationHeader/operationId");

        mockRequestAndResponse(urlGenerator.generateAppByIdUrl(4L), GET, "expectedresponses/AppOnboardingAppResponseSuccess.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateAppArtifactFileUrl(4L, 5L), GET, "expectedresponses/AppOnboardingTestHelmSourceFile.txt",
                APPLICATION_OCTET_STREAM);

        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER), POST, "expectedresponses/keycloak/ClientJsonOk.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);

        mockRequestAndResponse(urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER), GET, "expectedresponses/keycloak/ClientJsonOkArray.json", APPLICATION_JSON);

        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateSecretKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER,id), POST, "expectedresponses/keycloak/SecretJsonOk.json", APPLICATION_JSON);
        mockRequestAndResponseWithHeaders(urlGenerator.generateWorkloadInstancesUrl(), POST,
                "expectedresponses/HelmOrchestratorWorkloadInstancesResponseSuccess.json", mockResponseHeaders, APPLICATION_JSON);

        when(keycloakClient.createClientId("4")).thenReturn("rapp_4_12345");
        when(environmentHolder.getNamespaceEnv()).thenReturn("test");
        mvc.perform(post(APP_INSTANCES_URL).content(objectMapper.writeValueAsString(new AppInstancePostRequestDto().appId(4L)))

                .contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.appOnBoardingAppId").value("4"))
                .andExpect(jsonPath("$.healthStatus").value("PENDING")).andExpect(jsonPath("$.targetStatus").value("INSTANTIATED"));
    }
    @Test
    public void givenAppDoesNotExistInAppOnboarding_WhenCallPostAppInstanceEndpoint_ThenReturnHTTPError400() throws Exception {
        mockServer.expect(requestTo(urlGenerator.generateAppByIdUrl(123L))).andExpect(method(GET)).andRespond(withStatus(HttpStatus.BAD_REQUEST));
        mvc.perform(post(APP_INSTANCES_URL).content(objectMapper.writeValueAsString(new AppInstancePostRequestDto().appId(123L)))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void givenArtifactsDoNotExistInApp_WhenCallPostAppInstanceEndpoint_ThenReturnUnableToRetrieveArtifactException() throws Exception {
        mockServer.expect(requestTo(urlGenerator.generateAppByIdUrl(123L))).andExpect(method(GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/AppOnboardingAppWithoutArtifactsResponse.json"), APPLICATION_JSON));

        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);
        mockServer.expect(requestTo(urlGenerator.generateArtifactsByAppIdUrl(123L))).andExpect(method(GET)).andRespond(withBadRequest());
        mvc.perform(post(APP_INSTANCES_URL).content(objectMapper.writeValueAsString(new AppInstancePostRequestDto().appId(123L)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnableToRetrieveArtifactException))
                .andExpect(result -> assertEquals(AppLcmError.FAILURE_TO_RETRIEVE_ARTIFACT,
                        ((UnableToRetrieveArtifactException) Objects.requireNonNull(result.getResolvedException())).getAppLcmError()))
                .andExpect(result -> assertEquals("Artifact of type HELM could not be found", result.getResolvedException().getMessage()));
    }

    @Test
    public void givenArtifactFileCannotBeRetrieved_WhenCallPostAppInstanceEndpoint_ThenReturnUnableToRetrieveArtifactException() throws Exception {
        mockServer.expect(requestTo(urlGenerator.generateAppByIdUrl(4L))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/AppOnboardingAppResponseSuccess.json"), MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(urlGenerator.generateAppArtifactFileUrl(4L, 5L))).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
                getClasspathResourceAsString("expectedresponses/AppOnboardingBadHelmSourceFile.txt"), MediaType.APPLICATION_OCTET_STREAM));
        mvc.perform(post(APP_INSTANCES_URL).content(objectMapper.writeValueAsString(new AppInstancePostRequestDto().appId(4L)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnableToRetrieveArtifactException))
                .andExpect(result -> assertEquals(AppLcmError.FAILURE_TO_RETRIEVE_ARTIFACT,
                        ((UnableToRetrieveArtifactException) Objects.requireNonNull(result.getResolvedException())).getAppLcmError()))
                .andExpect(result -> assertEquals("Artifact File is Empty", result.getResolvedException().getMessage()));

    }

    @Test
    public void givenAppAndArtifactsExistInOnboarding_WhenCallUpdateAppInstanceEndpoint_ThenReturnHttpStatus200() throws Exception {
        final Long appId = 4L;
        final Long artifactId = 2L;
        final HttpHeaders mockResponseHeaders = new HttpHeaders();
        mockResponseHeaders.set("Location", "locationHeader/operationId");
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(artifactId).workloadInstanceId("1c880f7f-00fb-46be-b703"
                + "-3bc69b8fcc08")
            .appInstance(AppInstance.builder().id(4L).appOnBoardingAppId(4L).build()).build();
        actualArtifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        final AppInstance actualAppInstance = AppInstance.builder().appOnBoardingAppId(2L).id(1L).targetStatus(TargetStatus.INSTANTIATED)
            .artifactInstances(List.of(actualArtifactInstance)).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(appId)).thenReturn(Optional.of(actualAppInstance));
        when(artifactInstanceRepository.findByAppInstanceIdAndId(anyLong(), anyLong())).thenReturn(java.util.Optional.of(actualArtifactInstance));

        mockRequestAndResponse(urlGenerator.generateAppByIdUrl(5L), GET, "expectedresponses/AppOnboardingAppResponseSuccess.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateAppByIdUrl(2L), GET, "expectedresponses/AppOnboardingAppResponseSuccess2.json", APPLICATION_JSON);
        mockServer.expect(requestTo(urlGenerator.generateAppArtifactFileUrl(appId, 5L))).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
            getClasspathResourceAsString("expectedresponses/AppOnboardingTestHelmSourceFile.txt"), MediaType.APPLICATION_OCTET_STREAM));
        mockRequestAndResponseWithHeaders(urlGenerator.generateWorkloadInstanceByIdUrl("1c880f7f-00fb-46be-b703-3bc69b8fcc08"), PUT,
            "expectedresponses/HelmOrchestratorWorkloadInstancesResponseSuccess.json", mockResponseHeaders, APPLICATION_JSON);
        when(environmentHolder.getNamespaceEnv()).thenReturn("test");
        mvc.perform(put(APP_INSTANCES_URL)
                .content(objectMapper.writeValueAsString(new AppInstancePutRequestDto().appInstanceId(4L).appOnBoardingAppId(5L)))
                .contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.healthStatus").value("PENDING")).andExpect(jsonPath("$.targetStatus").value("INSTANTIATED"));
    }

    @Test
    public void givenAppDoesNotExistInRepository_WhenCallUpdateAppInstanceEndpoint_ThenAResourceNotFoundExceptionIsThrown() throws Exception {
        mvc.perform(put(APP_INSTANCES_URL)
                .content(objectMapper.writeValueAsString(new AppInstancePutRequestDto().appInstanceId(4L).appOnBoardingAppId(2L)))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
            .andExpect(result -> assertEquals(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND,
                ((ResourceNotFoundException) Objects.requireNonNull(result.getResolvedException())).getAppLcmError()));
    }

    @Test
    public void givenAppInstanceExistsInRepository_WhenCallGetAppInstanceByIdEndpoint_ThenReturnHTTPStatus200() throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(APP_INSTANCES_URL + "/1").contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void givenAppInstanceExistsInRepositoryWithHealthStatusFailed_WhenCallTerminateByIdEndpoint_ThenRetrieveHTTPStatus204()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.FAILED);

        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("1").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.FAILED);

        actualAppInstance.setArtifactInstances(Collections.singletonList(actualArtifactInstance));
        final HttpHeaders mockResponseHeaders = new HttpHeaders();
        mockResponseHeaders.set("Location", "locationHeader/operationId");

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(1L))
                .thenReturn(Optional.of(actualAppInstance.getArtifactInstances()));

        mockServer.expect(requestTo(urlGenerator.generateWorkloadInstanceOperationsByIdUrl("1"))).andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED).headers(mockResponseHeaders));

        mvc.perform(put(APP_INSTANCES_URL + "/1")).andExpect(status().isNoContent());

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }

    @Test
    public void givenAppInstanceDoesNotExistInRepository_WhenCallTerminateByIdEndpoint_ThenReturnHTTPError404AndVerifyResourceNotFoundExceptionIsThrown()
            throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        mvc.perform(put(APP_INSTANCES_URL + "/1")).andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
                .andExpect(result -> assertEquals(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND,
                        ((ResourceNotFoundException) Objects.requireNonNull(result.getResolvedException())).getAppLcmError()));
    }

    @Test
    public void givenAppInstanceExistsInRepositoryWithHealthStatusInstantiated_WhenCallTerminateByIdEndpoint_ThenRetrieveHTTPStatus204()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(15L).appOnBoardingAppId(15L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        final ArtifactInstance actualArtifactInstance = ArtifactInstance.builder().id(2L).workloadInstanceId("1").build();
        actualArtifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        actualAppInstance.setArtifactInstances(Collections.singletonList(actualArtifactInstance));
        final HttpHeaders mockResponseHeaders = new HttpHeaders();
        mockResponseHeaders.set("Location", "locationHeader/operationId");

        when(repository.findById(1L)).thenReturn(Optional.of(actualAppInstance));
        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(1L))
                .thenReturn(Optional.of(actualAppInstance.getArtifactInstances()));

        mockServer.expect(requestTo(urlGenerator.generateWorkloadInstanceOperationsByIdUrl("1"))).andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED).headers(mockResponseHeaders));

        mvc.perform(put(APP_INSTANCES_URL + "/1")).andExpect(status().isNoContent());

        assertEquals(HealthStatus.PENDING, actualAppInstance.getHealthStatus());
        assertEquals(TargetStatus.TERMINATED, actualAppInstance.getTargetStatus());
        assertEquals(HealthStatus.PENDING, actualAppInstance.getArtifactInstances().get(0).getHealthStatus());
        assertEquals("operationId", actualAppInstance.getArtifactInstances().get(0).getOperationId());
    }

    @Test
    public void givenAppInstancesExistInRepository_WhenCallGetAllAppInstancesEndpoint_ThenReturnHTTPStatus200() throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(Collections.singletonList(actualAppInstance));
        mvc.perform(get(APP_INSTANCES_URL).contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void givenAppInstanceInPendingStateExistsInRepository_WhenCallGetAppInstanceByIdEndpoint_ThenReturnAppInstanceWithHealthStatusPending()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(11L).appOnBoardingAppId(11L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.PENDING);

        when(repository.findById(11L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(APP_INSTANCES_URL + "/11").contentType(APPLICATION_JSON)).andExpect(jsonPath("$.id").value("11"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.PENDING.toString()))
                .andExpect(jsonPath("$.targetStatus").value(TargetStatus.INSTANTIATED.toString()));
    }

    @Test
    public void givenHealthyAppInstanceExistsInRepository_WhenCallGetAppInstanceByIdEndpoint_ThenRetrieveAppInstanceWithHealthStatusInstantiated()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(12L).appOnBoardingAppId(12L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(12L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(urlGenerator.generateAppInstanceByIdUrl(12L)).contentType(APPLICATION_JSON)).andExpect(jsonPath("$.id").value("12"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.INSTANTIATED.toString()))
                .andExpect(jsonPath("$.targetStatus").value(TargetStatus.INSTANTIATED.toString()));
    }

    @Test
    public void givenAFailedAppInstanceExistsInRepository_WhenCallGetAppInstanceByIdEndpoint_ThenRetrieveAppInstanceWithHealthStatusFailed()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(13L).appOnBoardingAppId(13L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.FAILED);

        when(repository.findById(13L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(urlGenerator.generateAppInstanceByIdUrl(13L)).contentType(APPLICATION_JSON)).andExpect(jsonPath("$.id").value("13"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.FAILED.toString()))
                .andExpect(jsonPath("$.targetStatus").value(HealthStatus.INSTANTIATED.toString()));

    }

    @Test
    public void givenTerminatedAppInstanceExistsInRepository_WhenCallGetAppInstanceByIdEndpoint_ThenRetrieveAppInstanceWithHealthStatusTerminated()
            throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(14L).appOnBoardingAppId(14L).targetStatus(TargetStatus.TERMINATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.TERMINATED);

        when(repository.findById(14L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(urlGenerator.generateAppInstanceByIdUrl(14L)).contentType(APPLICATION_JSON)).andExpect(jsonPath("$.id").value("14"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.TERMINATED.toString()))
                .andExpect(jsonPath("$.targetStatus").value(TargetStatus.TERMINATED.toString()));
    }

    @Test
    public void givenAppInstanceExistsInRepository_WhenCallAppInstanceByIdEndpoint_ThenRetrieveAppInstanceWithHealthStatusDeleted() throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(15L).appOnBoardingAppId(15L).targetStatus(TargetStatus.DELETED).build();
        actualAppInstance.setHealthStatus(HealthStatus.DELETED);

        when(repository.findById(15L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(urlGenerator.generateAppInstanceByIdUrl(15L)).contentType(APPLICATION_JSON)).andExpect(jsonPath("$.id").value("15"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.DELETED.toString()))
                .andExpect(jsonPath("$.targetStatus").value(TargetStatus.DELETED.toString()));
    }

    @Test
    public void givenRepositoryContainsNoAppInstances_WhenCallGetAllAppInstancesEndpoint_ThenThrowResourceNotFoundException() throws Exception {
        mvc.perform(get(urlGenerator.generateAppInstancesUrl()).contentType(APPLICATION_JSON)).andExpect(status().is4xxClientError());
    }

    private void mockRequestAndResponse(final String url, final HttpMethod requestMethod, final String expectedResponse, final MediaType mediaType)
            throws IOException {
        mockServer.expect(requestTo(url)).andExpect(method(requestMethod))
                .andRespond(withSuccess(getClasspathResourceAsString(expectedResponse), mediaType));
    }

    private void mockRequestAndResponseWithHeaders(final String url, final HttpMethod requestMethod, final String expectedResponse,
                                                   final HttpHeaders headers, final MediaType mediaType)
            throws IOException {
        mockServer.expect(requestTo(url)).andExpect(method(requestMethod)).andRespond(
                withStatus(HttpStatus.ACCEPTED).body(getClasspathResourceAsString(expectedResponse)).headers(headers).contentType(mediaType));
    }
}
