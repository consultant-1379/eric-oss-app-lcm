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
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UNDEPLOY_APP_INSTANCE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.acm.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Test case for the UndeployAppInstanceMonitoringJob.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, UndeployAppInstanceMonitoringJob.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class UndeployAppInstanceMonitoringJobTest {
    @Autowired
    private UndeployAppInstanceMonitoringJob undeployAppInstanceMonitoringJob;
    @Autowired
    private AppInstancesRepository appInstancesRepository;
    @Autowired
    private AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AcmUrlGenerator acmUrlGenerator;
    private MockRestServiceServer mockServer;
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;
    private MeterRegistry meterRegistry;
    @Value("${undeployAppInstanceUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;

    @BeforeEach
    public void setUp() {
        undeployAppInstanceMonitoringJob.setTimeout(this.defaultTimeout);
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
    public void testMonitoringOfAppsInstancesInStatusUndeployingForSuccessfulUndeploy() throws Throwable {
        //Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstancesRecord.getApp().getCompositionId(), appInstancesRecord.getCompositionInstanceId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeployed.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        undeployAppInstanceMonitoringJob.execute();
        undeployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance status should be UNDEPLOYED
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.UNDEPLOYED.getValue());
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusUndeployingForTimeout() throws Throwable {
        //Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();
        undeployAppInstanceMonitoringJob.setTimeout(0L);

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstancesRecord.getApp().getCompositionId(), appInstancesRecord.getCompositionInstanceId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeploy_Timeout.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        undeployAppInstanceMonitoringJob.execute();
        undeployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance status should be UNDEPLOY_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.UNDEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getDetail().contains(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.undeploy.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusUndeployingForAcmrError() throws Throwable {
        //Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstancesRecord.getApp().getCompositionId(), appInstancesRecord.getCompositionInstanceId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeploy_Error.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        undeployAppInstanceMonitoringJob.execute();
        undeployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance status should be UNDEPLOY_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.UNDEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UNDEPLOY_APP_INSTANCE_ERROR.getErrorTitle(), getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getDetail().contains(UNDEPLOY_APP_INSTANCE_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.undeploy.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInstancesInStatusUndeployingForAcmrTimeout() throws Throwable {
        //Given
        final AppInstances appInstancesRecord = initSavingAppInstanceToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(appInstancesRecord.getApp().getCompositionId(), appInstancesRecord.getCompositionInstanceId());

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionInstance_Undeploy_ACMR_Timeout.json"), MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        undeployAppInstanceMonitoringJob.execute();
        undeployAppInstanceMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App Instance status should be UNDEPLOY_ERROR
        assertThat(getAppInstanceFromDB(appInstancesRecord.getId()).getStatus().getValue()).isEqualTo(AppInstanceStatus.UNDEPLOY_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getType());
        assertEquals(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorTitle(), getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getTitle());
        assertTrue(getAppInstanceFromDB(appInstancesRecord.getId()).getAppInstanceEvents().get(0).getDetail().contains(UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.undeploy.failures.count").counter();
        Assertions.assertThat(counter).isNotNull();
        Assertions.assertThat(counter.count()).isEqualTo(1);
        Assertions.assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
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
        appInstancesUnderTest.setCompositionInstanceId(UUID.fromString("caf50cde-11a2-4915-a49c-609762714a6f"));
        appInstancesUnderTest.setApp(appUnderTest);
        appInstancesUnderTest.setStatus(AppInstanceStatus.UNDEPLOYING);
        return appInstancesRepository.save(appInstancesUnderTest);
    }
}