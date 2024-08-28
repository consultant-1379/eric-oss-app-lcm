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

package com.ericsson.oss.ae.acm.presentation.mapper;

import static com.ericsson.oss.ae.acm.TestConstants.LCM_RESOURCE_PATH;
import static com.ericsson.oss.ae.acm.TestConstants.LCM_ROUTE_PATH;
import static com.ericsson.oss.ae.acm.TestConstants.LCM_ROUTE_PATH_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class LcmUrlGeneratorTest {

    private LcmUrlGenerator lcmUrlGeneratorUnderTest;

    @BeforeEach
    void setUp() {
        lcmUrlGeneratorUnderTest = new LcmUrlGenerator();
        ReflectionTestUtils.setField(lcmUrlGeneratorUnderTest, LCM_ROUTE_PATH, LCM_ROUTE_PATH_VALUE);
    }

    @Test
    void testGetAppsUrlById() {
        UUID uuid = UUID.randomUUID();
        assertThat(lcmUrlGeneratorUnderTest.getAppsUrlById(uuid)).isEqualTo(LCM_RESOURCE_PATH + "apps/" + uuid);
    }

    @Test
    void testGetAppsInstanceUrlById() {
        assertThat(lcmUrlGeneratorUnderTest.getAppsInstanceUrlById("1")).isEqualTo(LCM_RESOURCE_PATH + "app-instances/" + "1");
    }
}

