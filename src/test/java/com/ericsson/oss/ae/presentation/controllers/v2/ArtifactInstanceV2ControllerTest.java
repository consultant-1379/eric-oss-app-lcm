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

package com.ericsson.oss.ae.presentation.controllers.v2;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.v2.api.model.ArtifactInstanceV2Dto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, ArtifactInstanceV2Controller.class })
public class ArtifactInstanceV2ControllerTest {

    private static final String SQL_SCRIPT = "file:src/test/resources/sql/monitoring/monitoring_data.sql";

    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @MockBean
    private ArtifactInstanceRepository artifactInstanceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UrlGenerator urlGenerator;

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
    @Transactional
    @Sql(scripts = { SQL_SCRIPT })
    public void givenArtifactInstancesExistInRepository_WhenCallGetArtifactInstanceByArtifactIdAndInstanceIdEndpoint_ThenReturnHTTPStatus200()
            throws Exception {

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).appInstance(actualAppInstance).build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(artifactInstanceRepository.findByAppInstanceIdAndId(1L, 2L)).thenReturn(Optional.of(artifactInstance));
        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdV2Url(2L, 1L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @Sql(scripts = { SQL_SCRIPT })
    public void givenNoArtifactInstancesInRepository_WhenCallGetArtifactInstanceByArtifactIdAndInstanceIdEndpoint_ThenReturnHTTPStatus200()
            throws Exception {

        when(artifactInstanceRepository.findByAppInstanceIdAndId(1L, 1L)).thenReturn(Optional.empty());

        ArtifactInstanceV2Dto artifactInstanceV2Dto = new ArtifactInstanceV2Dto();
        artifactInstanceV2Dto.setAppLcmErrorCode(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND.getErrorCode());
        artifactInstanceV2Dto.setAppLcmErrorMessage(String.valueOf(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND.getErrorMessage()));
        artifactInstanceV2Dto.setDetail(String.format("Artifact Instance with artifactInstanceId = 1 belonging to appInstanceId = 1 was not found"));
        artifactInstanceV2Dto.setUrl(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdV2Url(1L, 1L));

        mvc.perform(get(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdV2Url(1L, 1L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail").value(artifactInstanceV2Dto.getDetail()))
                .andExpect(jsonPath("$.appLcmErrorCode").value(artifactInstanceV2Dto.getAppLcmErrorCode()))
                .andExpect(jsonPath("$.appLcmErrorMessage").value(artifactInstanceV2Dto.getAppLcmErrorMessage()))
                .andExpect(jsonPath("$.url").value(artifactInstanceV2Dto.getUrl()));
    }

    @Test
    @Transactional
    @Sql(scripts = { SQL_SCRIPT })
    public void givenArtifactInstancesExistInRepository_WhenCallGetArtifactInstanceByInstanceIdEndpoint_ThenReturnHTTPStatus200()
            throws Exception {

        final AppInstance actualAppInstance = AppInstance.builder().id(1L).appOnBoardingAppId(2L).targetStatus(TargetStatus.INSTANTIATED).build();
        actualAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);
        final ArtifactInstance artifactInstance = ArtifactInstance.builder().id(2L).appInstance(actualAppInstance).build();
        artifactInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(2L)).thenReturn(Optional.of(Collections.singletonList(artifactInstance)));
        mvc.perform(get(urlGenerator.generateArtifactInstancesByAppIdV2Url(2L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @Sql(scripts = { SQL_SCRIPT })
    public void givenNoArtifactInstancesInRepository_WhenCallGetArtifactInstanceByInstanceIdEndpoint_ThenReturnHTTPStatus200()
            throws Exception {

        when(artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(2L)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        mvc.perform(get(urlGenerator.generateArtifactInstancesByAppIdV2Url(2L)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
