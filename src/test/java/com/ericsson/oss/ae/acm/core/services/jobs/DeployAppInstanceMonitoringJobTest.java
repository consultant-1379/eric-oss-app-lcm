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

import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_3000;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEPLOY_APP_INSTANCE_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Test case for the DeployAppInstanceMonitoringJob.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, DeployAppInstanceMonitoringJob.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class DeployAppInstanceMonitoringJobTest {
    @Autowired
    private DeployAppInstanceMonitoringJob deployAppInstanceMonitoringJob;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AcmUrlGenerator acmUrlGenerator;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;
    private MeterRegistry meterRegistry;

    @Value("${deployAppInstanceUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;
    private MockRestServiceServer mockServer;
    private MockMvc mvc;
    @BeforeEach
    public void setUp() {
        this.deployAppInstanceMonitoringJob.setTimeout(this.defaultTimeout);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    public void tearDown() {
        this.appInstancesRepository.deleteAll();
        this.appRepository.deleteAll();
        this.appInstanceEventRepository.deleteAll();
        mockServer.reset();
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    public void testMonitoringOfAppsInModeDeployingForSuccessfulDeploy() throws Throwable {
        //Given
        final AppInstances appRecord = deploySavingAppToDB();

        //Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_DEPLOYED.json"), MediaType.APPLICATION_JSON));


        //Trigger the monitoring job
        deployAppInstanceMonitoringJob.execute();
        this.deployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App should be DEPLOYED
        final AppInstances appInstanceResult = getAppFromDB(appRecord.getId());
        assertThat(appInstanceResult.getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOYED.getValue());
    }

    @Test
    public void testMonitoringOfAppsInStatusDeployingForTimeout() throws Throwable {
        //Given
        final AppInstances appRecord = deploySavingAppToDB();
        this.deployAppInstanceMonitoringJob.setTimeout(0L);

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_DEPLOYING.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        this.deployAppInstanceMonitoringJob.execute();
        this.deployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertEquals(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail());
        var counter = meterRegistry.find("app.lcm.deploy.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInStatusDeployingForAcmrError() throws Throwable {
        //Given
        final AppInstances appRecord = deploySavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_DEPLOY_ERROR.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        deployAppInstanceMonitoringJob.execute();
        this.deployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail()
                .contains(DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.deploy.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInStatusDeployingForAcmrTimeout() throws Throwable {
        //Given
        final AppInstances appRecord = deploySavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_DEPLOY_ACMR_TIMEOUT.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        deployAppInstanceMonitoringJob.execute();
        this.deployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail()
                .contains(DEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.deploy.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    void shouldLogAndCountErrorWhenRestException() throws Throwable {
        //Given
        final AppInstances appRecord = deploySavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withServerError());

        //Trigger the monitoring job
        deployAppInstanceMonitoringJob.execute();
        this.deployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check the metrics
        var counter = meterRegistry.find("app.lcm.deploy.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    private AppInstances getAppFromDB(final UUID appId) throws Throwable {
        final Optional<AppInstances> app = appInstancesRepository.findById(appId);
        if (app.isPresent()) {
            return app.get();
        } else {
            throw (new Throwable("Test code: Requested App Not in DB"));
        }
    }

    private AppInstances deploySavingAppToDB() {
        final AppInstances appUnderTest = TestUtils.generateAppInstanceEntity();
        appRepository.save(appUnderTest.getApp());
        appUnderTest.setStatus(AppInstanceStatus.DEPLOYING);
        return appInstancesRepository.save(appUnderTest);
    }
}
