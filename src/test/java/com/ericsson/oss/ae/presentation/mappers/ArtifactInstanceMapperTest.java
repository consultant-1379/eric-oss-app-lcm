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

package com.ericsson.oss.ae.presentation.mappers;

import static com.ericsson.oss.ae.constants.AppLcmConstants.*;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_APPS_URL;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_ARTIFACTS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.Link;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.utils.UrlGenerator;

@SpringBootTest(classes = { ArtifactInstanceMapper.class, UrlGenerator.class })
public class ArtifactInstanceMapperTest {
    private static final ZonedDateTime CREATED_TIME = ZonedDateTime.now();
    private static final String APP_MANAGER_ROUTE_PATH = "http://localhost/app-manager/";

    @Autowired
    private ArtifactInstanceMapper artifactInstanceMapper;

    @Test
    public void givenValidArtifactInstanceObject_whenMappingArtifactInstanceToArtifactInstanceDto_thenArtifactInstanceDtoShouldContainCorrectValues() {
        final ArtifactInstance expectedArtifactInstance = ArtifactInstance.builder().id(1L).appOnBoardingArtifactId(123L)
                .workloadInstanceId("workLoadInstanceId").operationId("operationId").statusMessage("statusMessage")
                .appInstance(AppInstance.builder().appOnBoardingAppId(123L).id(1L).build()).build();
        expectedArtifactInstance.setCreatedTimestamp(CREATED_TIME);
        expectedArtifactInstance.setHealthStatus(HealthStatus.PENDING);

        final ArtifactInstanceDto actualArtifactInstanceDto = artifactInstanceMapper.map(expectedArtifactInstance, ArtifactInstanceDto.class);

        assertThat(actualArtifactInstanceDto.getArtifactId()).isEqualTo(123L);
        assertThat(actualArtifactInstanceDto.getArtifactInstanceId()).isEqualTo(1L);
        assertThat(actualArtifactInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.PENDING.toString());
        assertThat(actualArtifactInstanceDto.getStatusMessage()).isEqualTo("statusMessage");
        assertThat(actualArtifactInstanceDto.getCreatedTimestamp()).isEqualTo(CREATED_TIME.toString());
        assertThat(actualArtifactInstanceDto.getLinks()).isEqualTo(createLinksList(123L, 1L));
    }

    @Test
    public void givenValidArtifactInstanceDtoObject_whenMappingArtifactInstanceDtoToArtifactInstance_thenArtifactInstanceShouldContainCorrectValues() {
        final ArtifactInstance artifactInstance = artifactInstanceMapper.map(new ArtifactInstanceDto().artifactId(123L).artifactInstanceId(1L)
                .healthStatus(HealthStatus.PENDING.toString()).statusMessage("statusMessage"), ArtifactInstance.class);

        assertThat(artifactInstance.getAppOnBoardingArtifactId()).isEqualTo(123L);
        assertThat(artifactInstance.getId()).isEqualTo(1L);
        assertThat(artifactInstance.getHealthStatus()).isEqualTo(HealthStatus.PENDING);
        assertThat(artifactInstance.getStatusMessage()).isEqualTo("statusMessage");
    }

    private List<Link> createLinksList(final Long artifactId, final Long artifactInstanceId) {
        return Arrays.asList(new Link().rel(APP_INSTANCE).href(APP_MANAGER_ROUTE_PATH + "lcm" + APP_INSTANCES_URL + SLASH + artifactInstanceId),
                new Link().rel(ARTIFACT_INSTANCES)
                        .href(APP_MANAGER_ROUTE_PATH + "lcm" + APP_INSTANCES_URL + SLASH + artifactInstanceId + SLASH + ARTIFACT_INSTANCES),
                new Link().rel(SELF)
                        .href(APP_MANAGER_ROUTE_PATH + "lcm" + APP_INSTANCES_URL + SLASH + artifactInstanceId + SLASH + ARTIFACT_INSTANCES + SLASH
                                + artifactInstanceId),
                new Link().rel(APP).href(APP_MANAGER_ROUTE_PATH + "onboarding" + APP_ONBOARDING_APPS_URL + SLASH + artifactId),
                new Link().rel(ARTIFACTS).href(
                        APP_MANAGER_ROUTE_PATH + "onboarding" + APP_ONBOARDING_APPS_URL + SLASH + artifactId + SLASH + APP_ONBOARDING_ARTIFACTS));
    }

}
