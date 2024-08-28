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

import static com.ericsson.oss.ae.acm.TestConstants.ACM_ERROR_DETAILS_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.AMP;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_MODE_DISABLED;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_MODE_ENABLED;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_NAME;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_STATUS_CREATED;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_TYPE;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.APP_VERSION_1_1_1;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_COMMISSION_ERROR;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_INVALID_BODY_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_PRIMING_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.COMMA;
import static com.ericsson.oss.ae.acm.TestConstants.ERROR_WHILE_COMMISSIONING_AC_DEFINITION_IN_ACM_R;
import static com.ericsson.oss.ae.acm.TestConstants.GET_APPS_MODE_QUERY_FILTER;
import static com.ericsson.oss.ae.acm.TestConstants.GET_APPS_NAME_QUERY_FILTER;
import static com.ericsson.oss.ae.acm.TestConstants.GET_APPS_STATUS_QUERY_FILTER;
import static com.ericsson.oss.ae.acm.TestConstants.GET_APPS_TYPE_QUERY_FILTER;
import static com.ericsson.oss.ae.acm.TestConstants.GET_APPS_VERSION_QUERY_FILTER;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_GO_APP_1;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_GO_APP_3;
import static com.ericsson.oss.ae.acm.TestConstants.HTTP_400;
import static com.ericsson.oss.ae.acm.TestConstants.HTTP_500;
import static com.ericsson.oss.ae.acm.TestConstants.LCM_PARTICIPANT;
import static com.ericsson.oss.ae.acm.TestConstants.PATH_PARAM_START;
import static com.ericsson.oss.ae.acm.TestConstants.TEST_CODE_REQUESTED_APP_NOT_IN_DB;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_3000;
import static com.ericsson.oss.ae.acm.TestConstants.VALIDATION_ERROR;
import static com.ericsson.oss.ae.acm.TestConstants.VALIDATION_MISSING_APP_NAME_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APPS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APPS_APP_ID;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.GET_AUTOMATION_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.LCM_APP_MODE_VALIDATION_ERROR;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
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

import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import spock.lang.Specification;

import com.ericsson.coverage.filter.ContractCoverageFilterV3;
import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestConstants;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.ToscaIdentifier;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.clients.minio.config.MinioTestClient;
import com.ericsson.oss.ae.acm.core.services.jobs.InitializeAppMonitoringJob;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.Artifact;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.presentation.controller.AppsController;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.EnableDisableAppRequest;
import com.ericsson.oss.ae.v3.api.model.InitializeActionRequest;


@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AppLcmApplication.class, AppsController.class, InitializeAppMonitoringJob.class})
@EnableRetry
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:acm/application-test.properties")
@ActiveProfiles("test")
@ContextConfiguration(classes = MinioTestClient.class)
public class AppsControllerTest extends Specification{

    private static final String INITIALIZE_ACTION_URL =  APPS_APP_ID + "/initialization-actions";
    private static final String ENABLE_DISABLE_URL =  APPS_APP_ID + "/mode";
    private static final String AUTOMATION_COMPOSITION_DEFINITION_DEPRIMING_JSON = "expectedresponses/acm/AutomationCompositionDefinition_DEPRIMING.json";
    private static final String ACTION_INITIALIZE = "initialize";
    private static final String ACTION_DEINITIALIZE = "deinitialize";

    private MockMvc mvc;

    private MockRestServiceServer mockServer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AcmUrlGenerator acmUrlGenerator;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppComponentRepository appComponentRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InitializeAppMonitoringJob initializeAppMonitoringJob;

    @Value("${initializeAppUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;

    @BeforeAll
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(new ContractCoverageFilterV3()).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        initializeAppMonitoringJob.setTimeout(defaultTimeout);
    }

    @BeforeEach
    public void beforeEach() {
        appRepository.deleteAll();
        appComponentRepository.deleteAll();
    }

    @AfterEach
    public void resetMockServer() {
        appRepository.deleteAll();
        mockServer.reset();
    }

    @Test
    public void createApp_return_http_status_created() throws Exception {

        String url = acmUrlGenerator.getAcmCompositionUrl();
        CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();

        ToscaIdentifier affectedAutomationComposition = new ToscaIdentifier(LCM_PARTICIPANT, APP_VERSION_1_1_1);
        List<ToscaIdentifier> affectedAutomationCompositionDefinitions = List.of(affectedAutomationComposition);
        AcCommissionResponse acmCreateCompositionResponse = new AcCommissionResponse(UUID.randomUUID(),
            affectedAutomationCompositionDefinitions);

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(new ObjectMapper().writeValueAsString(acmCreateCompositionResponse), MediaType.APPLICATION_JSON));

        mvc.perform(post(APPS)
                        .content(new ObjectMapper().writeValueAsString(createAppRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mode").value(AppMode.DISABLED.getValue()))
                .andExpect(jsonPath("$.type").value(APP_ENTITY_TYPE))
                .andExpect(jsonPath("$.name").value(APP_ENTITY_NAME))
                .andExpect(jsonPath("$.status").value(AppStatus.CREATED.getValue()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());


        mvc.perform(get(APPS))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value(APP_ENTITY_NAME))
            .andExpect(jsonPath("$.items[0].version").value(APP_VERSION_1_1_1))
            .andExpect(jsonPath("$.items[0].permissions", hasSize(3))).andExpect(jsonPath("$.items[0].permissions[*]",
                hasItem(hasEntry("resource", KeycloakConstants.KAFKA))));
    }

    @Test
    public void createApp_multiple_component_return_http_status_created() throws Exception {

        String url = acmUrlGenerator.getAcmCompositionUrl();
        CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequestWithMultipleComponents();

        final ToscaIdentifier affectedAutomationComposition = new ToscaIdentifier(LCM_PARTICIPANT, APP_VERSION_1_1_1);
        final List<ToscaIdentifier> affectedAutomationCompositionDefinitions = List.of(affectedAutomationComposition);
        final AcCommissionResponse acmCreateCompositionResponse = new AcCommissionResponse(UUID.randomUUID(),
            affectedAutomationCompositionDefinitions);

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
            withSuccess(new ObjectMapper().writeValueAsString(acmCreateCompositionResponse), MediaType.APPLICATION_JSON));

        mvc.perform(post(APPS)
                .content(new ObjectMapper().writeValueAsString(createAppRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.mode").value(AppMode.DISABLED.getValue()))
            .andExpect(jsonPath("$.type").value(APP_ENTITY_TYPE))
            .andExpect(jsonPath("$.name").value(APP_ENTITY_NAME))
            .andExpect(jsonPath("$.status").value(AppStatus.CREATED.getValue()))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.createdAt").isNotEmpty());


        mvc.perform(get(APPS))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value(APP_ENTITY_NAME))
            .andExpect(jsonPath("$.items[0].version").value(APP_VERSION_1_1_1))
            .andExpect(jsonPath("$.items[0].permissions", hasSize(3))).andExpect(jsonPath("$.items[0].permissions[*]",
                hasItem(hasEntry("resource", KeycloakConstants.KAFKA))));
    }

    @Test
    public void createApp_return_http_status_bad_request() throws Exception {

        String url = acmUrlGenerator.getAcmCompositionUrl();
        CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
            withBadRequest());

        mvc.perform(post(APPS)
                        .content(new ObjectMapper().writeValueAsString(createAppRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(AUTOMATION_COMPOSITION_DEFINITION_COMMISSION_ERROR))
                .andExpect(jsonPath("$.status").value(HTTP_500))
                .andExpect(jsonPath("$.detail").value(ERROR_WHILE_COMMISSIONING_AC_DEFINITION_IN_ACM_R));
    }

    @Test
    public void createApp_return_http_status_internal_server_error() throws Exception {

        String url = acmUrlGenerator.getAcmCompositionUrl();
        CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
            withServerError());

        mvc.perform(post(APPS)
                        .content(new ObjectMapper().writeValueAsString(createAppRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(AUTOMATION_COMPOSITION_DEFINITION_COMMISSION_ERROR))
                .andExpect(jsonPath("$.status").value(HTTP_500))
                .andExpect(jsonPath("$.detail").value(ERROR_WHILE_COMMISSIONING_AC_DEFINITION_IN_ACM_R));
    }

    @Test
    public void createApp_return_http_status_bad_request_validation_error_missing_app_name() throws Exception {

        CreateAppRequest invalidCreateAppRequest = TestUtils.generateCreateAppRequest();
        invalidCreateAppRequest.setName("");

        mvc.perform(post(APPS)
                .content(new ObjectMapper().writeValueAsString(invalidCreateAppRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value(VALIDATION_ERROR))
            .andExpect(jsonPath("$.status").value(HTTP_400))
            .andExpect(jsonPath("$.detail").value(VALIDATION_MISSING_APP_NAME_MESSAGE));
    }

    @Test
    public void getApps_return_http_status_ok() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_3))
                .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
                .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
                .andExpect(jsonPath("$.items[0].type").value(TestConstants.APP_ENTITY_TYPE))
                .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    public void getApps_with_query_return_http_status_ok() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_NAME_QUERY_FILTER + HELLO_WORLD_GO_APP_1))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_1))
                .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
                .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
                .andExpect(jsonPath("$.items[0].type").value(TestConstants.APP_ENTITY_TYPE));
    }

    @Test
    public void getApps_with_query_contains_status() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_STATUS_QUERY_FILTER + APP_ENTITY_STATUS_CREATED))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_3))
                .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
                .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
                .andExpect(jsonPath("$.items[0].type").value(APP_ENTITY_TYPE))
                .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    public void getApps_with_query_contains_invalid_status() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_STATUS_QUERY_FILTER + "cREAT"))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(HTTP_400))
            .andExpect(jsonPath("$.detail").value(
                "Status cannot be cREAT for GET Apps operation - must be CREATED or DELETED or DELETE_ERROR or INITIALIZING or INITIALIZED or INITIALIZE_ERROR or DEINITIALIZING or DEINITIALIZED or DEINITIALIZE_ERROR."));
    }

    @Test
    public void getApps_with_query_contains_mode() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_MODE_QUERY_FILTER + APP_ENTITY_MODE_DISABLED))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_3))
                .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
                .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
                .andExpect(jsonPath("$.items[0].type").value(APP_ENTITY_TYPE))
                .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    public void getApps_with_query_contains_invalid_mode() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_MODE_QUERY_FILTER + "DiSABLD"))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(HTTP_400))
            .andExpect(jsonPath("$.detail").value("App mode cannot be DiSABLD for GET Apps operation - must be ENABLED or DISABLED."));
    }

    @Test
    public void getApps_with_query_contains_type() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_TYPE_QUERY_FILTER + APP_ENTITY_TYPE))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_3))
            .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
            .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
            .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
            .andExpect(jsonPath("$.items[0].type").value(APP_ENTITY_TYPE))
            .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    public void getApps_with_query_contains_version() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_VERSION_QUERY_FILTER + APP_ENTITY_VERSION))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value(HELLO_WORLD_GO_APP_3))
            .andExpect(jsonPath("$.items[0].version").value(APP_ENTITY_VERSION))
            .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
            .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED))
            .andExpect(jsonPath("$.items[0].type").value(APP_ENTITY_TYPE));
    }

    @Test
    public void getApps_with_query_contains_mode_and_status() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);
        App app1 = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        App app2 = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        app1.setName("APP1");
        app2.setName("APP2");
        appsUnderTest.add(app1);
        appsUnderTest.add(app2);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_MODE_QUERY_FILTER
                        + APP_ENTITY_MODE_DISABLED + AMP + GET_APPS_STATUS_QUERY_FILTER + AppStatus.INITIALIZED))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value("APP2"))
            .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
            .andExpect(jsonPath("$.items[0].status").value("INITIALIZED"))
            .andExpect(jsonPath("$.items[1].name").value("APP1"))
            .andExpect(jsonPath("$.items[1].mode").value(APP_ENTITY_MODE_DISABLED))
            .andExpect(jsonPath("$.items[1].status").value("INITIALIZED"))
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    public void getApps_with_query_contains_names_and_modes() throws Exception {
        final List<App> appsUnderTest = TestUtils.generateAppEntities(3, AppMode.DISABLED);
        App app1 = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        App app2 = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        app1.setName("APP1");
        app2.setName("APP2");
        appsUnderTest.add(app1);
        appsUnderTest.add(app2);

        this.appRepository.saveAll(appsUnderTest);

        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_MODE_QUERY_FILTER + APP_ENTITY_MODE_DISABLED
                        + COMMA + APP_ENTITY_MODE_ENABLED + AMP + GET_APPS_NAME_QUERY_FILTER + "APP1" + COMMA + "APP2"))
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.items[0].name").value("APP2"))
            .andExpect(jsonPath("$.items[0].mode").value("ENABLED"))
            .andExpect(jsonPath("$.items[0].status").value("INITIALIZED"))
            .andExpect(jsonPath("$.items[1].name").value("APP1"))
            .andExpect(jsonPath("$.items[1].mode").value(APP_ENTITY_MODE_DISABLED))
            .andExpect(jsonPath("$.items[1].status").value("INITIALIZED"))
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    public void getAppById_return_http_status_ok() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity();

        this.appRepository.save(appUnderTest);

        mvc.perform(get(APPS + SLASH + "{appId}", appUnderTest.getId()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(APP_ENTITY_NAME))
                .andExpect(jsonPath("$.version").value(APP_ENTITY_VERSION))
                .andExpect(jsonPath("$.mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.status").value(APP_ENTITY_STATUS_CREATED))
                .andExpect(jsonPath("$.type").value(APP_ENTITY_TYPE));
    }

    @Test
    public void getApps_with_query_nonexisting_name() throws Exception {
        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_NAME_QUERY_FILTER + HELLO_WORLD_GO_APP_1))
                .andExpect(content().string("{\"items\":[]}"));
    }

    @Test
    public void getApps_with_query_nonexisting_status() throws Exception {
        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_STATUS_QUERY_FILTER + APP_ENTITY_STATUS_CREATED))
                .andExpect(content().string("{\"items\":[]}"));
    }

    @Test
    public void getApps_with_query_nonexisting_mode() throws Exception {
        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_MODE_QUERY_FILTER + APP_ENTITY_MODE_DISABLED))
                .andExpect(content().string("{\"items\":[]}"));
    }

    @Test
    public void getApps_with_query_nonexisting_type() throws Exception {
        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_TYPE_QUERY_FILTER + APP_ENTITY_TYPE))
                .andExpect(content().string("{\"items\":[]}"));
    }

    @Test
    public void getApps_with_query_nonexisting_version() throws Exception {
        mvc.perform(get(APPS + PATH_PARAM_START + GET_APPS_VERSION_QUERY_FILTER + APP_ENTITY_VERSION))
                .andExpect(content().string("{\"items\":[]}"));
    }

    @Test
    public void getAppById_return_http_status_not_found() throws Exception {
        mvc.perform(get(APPS_APP_ID, null, null)).andExpect(status().isNotFound());
    }

    @Test
    public void getAppById_return_http_status_bad_request() throws Exception {
        mvc.perform(get(APPS_APP_ID, 1)).andExpect(status().isBadRequest());
    }

    @Test
    public void getAppById_return_not_found() throws Exception {
        mvc.perform(get(APPS_APP_ID, UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    public void postInitializeAppAction_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));
        // Mock the Prime Request
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @Test
    public void postInitializeAppAction_in_state_InitializeError_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setStatus(AppStatus.INITIALIZE_ERROR);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();

        // Mock the GET request
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));
        // Mock the Prime Request
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @Test
    public void postInitializeAppAction_return_http_status_accepted_AND_appPrimed() throws Throwable {

        //Given
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(once(), requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));
        // Mock the Prime Request
        mockServer.expect(once(), requestTo(url)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // WHEN - the App is initialized and the Monitoring Job is triggered
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());

        //cleanup mock server
        mockServer.reset();

        // Mock the 1st poll attempt
        mockServer.expect(once(), requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON),
                        MediaType.APPLICATION_JSON));

        initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App should be INITIALIZE_ERROR
        assertThat(getAppFromDB(appId).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZED.getValue());
    }

    @Test
    public void postInitializeAppAction_acm_prime_4xx_error() throws Exception {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        // AND the PRIME request fails due to 400 Bad Request from ACM
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
                .andRespond(withBadRequest()
                        .body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user along with the error reason
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorTitle()));
    }

    @Test
    public void postInitializeAppAction_acm_prime_5xx_error() throws Throwable {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        // AND the PRIME request fails due to 501 Internal Server from ACM
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError()
                        .body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorTitle()));

        assertThat(getAppFromDB(appId).getStatus().getValue()).isEqualTo(AppStatus.CREATED.getValue());
    }

    @Test
    public void postInitializeAppAction_acm_get_comp_5xx_error() throws Throwable {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND Internal Server Error is returned by ACM for the GET request for the AC type check
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withServerError().body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));

        // AND the PRIME request fails due to 501 Internal Server from ACM
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError()
                        .body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(GET_AUTOMATION_COMPOSITION_TYPE_ERROR.getErrorTitle()));

        assertThat(getAppFromDB(appId).getStatus().getValue()).isEqualTo(AppStatus.CREATED.getValue());
    }

    @Test
    public void postInitializeAppAction_acm_connection_failure_5xx_error() throws Throwable {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND Internal Server Error is returned by ACM for the GET request for the AC type check
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(times(3), requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withException(new SocketTimeoutException()));

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(GET_AUTOMATION_COMPOSITION_TYPE_ERROR.getErrorTitle()));

        assertThat(getAppFromDB(appId).getStatus().getValue()).isEqualTo(AppStatus.CREATED.getValue());
    }

    @Test
    public void postInitializeAppAction_get_ac_def_invalid_response_body() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_INVALID_BODY_JSON),
                        MediaType.APPLICATION_JSON));

        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void postInitializeAppAction_app_already_initializing_4xx_error() throws Exception {
        // GIVEN an App is already created in LCM but is already INITIALIZING
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setStatus(AppStatus.INITIALIZING);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user along with the error reason
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value(LCM_APP_MODE_VALIDATION_ERROR.getErrorTitle()));
    }

    @Test
    public void postInitializeAppAction_acm_prime_503_with_retries_error() throws Exception {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        // AND the PRIME request fails due to 501 Internal Server from ACM
        mockServer.expect(times(3), requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServiceUnavailable());

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    public void postInitializeAppAction_acm_prime_503_retry_then_accepted() throws Throwable {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity();
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        // AND the PRIME request fails due to 501 Internal Server from ACM
        mockServer.expect(times(2), requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServiceUnavailable()
                        .body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));
        // The accepted
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());

        assertThat(getAppFromDB(appId).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZING.getValue());
    }

    @Test
    public void postInitializeAppAction_app_not_existing_4xx_error() throws Exception {
        // GIVEN the requested App not created in LCM

        // When the Initial App request is sent THEN 501 Internal Server Error is returned to the user along with the error reason
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, UUID.randomUUID())
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(APP_NOT_FOUND_ERROR.getErrorTitle()));
    }

    @Test
    public void postInitializeAppAction_invalid_action_type_request() throws Exception {
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, UUID.randomUUID())
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postInitializeAppAction_bad_body_request() throws Exception {
        mvc.perform(post(INITIALIZE_ACTION_URL, UUID.randomUUID())
                        .content(new ObjectMapper().writeValueAsString("{Bad:Data}"))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postInitializeAppAction_bad_appid_request() throws Exception {
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_INITIALIZE);

        mvc.perform(post(INITIALIZE_ACTION_URL, 1)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putInitializeAppAction_http_status_not_found() throws Exception {
        mvc.perform(put(INITIALIZE_ACTION_URL, null, null)
                        .content(new ObjectMapper().writeValueAsString(null))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void putEnableDisableApp_bad_body_request() throws Exception {
        mvc.perform(put(APPS + SLASH + "{appId}/mode", UUID.randomUUID())
                        .content(new ObjectMapper().writeValueAsString("{Bad:Data}"))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putEnableDisableApp_bad_appId_request() throws Exception {
        mvc.perform(put(APPS + SLASH + "{appId}/mode", 1)
                        .content(AppMode.ENABLED.getValue())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putEnableDisableApp_http_status_not_found() throws Exception {
        mvc.perform(put(APPS + SLASH + "{appId}/mode", null, null)
                        .content(new ObjectMapper().writeValueAsString(null))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAppById_return_http_status_no_content() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity());
        Artifact artifact = appUnderTest.getAppComponents().iterator().next().getArtifacts().iterator().next();
        String location = artifact.getLocation();
        String bucketId = location.split("/")[0];
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketId).build());
        byte[] objectData = "Dummy object".getBytes();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketId)
                .object(artifact.getName())
                .stream(new ByteArrayInputStream(objectData), objectData.length, -1)
                .build());

        String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appUnderTest.getCompositionId();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        // Act & Assert
        mvc.perform(delete("/apps/{appId}", appUnderTest.getId())).andExpect(status().isNoContent());
    }

    @Test
    public void deleteAppById_return_http_status_internal_server_error() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity());

        String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appUnderTest.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(withServerError());

        // Act & Assert
        mvc.perform(delete(APPS_APP_ID, appUnderTest.getId())).andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteAppById_return_http_status_not_found() throws Exception {
        String appId = null;
        mvc.perform(delete(APPS_APP_ID, appId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAppById_return_http_status_bad_request() throws Exception {
        mvc.perform(delete(APPS_APP_ID,
                        RandomStringUtils.random(10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putDisableApp_return_http_status_ok() throws Throwable {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        final EnableDisableAppRequest executeRequest = new EnableDisableAppRequest().mode(AppMode.valueOf(APP_ENTITY_MODE_DISABLED));

        mvc.perform(put(ENABLE_DISABLE_URL, appId).content(new ObjectMapper().writeValueAsString(executeRequest)).contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.mode").value("DISABLED"))
                .andExpect(jsonPath("$.app.id").value(appId.toString()))
                .andExpect(jsonPath("$.app.href").value("/app-lifecycle-management/v3/apps/"+appId.toString()));

        assertThat(getAppFromDB(appId).getMode().getValue()).isEqualTo(AppMode.DISABLED.getValue());
    }

    @Test
    void putEnableApp_return_http_status_ok() throws Throwable {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        final EnableDisableAppRequest executeRequest = new EnableDisableAppRequest().mode(AppMode.valueOf(APP_ENTITY_MODE_ENABLED));

        mvc.perform(put(ENABLE_DISABLE_URL, appId).content(new ObjectMapper().writeValueAsString(executeRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("ENABLED"))
                .andExpect(jsonPath("$.app.id").value(appId.toString()))
                .andExpect(jsonPath("$.app.href").value("/app-lifecycle-management/v3/apps/"+appId.toString()));
        ;

        assertThat(getAppFromDB(appId).getMode().getValue()).isEqualTo(AppMode.ENABLED.getValue());
    }

    @Test
    void putDisableApp_return_http_status_isBadRequest() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        final EnableDisableAppRequest executeRequest = new EnableDisableAppRequest().mode(AppMode.valueOf(APP_ENTITY_MODE_ENABLED));

        mvc.perform(put(ENABLE_DISABLE_URL, appId).content(new ObjectMapper().writeValueAsString(executeRequest)).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putCurrentAppMode_return_bad_request() throws Throwable {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        final EnableDisableAppRequest executeRequest = new EnableDisableAppRequest().mode(AppMode.valueOf(APP_ENTITY_MODE_ENABLED));

        mvc.perform(put(ENABLE_DISABLE_URL, appId).content(new ObjectMapper().writeValueAsString(executeRequest)).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void putWithInvalidStatus_return_http_status_badRequest() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        final EnableDisableAppRequest executeRequest = new EnableDisableAppRequest().mode(AppMode.valueOf(APP_ENTITY_MODE_ENABLED));

        mvc.perform(put(ENABLE_DISABLE_URL, appId).content(new ObjectMapper().writeValueAsString(executeRequest)).contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postDeInitializeAppAction_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON),
                        MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @Test
    public void postDeInitializeAppAction_in_state_dePriming_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_DEPRIMING_JSON),
                        MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }
    @Test
    public void postDeInitializeAppAction_in_state_INITIALIZE_ERROR_TIMEOUT_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZE_ERROR);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_COMMISSIONED_TIMEOUT_ERROR.json"),
                MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action("deinitialize");
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void postDeInitializeAppAction_in_state_INITIALIZE_ERROR_FAILED_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZE_ERROR);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_COMMISSIONED_FAILED_ERROR.json"),
                MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action("deinitialize");
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void postDeInitializeAppAction_in_state_Priming_With_Error_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZE_ERROR);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_PRIME_ERROR.json"),
                MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action("deinitialize");
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void postDeInitializeAppAction_in_state_Priming_With_Timeout_return_http_status_accepted() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZE_ERROR);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_PRIME_ACMR_TIMEOUT.json"),
                MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action("deinitialize");
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                .contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void postDeInitializeAppAction_in_state_Priming_NoError_return_http_status_badRequest() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZING);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMING_JSON),
                        MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postDeInitializeAppAction_in_invalid_appstatus_return_http_status_badRequest() throws Exception {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // Mock the GET request
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_DEPRIMING_JSON),
                        MediaType.APPLICATION_JSON));
        // Mock the DePrime Request
        mockServer.expect(requestTo(compositionUrl)).andExpect(method(HttpMethod.PUT)).andRespond(withAccepted());

        // Send De-Initialize action to
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postDeInitializeAppAction_app_already_deinitializing_4xx_error() throws Exception {
        // GIVEN an App is already created in LCM but is already DEINITIALIZING
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setStatus(AppStatus.DEINITIALIZING);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // When the Initial App request is sent THEN 400 Bad Request Error is returned to the user along with the error reason
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value(LCM_APP_MODE_VALIDATION_ERROR.getErrorTitle()));
    }

    @Test
    public void postDeInitializeAppAction_app_not_existing_4xx_error() throws Exception {
        // GIVEN the requested App not created in LCM

        // When the Initial App request is sent THEN 400 Bad Request Error is returned to the user along with the error reason
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, UUID.randomUUID())
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(APP_NOT_FOUND_ERROR.getErrorTitle()));
    }

    @Test
    public void postDeInitializeAppAction_bad_appid_request() throws Exception {
        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);

        mvc.perform(post(INITIALIZE_ACTION_URL, 1)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postDeInitializeAppAction_acm_prime_4xx_error() throws Exception {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED,AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM-R
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON), MediaType.APPLICATION_JSON));

        // AND the De-Prime request fails due to rest request failure
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
                .andRespond(withBadRequest()
                        .body(getClasspathResourceAsString(ACM_ERROR_DETAILS_JSON)));

        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void postDeInitializeAppAction_app_commissioned_in_acmr_no_error() throws Exception {
        // GIVEN an App is already created in LCM
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED,AppStatus.INITIALIZED);
        final App appRecord = appRepository.save(appUnderTest);
        final UUID appId = appRecord.getId();

        // AND the AC Type has already been commissioned in ACM-R
        final String url = acmUrlGenerator.getAcmCompositionUrl() + SLASH + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON), MediaType.APPLICATION_JSON));

        final InitializeActionRequest executeActionRequest = new InitializeActionRequest().action(ACTION_DEINITIALIZE);
        mvc.perform(post(INITIALIZE_ACTION_URL, appId)
                        .content(new ObjectMapper().writeValueAsString(executeActionRequest))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isAccepted());

        mvc.perform(get(APPS))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].mode").value(APP_ENTITY_MODE_DISABLED))
                .andExpect(jsonPath("$.items[0].status").value(APP_ENTITY_STATUS_CREATED));
    }

    private App getAppFromDB(final UUID appId) throws Throwable {
        final Optional<App> app = appRepository.findById(appId);
        if (app.isPresent()) {
            return app.get();
        } else {
            throw (new Throwable(TEST_CODE_REQUESTED_APP_NOT_IN_DB));
        }
    }
}