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

package com.ericsson.oss.ae.presentation.controllers.v2;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.v2.api.model.AppInstanceV2Dto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_V2_URL;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, AppInstanceV2Controller.class })
public class AppInstanceApiV2ControllerTest {

    @MockBean
    private AppInstanceRepository repository;

    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    public void givenNoAppInstances_WhenCallGetAllAppInstancesEndpoint_ThenReturnHTTPStatus200() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        mvc.perform(get(APP_INSTANCES_V2_URL).contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void givenAppInstancesExistInRepository_WhenCallGetAllAppInstancesEndpoint_ThenReturnHTTPStatus200() throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findAll()).thenReturn(Collections.singletonList(actualAppInstance));
        mvc.perform(get(APP_INSTANCES_V2_URL).contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void givenNoAppInstance_WhenCallGetAppInstanceByIdEndpoint_ThenReturnHTTPStatus200() throws Exception {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        AppInstanceV2Dto appInstanceV2Dto = new AppInstanceV2Dto();
        appInstanceV2Dto.setAppLcmErrorCode(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode());
        appInstanceV2Dto.setAppLcmErrorMessage(String.valueOf(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage()));
        appInstanceV2Dto.setDetail(String.format("App Instance by appInstanceId = 2 was not found"));
        appInstanceV2Dto.setUrl(APP_INSTANCES_V2_URL);

        mvc.perform(get(APP_INSTANCES_V2_URL + "/2").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail").value(appInstanceV2Dto.getDetail()))
                .andExpect(jsonPath("$.appLcmErrorCode").value(appInstanceV2Dto.getAppLcmErrorCode()))
                .andExpect(jsonPath("$.appLcmErrorMessage").value(appInstanceV2Dto.getAppLcmErrorMessage()))
                .andExpect(jsonPath("$.url").value(appInstanceV2Dto.getUrl()));
    }

    @Test
    public void givenAppInstancesExistInRepository_WhenCallGetAppInstancesByIdEndpoint_ThenReturnHTTPStatus200() throws Exception {
        final AppInstance actualAppInstance = AppInstance.builder().id(3L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(repository.findById(3L)).thenReturn(Optional.of(actualAppInstance));
        mvc.perform(get(APP_INSTANCES_V2_URL + "/3").contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }
}
