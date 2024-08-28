/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.api.model.AppInstanceListRequestDto;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static com.ericsson.oss.ae.constants.AppLcmConstants.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, AppInstanceController.class })
public class AppInstanceApiControllerDeleteTest {

    private MockMvc mvc;
    @MockBean
    private AppInstanceService appInstanceService;
    @MockBean
    private HelmOrchestratorClient helmOrchestratorClient;
    @MockBean
    private AppInstanceRepository repository;
    @MockBean
    private ArtifactInstanceRepository artifactInstanceRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;


    @BeforeAll
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void givenTerminatedAllAppInstancesExistsInRepository_WhenCallDeleteMultipleAppInstancesByAppIdAndAppInstancesList_ThenReturnNoContent_AllInstancesDeleted()
        throws Exception{
        final Long appId = 1L;
        final Long appInstanceId = 1L;

        final AppInstanceListRequestDto appInstanceListRequestDto = new AppInstanceListRequestDto();
        appInstanceListRequestDto.setAppInstanceId(Arrays.asList(appInstanceId));

        doNothing().when(appInstanceService).deleteAppInstances(appId, appInstanceListRequestDto);
        mvc.perform(delete(APPS_URL + appId + SLASH + APP_INSTANCES)
                        .content(objectMapper.writeValueAsString(appInstanceListRequestDto))
                        .contentType(APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    void givenValidRequestToDeleteApp_WhenCallDeleteApplicationByAppId_ThenReturnNoContent_AppDeleted() throws Exception{
        final Long appId = 1L;

        doNothing().when(appInstanceService).deleteApp(appId);

        mvc.perform(delete(APPS_URL + appId).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }
}
