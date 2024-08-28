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

package com.ericsson.oss.ae.api.contracts;

import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_ARTIFACTS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.oss.ae.presentation.controllers.ArtifactInstanceController;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.presentation.services.artifactinstance.ArtifactInstanceService;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

public class GetAllArtifactInstancesNegativeBase {
    @InjectMocks
    private ArtifactInstanceController controller;

    @Mock
    private ArtifactInstanceService service;

    @Mock
    private ArtifactInstanceRepository repository;

    @BeforeEach
    public void setup() {
        given(service.getAllArtifactInstances(anyLong())).willThrow(new ResourceNotFoundException(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND,
                String.format("Artifact Instances belonging to appInstanceId = %s were not found", anyLong()), SLASH + APP_ONBOARDING_ARTIFACTS));
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(controller, service, repository);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

}
