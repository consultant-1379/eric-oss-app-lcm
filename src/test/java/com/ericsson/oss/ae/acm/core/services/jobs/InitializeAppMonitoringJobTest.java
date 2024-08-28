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

package com.ericsson.oss.ae.acm.core.services.jobs;

import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_PRIMING_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.TEST_CODE_REQUESTED_APP_NOT_IN_DB;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_3000;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.INITIALIZE_APP_TIMEOUT_ERROR;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Test case for the EnableAppMonitoringJob.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, InitializeAppMonitoringJob.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class InitializeAppMonitoringJobTest {

    @Autowired
    private InitializeAppMonitoringJob initializeAppMonitoringJob;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private AppEventRepository appEventRepository;
    @Autowired
    private AcmUrlGenerator acmUrlGenerator;
    private MockRestServiceServer mockServer;
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;
    private MeterRegistry meterRegistry;
    @Value("${initializeAppUseCase.timeout.in.milliseconds}")
    private Long defaultTimeout;

    @BeforeEach
    public void setUp() {
        this.initializeAppMonitoringJob.setTimeout(this.defaultTimeout);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    public void tearDown() {
        this.appRepository.deleteAll();
        this.appEventRepository.deleteAll();
        mockServer.reset();
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    public void testMonitoringOfAppsInModeInitializingForSuccessfulInitialize() throws Throwable {
        //Given
        final App appRecord = initSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON),
                        MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        // THEN - the state of the App should be INITIALIZED
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZED.getValue());
    }

    @Test
    public void testMonitoringOfAppsInModeInitializingForTimeout() throws Throwable {
        //Given
        final App appRecord = initSavingAppToDB();
        this.initializeAppMonitoringJob.setTimeout(0L);

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_PRIMING_JSON),
                        MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        this.initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is INITIALIZE_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZE_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppEvents().get(0).getType());
        assertEquals(INITIALIZE_APP_TIMEOUT_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppEvents().get(0).getTitle());
        assertEquals(INITIALIZE_APP_TIMEOUT_ERROR.getErrorMessage(), getAppFromDB(appRecord.getId()).getAppEvents().get(0).getDetail());
        var counter = meterRegistry.find("app.lcm.initialize.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInModeInitializingForAcmrError() throws Throwable {
        //Given
        final App appRecord = initSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_PRIME_ERROR.json"),
                        MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is INITIALIZE_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZE_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppEvents().get(0).getType());
        assertEquals(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppEvents().get(0).getDetail()
                .contains(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.initialize.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    public void testMonitoringOfAppsInModeInitializingForAcmrTimeout() throws Throwable {
        //Given
        final App appRecord = initSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(getClasspathResourceAsString("expectedresponses/acm/AutomationCompositionDefinition_PRIME_ACMR_TIMEOUT.json"),
                        MediaType.APPLICATION_JSON));

        //Trigger the monitoring job
        initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check that the status of the app is INITIALIZE_ERROR and appEvent has the correct message and title
        assertThat(getAppFromDB(appRecord.getId()).getStatus().getValue()).isEqualTo(AppStatus.INITIALIZE_ERROR.getValue());

        assertEquals(EventType.ERROR, getAppFromDB(appRecord.getId()).getAppEvents().get(0).getType());
        assertEquals(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorTitle(), getAppFromDB(appRecord.getId()).getAppEvents().get(0).getTitle());
        assertTrue(getAppFromDB(appRecord.getId()).getAppEvents().get(0).getDetail()
                .contains(INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR.getErrorMessage()));
        var counter = meterRegistry.find("app.lcm.initialize.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    @Test
    void shouldLogAndCountErrorWhenRestException() throws InterruptedException {
        //Given
        final App appRecord = initSavingAppToDB();

        // Mock the GET request
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appRecord.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(withServerError());

        //Trigger the monitoring job
        initializeAppMonitoringJob.execute();
        this.initializeAppMonitoringJob.getThreadPoolTaskExecutor().getThreadPoolExecutor().awaitTermination(TIMEOUT_3000,
                TimeUnit.MILLISECONDS);

        //Check the metrics
        var counter = meterRegistry.find("app.lcm.initialize.failures.count").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.getId().getTag("LastFailedTimestamp")).isNotNull();
    }

    private App getAppFromDB(final UUID appId) throws Throwable {
        final Optional<App> app = appRepository.findById(appId);
        if (app.isPresent()) {
            return app.get();
        } else {
            throw (new Throwable(TEST_CODE_REQUESTED_APP_NOT_IN_DB));
        }
    }

    private App initSavingAppToDB() {
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setStatus(AppStatus.INITIALIZING);
        return appRepository.save(appUnderTest);
    }
}