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

package com.ericsson.oss.ae.acm.presentation.controllers;

import static com.ericsson.oss.ae.acm.TestConstants.ACM_COMPOSITION_INSTANCE_ID_DEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.ADMIN;
import static com.ericsson.oss.ae.acm.TestConstants.APP_COMPONENT_INSTANCE_ID_1_DEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.APP_COMPONENT_INSTANCE_ID_1_UNDEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.APP_COMPONENT_INSTANCE_ID_2_DEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.DELETE_AUTOMATION_COMPOSITION_INSTANCE_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.DEPLOY_ACTION;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_APP;
import static com.ericsson.oss.ae.acm.TestConstants.NAMESPACE_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.NAMESPACE_VALUE;
import static com.ericsson.oss.ae.acm.TestConstants.REPLICA_COUNT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.USER;
import static com.ericsson.oss.ae.acm.TestConstants.USER_DEFINED_HELM_PARAMETERS;
import static com.ericsson.oss.ae.acm.TestUtils.generateAppInstanceEntity;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ACM_INSTANCE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APPS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ID;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ID_PARENTHESIS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCES;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCES_COMPONENT_INSTANCE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCES_INSTANCE_ID;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.INSTANCE_ID;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.MICROSERVICE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.NAMESPACE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_INSTANCE_ENTITY_NOT_FOUND;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEPLOY_APP_INSTANCE_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_TOKEN_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UPDATE_APP_INSTANCE_ERROR;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spock.lang.Specification;

import com.ericsson.coverage.filter.ContractCoverageFilterV3;
import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.acm.core.services.AppInstancesServiceImpl;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.entity.Role;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentInstanceRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.persistence.repository.ClientCredentialRepository;
import com.ericsson.oss.ae.acm.presentation.controller.AppInstancesController;
import com.ericsson.oss.ae.acm.presentation.mapper.LcmUrlGenerator;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceItems;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AppLcmApplication.class, AppInstancesController.class})
@EnableRetry(proxyTargetClass = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class AppInstancesControllerTest extends Specification{

    private static final String ERROR_DURING_DELETING_AUTOMATION_COMPOSITION_INSTANCE_IN_ACM_R = "Error during deleting Automation Composition Instance in ACM-R";
    private static final String STATUS_CANNOT_BE_DEPLOYED_FOR_DELETE_OPERATION_MUST_BE_UNDEPLOYED_OR_DELETE_ERROR = "Status cannot be DEPLOYED for DELETE operation - must be UNDEPLOYED or DELETE_ERROR";

    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    // Create a UUID for the AppInstance
    private final UUID appInstanceId = UUID.randomUUID();

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private KeycloakUrlGenerator mockKeycloakUrlGenerator;

    @Autowired
    private AcmUrlGenerator acmUrlGenerator;

    @Autowired
    private LcmUrlGenerator lcmUrlGenerator;

    @Autowired
    private AppInstancesRepository appInstancesRepository;

    @Autowired
    private AppComponentInstanceRepository appComponentInstanceRepository;

    @Autowired
    private AppComponentRepository appComponentRepository;

    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;

    @Autowired
    private ClientCredentialRepository clientCredentialRepository;

    @Autowired
    private LcmUrlGenerator mockLcmUrlGenerator;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(new ContractCoverageFilterV3()).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
        appRepository.deleteAll();
        appInstancesRepository.deleteAll();
        appInstanceEventRepository.deleteAll();
        clientCredentialRepository.deleteAll();
        appInstancesRepository.deleteAll();
        appComponentInstanceRepository.deleteAll();
    }

    @Test
    public void createAppInstance_return_http_status_created_failure_5xx_error() throws Throwable {
        // Given an App is already created in LCM
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        createAppInstanceRequest.setAppId(app.getId().toString());
        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError());

        mvc.perform(post(APP_INSTANCES)
                        .content(new ObjectMapper().writeValueAsString(createAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(KEYCLOAK_RETRIEVE_TOKEN_ERROR.getErrorTitle()));
    }

    @Test
    public void createAppInstance_return_http_status_503_retry_exhausted() throws Throwable {
        // Given an App is already created in LCM
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        createAppInstanceRequest.setAppId(app.getId().toString());
        mockServer.expect(times(3), requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST))
                .andRespond(
                        withServiceUnavailable());
        mvc.perform(post(APP_INSTANCES)
                        .content(new ObjectMapper().writeValueAsString(createAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(KEYCLOAK_RETRIEVE_TOKEN_ERROR.getErrorTitle()));
    }

    @Test
    public void createAppInstance_return_http_status_created_failure_4xx_error() throws Throwable {
        // Given an App is already created in LCM
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        createAppInstanceRequest.setAppId(app.getId().toString());
        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withBadRequest());

        mvc.perform(post(APP_INSTANCES)
                        .content(new ObjectMapper().writeValueAsString(createAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(KEYCLOAK_RETRIEVE_TOKEN_ERROR.getErrorTitle()));
    }

    @Test
    public void getAllAppInstances_return_http_status_ok() throws Exception {
        // Given
        final List<AppInstances> appInstances = createAndSaveMultipleAppInstanceEntities();

        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstances.get(0).getApp().getCompositionId(), appInstances.get(0).getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        final String url2 = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstances.get(1).getApp().getCompositionId(), appInstances.get(1).getCompositionInstanceId());
        mockServer.expect(requestTo(url2)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        // When
        final String appInstanceResponse = mvc.perform(get(APP_INSTANCES))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString();
        final List<AppInstance> appInstanceList = getAppInstancesFromResponseString(appInstanceResponse);

        // Then
        assertThat(appInstanceList.isEmpty()).isFalse();
        // Expected data based on test setup
        assertThat(appInstanceList.size()).isEqualTo(2);

        // Check that multiple app instances can be returned in the response
        final AppInstance appInstanceDeployed = appInstanceList.get(0);
        final AppInstance appInstanceUndeployed = appInstanceList.get(1);

        // Check component instance data for undeployed instance
        assertThat(appInstanceUndeployed.getComponentInstances().size()).isEqualTo(1);
        assertThat(appInstanceUndeployed.getEvents().size()).isEqualTo(1);
        final ComponentInstances appComponentInstance = appInstanceUndeployed.getComponentInstances().get(0);
        assertThat(appComponentInstance.getDeployState()).isEqualTo(DeployState.UNDEPLOYED.name());
        assertThat(appComponentInstance.getName().isEmpty()).isFalse();
        assertThat(appComponentInstance.getMessage().isEmpty()).isFalse();
        assertThat(appComponentInstance.getVersion().isEmpty()).isFalse();
        assertThat(appComponentInstance.getType()).isEqualTo(MICROSERVICE);
        assertThat(appComponentInstance.getDeployState()).isEqualTo(DeployState.UNDEPLOYED.name());

        // Check when multiple component instances, that the expected number are returned
        assertThat(appInstanceDeployed.getComponentInstances().size()).isEqualTo(2);
    }

    @Test
    public void getAppInstanceById_return_http_status_ok() throws Exception {
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity();

        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(get(APP_INSTANCES_INSTANCE_ID,  appInstance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.appId").isNotEmpty())
            .andExpect(jsonPath("$.status").isNotEmpty())
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andExpect(jsonPath("$.credentials").isNotEmpty())
            .andExpect(jsonPath("$.componentInstances").isNotEmpty())
            .andExpect(jsonPath("$.events").isNotEmpty())
            .andExpect(jsonPath("$.self").isNotEmpty())
            .andExpect(jsonPath("$.app").isNotEmpty());
    }

    @Test
    public void getAppInstanceByIdWithDataManagementComponent_return_http_status_ok() throws Exception {
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntityWithASDAndDataManagementAppComponents();

        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_ASDAndDataManagement_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(get(APP_INSTANCES_INSTANCE_ID,  appInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.appId").isNotEmpty())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.credentials").isNotEmpty())
                .andExpect(jsonPath("$.componentInstances").isNotEmpty())
                .andExpect(jsonPath("$.componentInstances[1].properties.artifacts[*].name").isNotEmpty())
                .andExpect(jsonPath("$.componentInstances[1].properties.artifacts[*].bucketName").doesNotExist())
                .andExpect(jsonPath("$.componentInstances[1].properties.artifacts[*].objectName").doesNotExist())
                .andExpect(jsonPath("$.componentInstances[1].properties.artifacts[*].type").doesNotExist())
                .andExpect(jsonPath("$.self").isNotEmpty())
                .andExpect(jsonPath("$.app").isNotEmpty());
    }

    @Test
    public void getAppInstancesForAppId_return_http_status_ok() throws Exception {
        // Given
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity();
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        // When & Then
        final String appInstanceResponse = mvc.perform(get(APP_INSTANCES)
                .queryParam(APP_ID, String.valueOf(appInstance.getApp().getId())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        final List<AppInstance> appInstanceList = getAppInstancesFromResponseString(appInstanceResponse);
        // Then
        assertThat(appInstanceList.isEmpty()).isFalse();
        // Expected data based on test setup
        assertThat(appInstanceList.size()).isEqualTo(1);
        // Check if namespace present
        String properties = appInstanceList.get(0).getComponentInstances().get(0).getProperties().toString();
        assertThat(properties).contains(NAMESPACE_KEY);
        assertThat(properties).contains(NAMESPACE_VALUE);
    }

    @Test
    public void getAppInstances_expected_element_missing_return_http_status_server_error() throws Exception {
        // Given
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity();
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_ExpectedElementsMissing.json"), MediaType.APPLICATION_JSON));

        // When & Then
        mvc.perform(get(APP_INSTANCES)
                .queryParam(APP_ID, String.valueOf(appInstance.getApp().getId())))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void getAppInstances_state_deleting_and_expected_element_missing_return_http_status_server_error() throws Exception {
        // Given
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity(AppInstanceStatus.DELETING);
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_ExpectedElementsMissing.json"), MediaType.APPLICATION_JSON));

        // When & Then
        final String appInstanceResponse = mvc.perform(get(APP_INSTANCES)
                .queryParam(APP_ID, String.valueOf(appInstance.getApp().getId())))
            .andReturn().getResponse().getContentAsString();

        final List<AppInstance> appInstanceList = getAppInstancesFromResponseString(appInstanceResponse);

        assertThat(appInstanceList.isEmpty()).isFalse();
        // Expected data based on test setup
        assertThat(appInstanceList.size()).isEqualTo(1);

        // Check that multiple app instances can be returned in the response
        final AppInstance appInstanceDeleting = appInstanceList.get(0);

        // Check component instance data for undeployed instance
        assertThat(appInstanceDeleting.getComponentInstances().size()).isEqualTo(1);
        assertThat(appInstanceDeleting.getEvents().size()).isEqualTo(1);
        assertThat(appInstanceDeleting.getStatus()).isEqualTo(AppInstanceStatus.DELETING);
        final ComponentInstances appComponentInstance = appInstanceDeleting.getComponentInstances().get(0);

        // LCM Properties
        assertThat(appComponentInstance.getName().isEmpty()).isFalse();
        assertThat(appComponentInstance.getVersion().isEmpty()).isFalse();
        assertThat(appComponentInstance.getType()).isEqualTo(MICROSERVICE);

        // ACM properties should not be included in the response since it is in state DELETING
        assertNull(appComponentInstance.getMessage());
        assertNull(appComponentInstance.getDeployState());
        assertNull(appComponentInstance.getDescription());
        assertNull(appComponentInstance.getProperties());
    }

    @Test
    public void getAppInstances_state_deleting_and_acm_returns_composition_instance_not_found() throws Exception {
        // Given
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity(AppInstanceStatus.DELETING);
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withResourceNotFound());

        // When & Then
        final String appInstanceResponse = mvc.perform(get(APP_INSTANCES)
                .queryParam(APP_ID, String.valueOf(appInstance.getApp().getId())))
            .andReturn().getResponse().getContentAsString();

        final List<AppInstance> appInstanceList = getAppInstancesFromResponseString(appInstanceResponse);

        assertThat(appInstanceList.isEmpty()).isFalse();
        // Expected data based on test setup
        assertThat(appInstanceList.size()).isEqualTo(1);

        // Check that multiple app instances can be returned in the response
        final AppInstance appInstanceDeleting = appInstanceList.get(0);

        // Check component instance data for undeployed instance
        assertThat(appInstanceDeleting.getComponentInstances().size()).isEqualTo(1);
        assertThat(appInstanceDeleting.getEvents().size()).isEqualTo(1);
        assertThat(appInstanceDeleting.getStatus()).isEqualTo(AppInstanceStatus.DELETING);
        final ComponentInstances appComponentInstance = appInstanceDeleting.getComponentInstances().get(0);

        // LCM Properties
        assertThat(appComponentInstance.getName().isEmpty()).isFalse();
        assertThat(appComponentInstance.getVersion().isEmpty()).isFalse();
        assertThat(appComponentInstance.getType()).isEqualTo(MICROSERVICE);

        // ACM properties should not be included in the response since it is in state DELETING
        assertNull(appComponentInstance.getMessage());
        assertNull(appComponentInstance.getDeployState());
        assertNull(appComponentInstance.getDescription());
        assertNull(appComponentInstance.getProperties());
    }

    @Test
    public void getAppInstances_invalid_queryParameter_return_http_status_bad_request() throws Exception {
        mvc.perform(get(APP_INSTANCES).queryParam(APP_ID, "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAppInstances_unsupported_queryParameter_ignored_return_http_status_ok() throws Exception {
        final AppInstances appInstance = createAndSaveSingleAppInstanceEntity();

        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(get(APP_INSTANCES).queryParam("unsupported", String.valueOf(UUID.randomUUID())))
            .andExpect(status().isOk());
    }

    @Test
    public void getAppInstances_invalid_queryParameter_return_http_status_not_found() throws Exception {
        final UUID appId = UUID.randomUUID();
        mvc.perform(get(APP_INSTANCES)
                        .queryParam(APP_ID_PARENTHESIS, String.valueOf(appId)))
                .andExpect(content().contentType(APPLICATION_JSON));

        mvc.perform(get(APPS + SLASH + APP_ID_PARENTHESIS, appId))
                .andExpect(status().isNotFound());

    }

    @Test
    public void getAppInstance_invalid_instanceId_return_http_status_bad_request() throws Exception {
        mvc.perform(get(APP_INSTANCES_INSTANCE_ID, RandomStringUtils.random(10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAppInstanceUndeployed_return_http_status_accepted() throws Exception {

        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);
        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));

        AppInstancesServiceImpl appInstancesService = Mockito.mock(AppInstancesServiceImpl.class);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstance appInstance = new AppInstance().appId(appInstancesUnderTest.getId().toString()).status(appInstancesUnderTest.getStatus());

        final AppInstances appInstances = appInstancesRepository.save(appInstancesUnderTest);

        AppComponentInstance appComponentInstance = appInstances.getAppComponentInstances().get(0);
        appComponentInstance.setCompositionElementInstanceId(UUID.fromString("951b4e6a-d9dd-4df1-a8d8-f76e1862f492"));
        appComponentInstanceRepository.save(appComponentInstance);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(times(3), anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        Mockito.doReturn(appInstance).when(appInstancesService).getAppInstanceById(appInstancesUnderTest.getId().toString());

        mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstancesUnderTest.getId())
                        .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void updateAppInstanceDeployed_return_http_status_accepted() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));

        AppInstancesServiceImpl appInstancesService = Mockito.mock(AppInstancesServiceImpl.class);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstance appInstance = new AppInstance().appId(appInstancesUnderTest.getId().toString()).status(appInstancesUnderTest.getStatus());

        final AppInstances appInstances = appInstancesRepository.save(appInstancesUnderTest);

        AppComponentInstance appComponentInstance = appInstances.getAppComponentInstances().get(0);
        appComponentInstance.setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));
        appComponentInstanceRepository.save(appComponentInstance);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(times(3), anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        Mockito.doReturn(appInstance).when(appInstancesService).getAppInstanceById(appInstancesUnderTest.getId().toString());

        mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstancesUnderTest.getId())
                .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    @Transactional
    public void updateDataManagement_should_ignore_properties_and_return_accepted() throws Exception {
        final App app = getAppWithMultipleComponents();
        final AppInstances appInstancesUnderTest = getAppInstance(app);

        AppComponentInstance asdAppComponentInstance = appInstancesUnderTest.getAppComponentInstances().get(0);
        AppComponentInstance otherAppComponentInstance = appInstancesUnderTest.getAppComponentInstances().get(1);
        asdAppComponentInstance.setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));
        otherAppComponentInstance.setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c24"));
        appComponentInstanceRepository.save(asdAppComponentInstance);
        appComponentInstanceRepository.save(otherAppComponentInstance);

        final ComponentInstances dmComponentInstance = TestUtils.createDataManagementComponentInstanceWithInvalidPropertiesForUpdate(app, AppInstanceStatus.DEPLOYED);
        final ComponentInstances asdComponentInstance = TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED);
        final List<ComponentInstances> componentInstances = new ArrayList<>();
        componentInstances.add(asdComponentInstance);
        componentInstances.add(dmComponentInstance);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest()
            .componentInstances(componentInstances);

        // set up mocks
        AppInstancesServiceImpl appInstancesService = Mockito.mock(AppInstancesServiceImpl.class);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(times(3), anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        final AppInstance appInstance = new AppInstance().appId(appInstancesUnderTest.getId().toString()).status(appInstancesUnderTest.getStatus());
        Mockito.doReturn(appInstance).when(appInstancesService).getAppInstanceById(appInstancesUnderTest.getId().toString());

        mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstancesUnderTest.getId())
                .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    public void updateAppInstance_invalid_state() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.UNDEPLOYING);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_UNDEPLOYING.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_UNDEPLOYING.json"), MediaType.APPLICATION_JSON));

        mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstance.getId())
                .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAppInstance_invalid_request_return_http_status_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        updateAppInstanceRequest.getComponentInstances().get(0).setName("INVALID");

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateAppInstance_acmr_return_http_status_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYING);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.POST)).andRespond(withServerError());

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        MvcResult mvcResult = mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstance.getId())
                .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();

        mvcResult.getResponse().getContentAsString().contains(UPDATE_APP_INSTANCE_ERROR.getErrorMessage());
        final Optional<AppInstances> appInstancesResult = appInstancesRepository.findById(appInstancesUnderTest.getId());

        assertThat(appInstancesResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOYING);
    }

    @Test
    public void updateAppInstance_acmr_deployed_return_http_status_server_error() throws Exception {

        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstance.getApp().getCompositionId()))).andExpect(method(HttpMethod.POST)).andRespond(withServerError());

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        MvcResult mvcResult = mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstance.getId())
                .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().is5xxServerError())
            .andReturn();

        mvcResult.getResponse().getContentAsString().contains(UPDATE_APP_INSTANCE_ERROR.getErrorMessage());

        final Optional<AppInstances> appInstancesResult = appInstancesRepository.findById(appInstancesUnderTest.getId());
        assertThat(appInstancesResult.get().getStatus()).isEqualTo(AppInstanceStatus.UPDATE_ERROR);
    }

    @Test
    public void updateAppInstance_undeployed_acmr_return_http_status_server_error() throws Exception {

        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        final App app = appRepository.save(appEntity);

        final UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        appInstancesUnderTest.setStatus(AppInstanceStatus.UNDEPLOYED);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstance.getApp().getCompositionId())))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        MvcResult mvcResult = mvc.perform(put(APP_INSTANCES_INSTANCE_ID + APP_INSTANCES_COMPONENT_INSTANCE, appInstancesUnderTest.getId())
                        .content(new ObjectMapper().writeValueAsString(updateAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();

        mvcResult.getResponse().getContentAsString().contains(UPDATE_APP_INSTANCE_ERROR.getErrorMessage());

        final Optional<AppInstances> appInstancesResult = appInstancesRepository.findById(appInstancesUnderTest.getId());
        assertThat(appInstancesResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
    }

    @Test
    public void deployAppInstance_return_http_status_accepted() throws Exception {

        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.getAppComponentInstances().get(0).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
            DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void deployAppInstance_status_validation() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYING);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deployAppInstance_return_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.PUT)).andRespond(withBadRequest());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void deployAppInstance_return_500_status() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(), appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.PUT)).andRespond(withServerError());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void deployAppInstance_return_503_status() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(times(3), anything()).andExpect(method(HttpMethod.PUT)).andRespond(withServiceUnavailable());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void deployAppInstance_update_call_return_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(withBadRequest());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
    }

    @Test
    public void deployAppInstance_update_call_return_500_status() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.POST)).andRespond(withServerError());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
    }

    @Test
    public void deployAppInstance_update_call_return_503_status() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(times(3), anything()).andExpect(method(HttpMethod.POST)).andRespond(withServiceUnavailable());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
    }

    @Test
    public void undeployAppInstance_return_http_status_accepted() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);
        appInstancesUnderTest.getAppComponentInstances().get(0).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(),
                appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(),
            appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());

        final AppInstances appInstanceUndeploying = appInstancesRepository.getReferenceById(appInstancesUnderTest.getId());
        Assertions.assertEquals(AppInstanceStatus.UNDEPLOYING, appInstanceUndeploying.getStatus());
    }

    @Test
    public void undeployAppInstance_acm_instance_deploy_state_in_undeploying_return_http_status_accepted() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.getAppComponentInstances().get(0).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(),
                appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_UNDEPLOYING.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());

        final AppInstances appInstanceUndeploying = appInstancesRepository.getReferenceById(appInstancesUnderTest.getId());
        Assertions.assertEquals(AppInstanceStatus.UNDEPLOYING, appInstanceUndeploying.getStatus());
    }

    @Test
    public void undeployAppInstance_status_validation_return_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.UNDEPLOYING);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void undeployAppInstance_return_bad_request() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(),
            appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.PUT)).andRespond(withBadRequest());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        final Optional<AppInstances> appInstancesResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstancesResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOY_ERROR);
    }

    @Test
    public void undeployAppInstance_return_500_status() throws Exception {
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final App app = appRepository.save(appEntity);

        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstance.getApp().getCompositionId(),
            appInstance.getCompositionInstanceId()))).andExpect(method(HttpMethod.PUT)).andRespond(withServerError());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        final Optional<AppInstances> appInstancesResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstancesResult.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOY_ERROR);
    }

    @Test
    public void deployUndeployAppInstance_return_http_status_bad_request() throws Exception {
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);
        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                DEPLOY_ACTION, RandomStringUtils.random(10), RandomStringUtils.random(10))
                .content(new ObjectMapper().writeValueAsString(deployUndeployAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteAppInstance_return_http_status_accepted() throws Exception {
        // Given
        final App appUnderTest = TestUtils.generateAppEntity();
        appRepository.save(appUnderTest);

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setApp(appUnderTest);
        appInstancesUnderTest.setId(appInstanceId);

        appInstancesRepository.save(appInstancesUnderTest);

        String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appUnderTest.getCompositionId() + SLASH + ACM_INSTANCE + SLASH + appInstancesUnderTest.getCompositionInstanceId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess(getClasspathResourceAsString(DELETE_AUTOMATION_COMPOSITION_INSTANCE_JSON), MediaType.APPLICATION_JSON));

        // When
        final MvcResult result = mvc.perform(delete(APP_INSTANCES_INSTANCE_ID, appInstancesUnderTest.getId())).andReturn();

        // Then
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
        final AppInstanceStatus status = appInstancesRepository.findById(appInstancesUnderTest.getId()).get().getStatus();
        assertEquals(AppInstanceStatus.DELETING, status);
    }

    @Test
    public void deleteAppInstance_return_http_status_bad_request() throws Exception {
        // GIVEN the requested App Instance does not exist in LCM

        // When
        final MvcResult result = mvc.perform(delete(APP_INSTANCES_INSTANCE_ID, UUID.randomUUID())).andReturn();

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains(APP_INSTANCE_ENTITY_NOT_FOUND.getErrorTitle()));
    }

    @Test
    public void deleteAppInstanceById_with_deployed_app_instance_status_return_http_status_bad_request() throws Exception {
        // Given
        final App appUnderTest = TestUtils.generateAppEntity();
        appRepository.save(appUnderTest);

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();

        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);
        appInstancesUnderTest.setApp(appUnderTest);
        appInstancesUnderTest.setId(appInstanceId);

        appInstancesRepository.save(appInstancesUnderTest);

        // When
        final MvcResult result = mvc.perform(delete(APP_INSTANCES_INSTANCE_ID, appInstancesUnderTest.getId())).andReturn();

        // Then
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString()
                .contains(STATUS_CANNOT_BE_DEPLOYED_FOR_DELETE_OPERATION_MUST_BE_UNDEPLOYED_OR_DELETE_ERROR));
    }

    @Test
    public void deleteAppInstance_return_http_status_internal_server_error() throws Exception {
        // Given
        final App appUnderTest = TestUtils.generateAppEntity();
        appRepository.save(appUnderTest);

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setApp(appUnderTest);
        appInstancesUnderTest.setId(appInstanceId);

        appInstancesRepository.save(appInstancesUnderTest);

        String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appUnderTest.getCompositionId() + SLASH + ACM_INSTANCE + SLASH + appInstancesUnderTest.getCompositionInstanceId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(withServerError());

        // When
        final MvcResult result = mvc.perform(delete(APP_INSTANCES_INSTANCE_ID, appInstancesUnderTest.getId())).andReturn();

        // Then
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains(ERROR_DURING_DELETING_AUTOMATION_COMPOSITION_INSTANCE_IN_ACM_R));
    }

    @Test
    public void upgradeAppInstance_return_http_status_accepted() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);
        appInstancesUnderTest.getAppComponentInstances().get(0).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        final App app2 = appRepository.save(appEntity2);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockKeycloakServer(appInstance);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void upgradeAppInstance_multiple_components_return_http_status_Accepted() throws Exception {
        final App appEntity = getAppDetailsWithMultipleComponentsForUpgradeApp();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app) ;
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);
        appInstancesUnderTest.getAppComponentInstances().get(0).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23"));
        appInstancesUnderTest.getAppComponentInstances().get(1).setCompositionElementInstanceId(UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c24"));
        
        final App targetAppEntity = getAppDetailsWithMultipleComponentsForUpgradeAppWithHigherVersion();
        final App targetApp = appRepository.save(targetAppEntity);

        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(targetApp.getId().toString());

        appInstancesUnderTest.setTargetApp(targetApp);

        appInstancesRepository.save(appInstancesUnderTest);

        mockKeycloakServer(appInstancesUnderTest);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance.json"), MediaType.APPLICATION_JSON));

        mockServer.expect(anything()).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_ASDAndDataManagement_Deployed.json"), MediaType.APPLICATION_JSON));

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION, appInstancesUnderTest.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void upgradeAppInstance_status_validation() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.UPGRADING);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        final App app2 = appRepository.save(appEntity2);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void upgradeAppInstance_component_validation() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        appEntity2.getAppComponents().get(0).setName("check");
        final App app2 = appRepository.save(appEntity2);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void upgradeAppInstance_return_bad_request() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        final App app2 = appRepository.save(appEntity2);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockKeycloakServer(appInstance);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST)).andRespond(withBadRequest());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void upgradeAppInstance_return_500_status() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        final App app2 = appRepository.save(appEntity2);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        mockKeycloakServer(appInstance);

        mockServer.expect(requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void upgradeAppInstance_invalid_target_app_mode() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        appEntity2.setMode(AppMode.DISABLED);
        final App app2 = appRepository.save(appEntity2);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
            .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
            .targetAppId(app2.getId().toString())
            .additionalData(additionalData);

        mockKeycloakServer(appInstance);

        mockServer.expect(times(3), requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST))
            .andRespond(withServiceUnavailable());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                DEPLOY_ACTION,appInstance.getId())
                .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOYED);
    }

    @Test
    public void upgradeAppInstance_return_503_status() throws Exception {
        final App appEntity = getAppDetailsForUpgradeApp1();
        final App app = appRepository.save(appEntity);

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);

        final App appEntity2 = getAppDetailsForUpgradeApp2();
        final App app2 = appRepository.save(appEntity2);

        appInstancesUnderTest.setTargetApp(app2);
        final AppInstances appInstance = appInstancesRepository.save(appInstancesUnderTest);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(app, AppInstanceStatus.DEPLOYED)));
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(app2.getId().toString())
                .additionalData(additionalData);

        mockKeycloakServer(appInstance);

        mockServer.expect(times(3), requestTo(acmUrlGenerator.generateAcmCompositionUrlWithInstance(appEntity.getCompositionId()))).andExpect(method(HttpMethod.POST))
                .andRespond(withServiceUnavailable());

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                        DEPLOY_ACTION,appInstance.getId())
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        final Optional<AppInstances> appInstanceResult = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstanceResult.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR);
    }

    @Test
    public void upgradeAppInstance_return_http_status_bad_request() throws Exception {

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of());
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(UUID.randomUUID().toString())
                .additionalData(additionalData);

        mvc.perform(post(APP_INSTANCES + SLASH + INSTANCE_ID + SLASH +
                                DEPLOY_ACTION,
                        RandomStringUtils.random(10))
                        .content(new ObjectMapper().writeValueAsString(upgradeAppInstanceRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private App getAppDetailsForUpgradeApp1(){
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        Role role = Role.builder().name(USER).app(appEntity).build();
        appEntity.setRoles(List.of(role));
        return appEntity;
    }

    private App getAppDetailsForUpgradeApp2(){
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        Role role = Role.builder().name(ADMIN).app(appEntity).build();
        appEntity.setRoles(List.of(role));
        return appEntity;
    }

    private App getAppDetailsWithMultipleComponentsForUpgradeApp(){
        final App appEntity = TestUtils.generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.ENABLED,AppStatus.INITIALIZED);
        Role role = Role.builder().name(USER).app(appEntity).build();
        appEntity.setRoles(List.of(role));
        return appEntity;
    }

    private App getAppDetailsWithMultipleComponentsForUpgradeAppWithHigherVersion(){
        final App appEntity = TestUtils.generateAppEntityWithMicroserviceAndDataManagementComponentsHigherVersion(AppMode.ENABLED,AppStatus.INITIALIZED);
        Role role = Role.builder().name(USER).app(appEntity).build();
        appEntity.setRoles(List.of(role));
        return appEntity;
    }

    private void mockKeycloakServer(final AppInstances appInstancesUnderTest) throws JsonProcessingException {
        TokenDto tokenDto = TestUtils.getTokenDto();
        final ClientDto clientDto = new ClientDto();
        clientDto.setClientId(appInstancesUnderTest.getClientCredentials().get(0).getClientId());
        clientDto.setId("1");
        ClientDto[] clientDtoArray = new ClientDto[]{clientDto};

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(tokenDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER))).andExpect(method(HttpMethod.GET)).andRespond(request -> {
            ClientHttpResponse response = new MockClientHttpResponse(new ObjectMapper().writeValueAsString(clientDtoArray).getBytes(), HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(tokenDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateClientRealmRoleUrl(KEYCLOAK_REALM_MASTER))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientRolesDtos()), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(tokenDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateServiceAccountUrl(KEYCLOAK_REALM_MASTER, "1"))).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getServiceAccount()), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(tokenDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, "1"))).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(new ArrayList<>()), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl())).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(tokenDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, "1"))).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(new ArrayList<>()), MediaType.APPLICATION_JSON));
    }

    private AppInstances createAndSaveSingleAppInstanceEntity() {
        final AppInstances appInstances = createAppInstanceWithSingleAppComponent();
        setupClientCredential(appInstances);
        return appInstances;
    }

    private AppInstances createAndSaveSingleAppInstanceEntityWithASDAndDataManagementAppComponents() {
        final AppInstances appInstances = createAppInstanceWithMultipleAppComponents(true);
        setupClientCredential(appInstances);
        return appInstances;
    }

    private AppInstances createAndSaveSingleAppInstanceEntity(final AppInstanceStatus withStatus) {
        final AppInstances appInstances = createAppInstanceWithSingleAppComponent(withStatus);
        setupClientCredential(appInstances);
        return appInstances;
    }

    private AppInstances createAndSaveSingleAppInstanceEntityWithoutCredential() {
        return createAppInstanceWithSingleAppComponent();
    }

    private List<AppInstances> createAndSaveMultipleAppInstanceEntities() {
        List<AppInstances> appInstanceEntities = new ArrayList<>();

        final AppInstances singleComponentAppInstance = createAppInstanceWithSingleAppComponent();
        setupClientCredential(singleComponentAppInstance);
        appInstanceEntities.add(singleComponentAppInstance);

        final AppInstances multiComponentAppInstance = createAppInstanceWithMultipleAppComponents(false);
        setupClientCredential(multiComponentAppInstance);
        appInstanceEntities.add(multiComponentAppInstance);

        return appInstanceEntities;
    }

    private AppInstances createAppInstanceWithSingleAppComponent() {
        final AppInstances appInstance = generateAppInstanceEntity();
        // Save the generated App
        return saveAppInstance(appInstance);
    }

    private AppInstances createAppInstanceWithSingleAppComponent(final AppInstanceStatus withStatus) {
        final AppInstances appInstance = generateAppInstanceEntity(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED, withStatus, false, false);
        // Save the generated App
       return saveAppInstance(appInstance);
    }

    private AppInstances saveAppInstance(final AppInstances appInstance){
        final App app = appRepository.save(appInstance.getApp());
        // Save the App Instance
        appInstance.setApp(app);
        final AppInstances savedAppInstance = appInstancesRepository.save(appInstance);

        final AppComponentInstance appComponentInstance = TestUtils.generateAppComponentInstanceEntity(savedAppInstance, app.getId(),
            APP_COMPONENT_INSTANCE_ID_1_UNDEPLOYED);
        final AppComponent appComponent = app.getAppComponents().get(0);
        appComponentInstance.setAppComponent(appComponent);
        appComponentInstanceRepository.save(appComponentInstance);

        setupAppInstanceEvent(savedAppInstance);

        return savedAppInstance;
    }

    private AppInstances createAppInstanceWithMultipleAppComponents(final boolean withDataManagementComponent) {
        final AppInstances appInstance = generateAppInstanceEntity(ACM_COMPOSITION_INSTANCE_ID_DEPLOYED, AppInstanceStatus.DEPLOYED, true, withDataManagementComponent);
        // Save the generated App
        final App app = appRepository.save(appInstance.getApp());
        // Save the App Instance
        appInstance.setApp(app);
        final AppInstances savedAppInstance = appInstancesRepository.save(appInstance);

        // Create 2 AppComponent instances. Each instance should be provided with the AppComponent Id and the associated Comp Element Id
        final List<AppComponent> appComponentList = savedAppInstance.getApp().getAppComponents();
        final AppComponentInstance firstAppComponentInstance = TestUtils.generateAppComponentInstanceEntity(savedAppInstance, app.getId(),
            APP_COMPONENT_INSTANCE_ID_1_DEPLOYED);
        final AppComponent appComponent = appComponentList.get(0);
        firstAppComponentInstance.setAppComponent(appComponent);
        appComponentInstanceRepository.save(firstAppComponentInstance);

        final AppComponentInstance secondAppComponentInstance = TestUtils.generateAppComponentInstanceEntity(savedAppInstance, app.getId(),
            APP_COMPONENT_INSTANCE_ID_2_DEPLOYED);
        final AppComponent appComponentSecond = app.getAppComponents().get(1);
        secondAppComponentInstance.setAppComponent(appComponentSecond);
        appComponentInstanceRepository.save(secondAppComponentInstance);

        return savedAppInstance;
    }

    private ClientCredential setupClientCredential(final AppInstances appInstance) {
        final ClientCredential clientCredential = TestUtils.getClientCredential();
        clientCredential.setAppInstance(appInstance);
        return clientCredentialRepository.save(clientCredential);
    }

    private List<AppInstance> getAppInstancesFromResponseString(final String responseString) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        AppInstanceItems appInstanceItems = mapper.readValue(responseString, AppInstanceItems.class);
        return appInstanceItems.getItems();    }

    private void setupAppInstanceEvent(final AppInstances appInstance) {
        this.appInstanceEventRepository.save(
                AppInstanceEvent.builder()
                        .type(EventType.ERROR)
                        .title(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle())
                        .detail(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage())
                        .appInstance(appInstance)
                        .build()
        );
    }

    private Map<String,Object> createPropertyMap (){
        final Map<String, Object> userDefinedPropertyMap = new HashMap<>();
        userDefinedPropertyMap.put(REPLICA_COUNT_KEY, 2);
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put(TIMEOUT_KEY, 5000);
        componentPropertyMap.put(NAMESPACE, "valid-ns");
        componentPropertyMap.put(USER_DEFINED_HELM_PARAMETERS, userDefinedPropertyMap);

        final Map<String,Object> propertyMap = new HashMap<>();
        propertyMap.put(HELLO_WORLD_APP, componentPropertyMap);
        return propertyMap;
    }

    private App getAppWithMultipleComponents(){
        final App appEntity = getAppDetailsWithMultipleComponentsForUpgradeApp();
        appEntity.setMode(AppMode.ENABLED);
        return appRepository.saveAndFlush(appEntity);
    }

    private AppInstances getAppInstance(final App app) {
        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(app);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DEPLOYED);
        return appInstancesRepository.save(appInstancesUnderTest);
    }
}
