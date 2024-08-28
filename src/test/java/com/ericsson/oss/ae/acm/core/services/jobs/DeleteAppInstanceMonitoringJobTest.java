/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

import static com.ericsson.oss.ae.acm.TestConstants.ID_1;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_ID_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.SCOPE;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_3000;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DELETE_APP_INSTANCE_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.acm.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Test case for the DeleteAppInstanceMonitoringJob.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, DeleteAppInstanceMonitoringJob.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class DeleteAppInstanceMonitoringJobTest {
    @Autowired
    private DeleteAppInstanceMonitoringJob deleteAppInstanceMonitoringJob;
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AcmUrlGenerator acmUrlGenerator;
    @Autowired
    private KeycloakUrlGenerator mockKeycloakUrlGenerator;
    private MockRestServiceServer mockServer;
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;
    private MeterRegistry meterRegistry;
    @Value("${deleteAppInstanceUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;

    private static final String ACCESS_TOKEN = "accessToken";

    @BeforeEach
    public void setUp() {
        deleteAppInstanceMonitoringJob.setTimeout(this.defaultTimeout);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    public void tearDown() {
        appInstancesRepository.deleteAll();
        appInstanceEventRepository.deleteAll();
        mockServer.reset();
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingForSuccessfulDeletion() throws Throwable {
        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstances.json"),MediaType.APPLICATION_JSON));

        final String authUrl = mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl();
        TokenDto token = new TokenDto();
        token.setAccessToken(ACCESS_TOKEN);
        mockServer.expect(ExpectedCount.times(5),requestTo(authUrl)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(token), MediaType.APPLICATION_JSON));

        final String clientKeycloakUrl = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(clientKeycloakUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()), MediaType.APPLICATION_JSON));

        final String clientKeycloakUrlById = mockKeycloakUrlGenerator.generateClientKeycloakUrlById(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(clientKeycloakUrlById)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess());

        //Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        this.deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the app instance will be deleted and doesn't exist in DB
        final Optional<AppInstances> appInstances = appInstancesRepository.findById(appInstancesRecord.getId());
        assertTrue(!appInstances.isPresent());
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingForFailedDeletion_ACMR_Deletion_Error() throws Throwable {
        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(ResourceLoaderUtils.getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstances_DELETE_ACMR_ERROR.json"),MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        this.deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance should be DELETE_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DELETE_ERROR.getValue());
        var counter = meterRegistry.find("app.lcm.delete.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingForFailedDeletion_ACMR_Timeout_Error() throws Throwable {
        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(ResourceLoaderUtils.getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstances_DELETE_ACMR_TIMEOUT.json"),MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        this.deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance should be DELETE_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DELETE_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getDetail().contains(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.delete.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingForMonitorJobTimeout() throws Throwable {
        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();
        deleteAppInstanceMonitoringJob.setTimeout(0L);

        // Mock the GET request
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(ResourceLoaderUtils.getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstances_DELETING.json"),MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance status should be DELETE_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DELETE_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getDetail().contains(DELETE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.delete.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingFor_Get_Req_Failure() throws Throwable {
        // This Test method is used to test the catch block when a server exception is thrown when doing a GET for all instances with compositionId towards ACMR

        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request response
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());
        mockServer.expect(ExpectedCount.times(3), requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withServerError());

        //Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        this.deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance should be still in DELETING
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DELETING.getValue());
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusDeletingForSuccessfullDeletionFromAcmr_Keycloak_Service_Exception() throws Throwable {
        // Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(appInstancesRecord.getApp().getCompositionId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstances.json"),MediaType.APPLICATION_JSON));

        final String authUrl = mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl();
        TokenDto token = new TokenDto();
        token.setAccessToken(ACCESS_TOKEN);
        mockServer.expect(ExpectedCount.times(3),requestTo(authUrl)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(token), MediaType.APPLICATION_JSON));

        final String clientKeycloakUrl = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(clientKeycloakUrl)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()), MediaType.APPLICATION_JSON));

        final String clientKeycloakUrlById = mockKeycloakUrlGenerator.generateClientKeycloakUrlById(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(clientKeycloakUrlById)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withServerError());


        // Trigger the monitoring job
        deleteAppInstanceMonitoringJob.execute();
        this.deleteAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the App instance will be still in DELETING
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DELETING.getValue());
    }

    private AppInstances getAppInstanceFromDB(final UUID appInstanceId) throws Throwable {
        final Optional<AppInstances> appInstances = appInstancesRepository.findById(appInstanceId);
        if (appInstances.isPresent()) {
            return appInstances.get();
        } else {
            throw (new Throwable("Test code: Requested App Instance Not Found In DB"));
        }
    }

    private AppInstances initSavingAppInstanceToDB() {
        final App appUnderTest = TestUtils.generateAppEntity();
        appRepository.save(appUnderTest);
        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setId(UUID.randomUUID());
        appInstancesUnderTest.setCompositionInstanceId(UUID.fromString("caf50cde-11a2-4915-a49c-609762714a6f"));
        appInstancesUnderTest.setApp(appUnderTest);
        appInstancesUnderTest.setStatus(AppInstanceStatus.DELETING);
        appInstancesUnderTest.setClientCredentials(createClientCredentials(appInstancesUnderTest));
        return appInstancesRepository.save(appInstancesUnderTest);
    }

    private List<ClientCredential> createClientCredentials(final AppInstances appInstance) {
        List<ClientCredential> clientCredentials = new ArrayList<>();
        clientCredentials.add(ClientCredential.builder()
                .id(new Random().nextLong())
                .clientId(KEYCLOAK_CLIENT_ID_KEY)
                .clientSecret("clientSecret")
                .clientScope(SCOPE)
                .appInstance(appInstance)
                .build());

        return clientCredentials;
    }
}
