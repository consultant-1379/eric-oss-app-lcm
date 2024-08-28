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

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.Link;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.utils.UrlGenerator;

@SpringBootTest(classes = { AppInstanceMapper.class, UrlGenerator.class })
class AppInstanceMapperTest {
    private static final ZonedDateTime CREATED_TIMESTAMP = ZonedDateTime.now();
    private static final String APP_MANAGER_ROUTE_PATH = "http://localhost/app-manager/";

    @Autowired
    private AppInstanceMapper mapper;

    @Test
    public void givenValidAppInstance_whenMappingAppInstanceToAppInstanceDto_thenAppInstanceDtoShouldContainCorrectValues() {
        final AppInstance expectedAppInstance = AppInstance.builder().appOnBoardingAppId(1L).id(23L).targetStatus(TargetStatus.INSTANTIATED).build();
        expectedAppInstance.setCreatedTimestamp(CREATED_TIMESTAMP);
        expectedAppInstance.setHealthStatus(HealthStatus.INSTANTIATED);

        final AppInstanceDto actualAppInstanceDto = mapper.map(expectedAppInstance, AppInstanceDto.class);

        assertThat(actualAppInstanceDto.getAppOnBoardingAppId()).isEqualTo(1L);
        assertThat(actualAppInstanceDto.getId()).isEqualTo(23L);
        assertThat(actualAppInstanceDto.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getHealthStatus()).isEqualTo(HealthStatus.INSTANTIATED.toString());
        assertThat(actualAppInstanceDto.getCreatedTimestamp()).isEqualTo(CREATED_TIMESTAMP.toString());

        assertThat(actualAppInstanceDto.getLinks()).isEqualTo(createLinksList(1L, 23L));
    }

    @Test
    public void givenValidAppInstanceDto_whenMappingAppInstanceDtoToAppInstance_thenAppInstanceShouldContainCorrectValues() {
        final AppInstance actualAppInstance = mapper.map(new AppInstanceDto().id(1L).appOnBoardingAppId(23L)
                .targetStatus(TargetStatus.INSTANTIATED.toString()).healthStatus(HealthStatus.INSTANTIATED.toString()), AppInstance.class);

        assertThat(actualAppInstance.getAppOnBoardingAppId()).isEqualTo(23L);
        assertThat(actualAppInstance.getId()).isEqualTo(1L);
        assertThat(actualAppInstance.getTargetStatus()).isEqualTo(TargetStatus.INSTANTIATED);
        assertThat(actualAppInstance.getHealthStatus()).isEqualTo(HealthStatus.INSTANTIATED);
    }

    private List<Link> createLinksList(final Long appId, final Long appInstanceId) {
        return Arrays.asList(new Link().rel(SELF).href(APP_MANAGER_ROUTE_PATH + "lcm" + APP_INSTANCES_URL + SLASH + appInstanceId),
                new Link().rel(ARTIFACT_INSTANCES)
                        .href(APP_MANAGER_ROUTE_PATH + "lcm" + APP_INSTANCES_URL + SLASH + appInstanceId + SLASH + ARTIFACT_INSTANCES),
                new Link().rel(APP).href(APP_MANAGER_ROUTE_PATH + "onboarding" + APP_ONBOARDING_APPS_URL + SLASH + appId), new Link().rel(ARTIFACTS)
                        .href(APP_MANAGER_ROUTE_PATH + "onboarding" + APP_ONBOARDING_APPS_URL + SLASH + appId + SLASH + APP_ONBOARDING_ARTIFACTS));
    }
}
