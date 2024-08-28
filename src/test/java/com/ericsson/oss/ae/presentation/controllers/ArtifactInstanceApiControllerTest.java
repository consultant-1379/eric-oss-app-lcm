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

import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Map;

import com.ericsson.oss.ae.clients.helmorchestrator.mapper.EnvironmentHolder;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, ArtifactInstanceController.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArtifactInstanceApiControllerTest {
    private static final String SQL_SCRIPT = "file:src/test/resources/sql/monitoring/monitoring_data.sql";

    @Autowired
    private UrlGenerator urlGenerator;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @SpyBean
    private EnvironmentHolder environmentHolder;

    @SpyBean
    private KeycloakClient keycloakClient;

    @BeforeAll
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterAll
    public void resetMockServer() {
        mockServer.reset();
    }

    @Transactional
    @Test
    public void givenAppInstanceArtifacts_WhenCallGetArtifactsEndPoint_ThenRetrieveArtifacts() throws Exception {
        final HttpHeaders mockResponseHeaders = new HttpHeaders();
        String id="12345";
        mockResponseHeaders.set("Location", "locationHeader/operationId");
        mockServer.expect(requestTo(urlGenerator.generateAppByIdUrl(4L))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/AppOnboardingAppResponseSuccess.json"), MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(urlGenerator.generateAppArtifactFileUrl(4L, 5L))).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
                getClasspathResourceAsString("expectedresponses/AppOnboardingTestHelmSourceFile.txt"), MediaType.APPLICATION_OCTET_STREAM));
        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER), POST, "expectedresponses/keycloak/ClientJsonOk.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);

        mockRequestAndResponse(urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER), GET, "expectedresponses/keycloak/ClientJsonOkArray.json", APPLICATION_JSON);

        mockRequestAndResponse(urlGenerator.generateBearerTokenKeycloakUrl(), POST, "expectedresponses/keycloak/TokenDto.json", APPLICATION_JSON);
        mockRequestAndResponse(urlGenerator.generateSecretKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER,id), POST, "expectedresponses/keycloak/SecretJsonOk.json", APPLICATION_JSON);


        mockServer.expect(requestTo(urlGenerator.generateWorkloadInstancesUrl())).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .body(getClasspathResourceAsString("expectedresponses/HelmOrchestratorWorkloadInstancesResponseSuccess.json"))
                        .headers(mockResponseHeaders).contentType(MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(urlGenerator.generateWorkloadInstanceOperationsByIdUrl("d9920094-8a60-413d-a975-c97d36a3246d")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/HelmOrchestratorOperationsResponseCompleted.json"),
                        MediaType.APPLICATION_JSON));

        when(keycloakClient.createClientId("4")).thenReturn("rapp_4_12345");
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");

        mvc.perform(post(urlGenerator.generateAppInstancesUrl())
                .content(objectMapper.writeValueAsString(new AppInstancePostRequestDto().appId(4L).additionalParameters(Map.of("namespace", "test"))))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
        mvc.perform(get(urlGenerator.generateArtifactInstancesByAppIdUrl(1L)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void givenNoArtifacts_WhenCallGetArtifactsEndPoint_ThenRetrieveError() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstancesByAppIdUrl(2L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithIdOne() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(2L, 1L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithHealthStratusPending() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(11L, 18L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artifactInstanceId").value("18")).andExpect(jsonPath("$.healthStatus").value(HealthStatus.PENDING.toString()))
                .andExpect(jsonPath("$.statusMessage").value(IsNull.nullValue()));
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithHealthStratusFailed() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(11L, 19L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artifactInstanceId").value("19")).andExpect(jsonPath("$.healthStatus").value(HealthStatus.FAILED.toString()))
                .andExpect(jsonPath("$.statusMessage").value("Error:helloworld app already deployed"));
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithHealthStratusInstantiated() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(11L, 20L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artifactInstanceId").value("20"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.INSTANTIATED.toString()))
                .andExpect(jsonPath("$.statusMessage").value(IsNull.nullValue()));
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithHealthStratusTerminated() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(11L, 21L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artifactInstanceId").value("21"))
                .andExpect(jsonPath("$.healthStatus").value(HealthStatus.TERMINATED.toString()))
                .andExpect(jsonPath("$.statusMessage").value(IsNull.nullValue()));
    }

    @Transactional
    @Test
    @Sql(scripts = { SQL_SCRIPT })
    public void givenAppInstanceArtifact_WhenCallGetArtifactByIdEndPoint_ThenRetrieveArtifactWithHealthStratusDeleted() throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(11L, 22L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artifactInstanceId").value("22")).andExpect(jsonPath("$.healthStatus").value(HealthStatus.DELETED.toString()))
                .andExpect(jsonPath("$.statusMessage").value(IsNull.nullValue()));
    }

    @Test
    public void givenRepositoryContainsNoArtifactInstances_WhenCallGetArtifactInstancesByAppInstanceId_ThenThrowResourceNotFoundException()
            throws Exception {
        mvc.perform(get(urlGenerator.generateArtifactInstancesByAppIdUrl(1L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private void mockRequestAndResponse(final String url, final HttpMethod requestMethod, final String expectedResponse, final MediaType mediaType)
            throws IOException {
        mockServer.expect(requestTo(url)).andExpect(method(requestMethod))
                .andRespond(withSuccess(getClasspathResourceAsString(expectedResponse), mediaType));
    }
}