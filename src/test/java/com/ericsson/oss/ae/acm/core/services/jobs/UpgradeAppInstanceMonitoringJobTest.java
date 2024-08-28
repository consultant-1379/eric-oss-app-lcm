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
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UPGRADE_APP_INSTANCE_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UPGRADE_APP_INSTANCE_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.Assertions;
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
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Test case for the UpgradeAppInstanceMonitoringJob.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, UpgradeAppInstanceMonitoringJob.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class UpgradeAppInstanceMonitoringJobTest {
    @Autowired
    private UpgradeAppInstanceMonitoringJob upgradeAppInstanceMonitoringJob;
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

    @Value("${upgradeAppInstanceUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;
    private MockRestServiceServer mockServer;
    private MockMvc mvc;
    @BeforeEach
    public void setUp() {
        this.upgradeAppInstanceMonitoringJob.setTimeout(this.defaultTimeout);
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
    void testMonitoringOfAppsInModeUpgradingForSuccessfulUpgrade() throws Throwable {
        //Given
        final AppInstances appRecord = upgradeSavingAppToDB();

        //Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withResourceNotFound());

        final String url2 = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getTargetApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url2)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_UPGRADED.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        upgradeAppInstanceMonitoringJob.execute();
        this.upgradeAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
            TimeUnit.MILLISECONDS);

        // THEN - the state of the App should be DEPLOYED
        final AppInstances appInstanceResult = getAppFromDB(appRecord.getId());
        assertThat(appInstanceResult.getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOYED.getValue());
    }

    @Test
    void testMonitoringOfAppsInStatusUpgradingForTimeout() throws Throwable {
        //Given
        final AppInstances appRecord = upgradeSavingAppToDB();
        this.upgradeAppInstanceMonitoringJob.setTimeout(0L);

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_UPGRADING.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        this.upgradeAppInstanceMonitoringJob.execute();
        this.upgradeAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
            TimeUnit.MILLISECONDS);

        //Check that the status of the App is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UPGRADE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertEquals(UPGRADE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail());
        var counter = meterRegistry.find("app.lcm.upgrade.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    void testMonitoringOfAppsInStatusUpgradingForAcmrError() throws Throwable {
        //Given
        final AppInstances appRecord = upgradeSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_UPGRADE_ERROR.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        upgradeAppInstanceMonitoringJob.execute();
        this.upgradeAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
            TimeUnit.MILLISECONDS);

        //Check that the status of the app is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UPGRADE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail()
            .contains(UPGRADE_APP_INSTANCE_COMPOSITION_TYPE_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.upgrade.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    void testMonitoringOfAppsInStatusUpgradingForAcmrTimeout() throws Throwable {
        //Given
        final AppInstances appRecord = upgradeSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appRecord.getApp().getCompositionId(), appRecord.getCompositionInstanceId());
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_UPGRADE_ACMR_TIMEOUT.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        upgradeAppInstanceMonitoringJob.execute();
        this.upgradeAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
            TimeUnit.MILLISECONDS);

        //Check that the status of the App is DEPLOY_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.DEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UPGRADE_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppInstanceEvents().get(0).getDetail()
            .contains(UPGRADE_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.upgrade.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    private AppInstances getAppFromDB(final UUID appId) throws Throwable {
        final Optional<AppInstances> app = appInstancesRepository.findById(appId);
        if (app.isPresent()) {
            return app.get();
        } else {
            throw (new Throwable("Test code: Requested App Not in DB"));
        }
    }

    private AppInstances upgradeSavingAppToDB() {
        final AppInstances appUnderTest = TestUtils.generateAppInstanceEntity();
        appUnderTest.setTargetApp(appUnderTest.getApp());
        appUnderTest.getTargetApp().setCompositionId(UUID.randomUUID());
        AppComponentInstance appComponentInstance = TestUtils.generateAppComponentInstanceEntity(appUnderTest, appUnderTest.getApp().getId(), UUID.randomUUID());
        appUnderTest.setAppComponentInstances(List.of(appComponentInstance));
        appRepository.save(appUnderTest.getApp());
        appUnderTest.setStatus(AppInstanceStatus.UPGRADING);
        return appInstancesRepository.save(appUnderTest);
    }
}