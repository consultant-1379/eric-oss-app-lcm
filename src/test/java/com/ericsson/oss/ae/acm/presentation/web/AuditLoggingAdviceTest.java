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
package com.ericsson.oss.ae.acm.presentation.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.presentation.controller.AppsController;
import com.ericsson.oss.ae.presentation.controllers.AppInstanceController;
import com.ericsson.oss.ae.presentation.controllers.v2.AppInstanceV2Controller;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, AppInstanceController.class, AppInstanceV2Controller.class, AppsController.class})
@EnableRetry(proxyTargetClass = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:acm/application-test-audit-log.properties")
public class AuditLoggingAdviceTest {

    private MockMvc mvc;
    private MockRestServiceServer mockServer;
    private Logger auditLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        auditLogger = (Logger) LoggerFactory.getLogger(AuditLogger.class);
        listAppender = new ListAppender<>();
        listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        auditLogger.addAppender(listAppender);
        listAppender.start();
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
        listAppender.stop();
        listAppender.list.clear();
    }

    @Test
    public void verifyAuditLogIsMissingForV3Get() throws Throwable {

        mvc.perform(MockMvcRequestBuilders.get("/v3/apps"))
            .andExpect(status().isOk());

        final List<ILoggingEvent> logList = listAppender.list;
        assertEquals(0, logList.size());
    }

    @Test
    public void verifyAuditLogIsMissingForV2() throws Throwable {

        mvc.perform(MockMvcRequestBuilders.get("/app-lcm/v2/app-instances"))
            .andExpect(status().isOk());

        final List<ILoggingEvent> logList = listAppender.list;
        assertEquals(0, logList.size());
    }

    @Test
    public void verifyAuditLogIsMissingForV1Get() throws Throwable {

        mvc.perform(MockMvcRequestBuilders.get("/app-lcm/v1/app-instances"))
            .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        assertEquals(0, logList.size());
    }

    @Test
    public void verifyAuditLogSubjectIsMissingWhenHeaderIsNotSet() throws Throwable {
        mvc.perform(put("/app-lcm/v1/app-instances/1"))
            .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        final ILoggingEvent loggedEvent = logList.get(0);
        final Map<String, String> mdcPropertyMap = loggedEvent.getMDCPropertyMap();

        assertEquals("404", mdcPropertyMap.get("resp_code"));
        assertEquals("log audit", mdcPropertyMap.get("facility"));
        assertEquals("n/av", mdcPropertyMap.get("subject"));
        assertEquals("257", mdcPropertyMap.get("resp_message"));
        assertEquals("TERMINATE App Instance {1} PUT http://localhost/app-lcm/v1/app-instances/1", loggedEvent.getFormattedMessage());
    }

    @Test
    public void verifyAuditLogSubjectIsMissingWhenAuthIsInvalid() throws Throwable {

        mvc.perform(MockMvcRequestBuilders
                .delete("/v3/apps/951b4e6a-d9dd-4df1-a8d8-f76e1862f492")
                .header("authorization", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3MHhHV1hGRFZrcTF5bHFxYm1rMV83ZVh1b1BmTnlDNTdRZlM3MnJFSVJRIn0.eyJleHAiOjE3MDU5NTg0ODEsImlhdCI6MTcwNTk1ODE4MSwianRpIjoiODU5YzU0ZTMtY2IxNS00NGZmLTllZTgtZWUyZTYzZWI1YWE2IiwiaXNzIjoiaHR0cHM6Ly9laWMuaGFsbDEzNC14MS5ld3MuZ2ljLmVyaWNzc29uLnNlL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6WyJtYXN0ZXItcmVhbG0iLCJhY2NvdW50Il0sInN1YiI6IjVmNWVkYzI0LWFjZGUtNDVmYy1iYjUwLWJhMDU5NTYxNGYzNiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVwcm9yb21BZG1pbiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyZWF0ZS1yZWFsbSIsImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwiQXBwTWdyQWRtaW4iLCJVc2VyQWRtaW4iLCJBcHBNZ3JPcGVyYXRvciIsIm9mZmxpbmVfYWNjZXNzIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsIkdBU19Vc2VyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsibWFzdGVyLXJlYWxtIjp7InJvbGVzIjpbInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwidmlldy1yZWFsbSIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInF1ZXJ5LXJlYWxtcyIsInZpZXctYXV0aG9yaXphdGlvbiIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS11c2VycyIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwgcm9sZXMiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudEhvc3QiOiIxMC4xNTYuNzYuMjgiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtZXByb3JvbWFkbWluIiwiY2xpZW50QWRkcmVzcyI6IjEwLjE1Ni43Ni4yOCIsImNsaWVudF9pZCI6ImVwcm9yb21BZG1pbiJ9.N5L8XqC2gjQbWqpHwGRJF7CbDt66FfFXI1mXJUhlCoslRBtiFlLS4B02GEkDTUKLcMp80WBI50LF2B006kjU0PmgZ9JlJYlS_M24LYH1wZGYPCtb91zqfRmKdrk0d-VZwl4OHKzae9os2Ebr9QkKH7iavkEAHo-A1GRxUUqbiM1oHRs_EVgGycYPRrII-6Viz32Kalhp-ZNhDAPMmqAxrb78JdtTpIvOsNnToguGonNGPjlRHZ8e3gLnqRNPPbHU_3vF-MUSo2hpPRtNKeXm5N10Al9qMf9cirFzKcqLZv6j1RQdEBHeoQ0dUvmRxdeMJRo-PFi-k9KAKIYwIoj84w"))
            .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        final ILoggingEvent loggedEvent = logList.get(0);
        final Map<String, String> mdcPropertyMap = loggedEvent.getMDCPropertyMap();

        assertEquals("404", mdcPropertyMap.get("resp_code"));
        assertEquals("log audit", mdcPropertyMap.get("facility"));
        assertEquals("n/av", mdcPropertyMap.get("subject"));
        assertEquals("79", mdcPropertyMap.get("resp_message"));
    }

    @Test
    public void verifyAuditLogSubjectIsPresentWhenUpnIsSet() throws Throwable {

        mvc.perform(MockMvcRequestBuilders
                .delete("/v3/apps/951b4e6a-d9dd-4df1-a8d8-f76e1862f492")
                .header("authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDRmVweVdhVjBGQjExYUhsalY3TDhyQ2l1RUlZZG9WT0JWZUxxM3lHdjNBIn0.eyJleHAiOjE3MTI3NjM0MTEsImlhdCI6MTcxMjc2MzExMSwiYXV0aF90aW1lIjoxNzEyNzYwNjMyLCJqdGkiOiIzODEzMDBlYy1hM2Q4LTQ4MzEtODExNC1kNzFhY2NmMzMxOWQiLCJpc3MiOiJodHRwczovL2VpYy5oYXJ0MDk4LXgyLmV3cy5naWMuZXJpY3Nzb24uc2UvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjpbImFkcC1pYW0tYWEtY2xpZW50IiwibWFzdGVyLXJlYWxtIiwiYWNjb3VudCJdLCJzdWIiOiJlNTIxYzk1ZS03MWM4LTQxMjEtOTdjYi01YWRiOGYwN2RmODIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZHAtaWFtLWFhLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiIyOWIyOWE4My1iYWIxLTRmODQtOWZmNC0xZTBmZDNiYzcxNDkiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1tYXN0ZXIiLCJVc2VyQWRtaW4iLCJMb2dWaWV3ZXJfU3lzdGVtX0FwcGxpY2F0aW9uX09wZXJhdG9yIiwiYWRtaW4iLCJMb2dBUElfRXh0QXBwc19BcHBsaWNhdGlvbl9SZWFkT25seSIsImNyZWF0ZS1yZWFsbSIsIkFwcE1nckFkbWluIiwiTG9nVmlld2VyX0V4dEFwcHNfQXBwbGljYXRpb25fT3BlcmF0b3IiLCJvZmZsaW5lX2FjY2VzcyIsIkFwcE1ncl9BcHBsaWNhdGlvbl9BZG1pbmlzdHJhdG9yIiwiTG9nVmlld2VyIiwidW1hX2F1dGhvcml6YXRpb24iLCJHQVNfVXNlciIsIlNlYXJjaEVuZ2luZVJlYWRlciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7Im1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJxdWVyeS1yZWFsbXMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJxdWVyeS1jbGllbnRzIiwicXVlcnktdXNlcnMiLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZS1hZHAtYXV0aCByb2xlcyIsInNpZCI6IjI5YjI5YTgzLWJhYjEtNGY4NC05ZmY0LTFlMGZkM2JjNzE0OSIsInVwbiI6ImtjYWRtaW4ifQ.EudtPhUHqgYD1M1oDBok-JRBuaHxlbsgsJii3yHndS3MtYztNZkbZzM-e_1Q5El9YmqoD33CYXaGfvQ4V4sduYh6DpXO44ID93JnTzPxXbcVGWz1-C_U_2l7UtlGlA7SfYevsQnfQWXbIqRwpJz5QDJ3ufyDA2uphcTwbjRqAa444JBYiUZxWQr59BBjKQK-q7RktITBOUPBIZ33MjmR_9eXBmK_N3VT_ajgT291dws_-uV7XEvKsrQvUKwBoWbx9qx9Mrp8T7x5mvhkyrUYQe-HKU7fM7t-S7XuIn1wNzZp1LE2Pl5u-Wfd4aVlj3W3n-iKbAOxSKYmsaRNOGFP9Q"))
            .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        final ILoggingEvent loggedEvent = logList.get(0);
        final Map<String, String> mdcPropertyMap = loggedEvent.getMDCPropertyMap();

        assertEquals("404", mdcPropertyMap.get("resp_code"));
        assertEquals("log audit", mdcPropertyMap.get("facility"));
        assertEquals("kcadmin", mdcPropertyMap.get("subject"));
        assertEquals("79", mdcPropertyMap.get("resp_message"));
    }

    @Test
    public void verifyAuditLogSubjectIsMissingWhenTokenIsInvalid() throws Throwable {
        mvc.perform(MockMvcRequestBuilders
                .delete("/v3/apps/951b4e6a-d9dd-4df1-a8d8-f76e1862f492")
                .header("authorization", "Bearer IsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDRmVweVdhVjBGQjExYUhsalY3TDhyQ2l1RUlZZ"))
            .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        final ILoggingEvent loggedEvent = logList.get(0);
        final Map<String, String> mdcPropertyMap = loggedEvent.getMDCPropertyMap();

        assertEquals("404", mdcPropertyMap.get("resp_code"));
        assertEquals("log audit", mdcPropertyMap.get("facility"));
        assertEquals("n/av", mdcPropertyMap.get("subject"));
        assertEquals("79", mdcPropertyMap.get("resp_message"));
    }


    @Test
    public void verifyAuditLogSubjectIsPresentWhenPreferredUsernameIsSet() throws Throwable {
        mvc.perform(MockMvcRequestBuilders
            .delete("/v3/apps/951b4e6a-d9dd-4df1-a8d8-f76e1862f492")
            .header("authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3MHhHV1hGRFZrcTF5bHFxYm1rMV83ZVh1b1BmTnlDNTdRZlM3MnJFSVJRIn0.eyJleHAiOjE3MDU5NTg0ODEsImlhdCI6MTcwNTk1ODE4MSwianRpIjoiODU5YzU0ZTMtY2IxNS00NGZmLTllZTgtZWUyZTYzZWI1YWE2IiwiaXNzIjoiaHR0cHM6Ly9laWMuaGFsbDEzNC14MS5ld3MuZ2ljLmVyaWNzc29uLnNlL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6WyJtYXN0ZXItcmVhbG0iLCJhY2NvdW50Il0sInN1YiI6IjVmNWVkYzI0LWFjZGUtNDVmYy1iYjUwLWJhMDU5NTYxNGYzNiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVwcm9yb21BZG1pbiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNyZWF0ZS1yZWFsbSIsImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwiQXBwTWdyQWRtaW4iLCJVc2VyQWRtaW4iLCJBcHBNZ3JPcGVyYXRvciIsIm9mZmxpbmVfYWNjZXNzIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsIkdBU19Vc2VyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsibWFzdGVyLXJlYWxtIjp7InJvbGVzIjpbInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwidmlldy1yZWFsbSIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInF1ZXJ5LXJlYWxtcyIsInZpZXctYXV0aG9yaXphdGlvbiIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS11c2VycyIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwgcm9sZXMiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudEhvc3QiOiIxMC4xNTYuNzYuMjgiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtZXByb3JvbWFkbWluIiwiY2xpZW50QWRkcmVzcyI6IjEwLjE1Ni43Ni4yOCIsImNsaWVudF9pZCI6ImVwcm9yb21BZG1pbiJ9.N5L8XqC2gjQbWqpHwGRJF7CbDt66FfFXI1mXJUhlCoslRBtiFlLS4B02GEkDTUKLcMp80WBI50LF2B006kjU0PmgZ9JlJYlS_M24LYH1wZGYPCtb91zqfRmKdrk0d-VZwl4OHKzae9os2Ebr9QkKH7iavkEAHo-A1GRxUUqbiM1oHRs_EVgGycYPRrII-6Viz32Kalhp-ZNhDAPMmqAxrb78JdtTpIvOsNnToguGonNGPjlRHZ8e3gLnqRNPPbHU_3vF-MUSo2hpPRtNKeXm5N10Al9qMf9cirFzKcqLZv6j1RQdEBHeoQ0dUvmRxdeMJRo-PFi-k9KAKIYwIoj84w"))
                .andExpect(status().isNotFound());

        final List<ILoggingEvent> logList = listAppender.list;
        final ILoggingEvent loggedEvent = logList.get(0);
        final Map<String, String> mdcPropertyMap = loggedEvent.getMDCPropertyMap();

        assertEquals("404", mdcPropertyMap.get("resp_code"));
        assertEquals("log audit", mdcPropertyMap.get("facility"));
        assertEquals("service-account-eproromadmin", mdcPropertyMap.get("subject"));
        assertEquals("79", mdcPropertyMap.get("resp_message"));
        assertEquals("DELETE App {951b4e6a-d9dd-4df1-a8d8-f76e1862f492} DELETE http://localhost/v3/apps/951b4e6a-d9dd-4df1-a8d8-f76e1862f492", loggedEvent.getFormattedMessage());
    }
    @Test
    public void verifyAuditLogIsMissingForExcludedPattern() throws Throwable {

        mvc.perform(MockMvcRequestBuilders
                .get("/actuator"));

        final List<ILoggingEvent> logList = listAppender.list;
        assertEquals(0, logList.size());
    }

    @Test
    public void verifyAuditLogIsMissingWhenPropagated() throws Throwable {

        mvc.perform(MockMvcRequestBuilders
            .get("/app-lcm/v1/app-instances")
            .header("propagated", "onboarding"));

        final List<ILoggingEvent> logList = listAppender.list;
        assertEquals(0, logList.size());
    }
}