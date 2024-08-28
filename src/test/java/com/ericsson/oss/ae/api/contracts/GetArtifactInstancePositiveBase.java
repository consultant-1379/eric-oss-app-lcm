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

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.presentation.controllers.ArtifactInstanceController;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.ae.presentation.services.artifactinstance.ArtifactInstanceService;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class GetArtifactInstancePositiveBase {

    @InjectMocks
    private ArtifactInstanceController controller;

    @Mock
    private ArtifactInstanceService service;

    @Mock
    private ArtifactInstanceRepository repository;

    @BeforeEach
    public void setup() {
        given(service.getArtifactInstance(1L, 1L)).willReturn(getArtifactInstance());
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(controller, service, repository);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

    private ArtifactInstanceDto getArtifactInstance() {
        ArtifactInstanceDto result = new ArtifactInstanceDto();
        result.setArtifactInstanceId(1L);
        result.artifactId(123L);
        result.setHealthStatus(HealthStatus.PENDING.name());
        result.setCreatedTimestamp( "2021-08-19 19:10:25-07");
        return result;
    }
}
