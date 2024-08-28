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

package com.ericsson.oss.ae.acm.core.services.builders;

import static org.junit.jupiter.api.Assertions.*;

import static com.ericsson.oss.ae.acm.TestConstants.NAMESPACE_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.USER_DEFINED_HELM_PARAMETERS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = { AppComponentInstancePropertyBuilder.class })
class AppComponentInstancePropertyBuilderTest {
    @Autowired
    private AppComponentInstancePropertyBuilder appComponentInstancePropertyBuilder;
    private static final String APP_COMPONENT_TYPE = "MICROSERVICE";
    private static final String ANOTHER_APP_COMPONENT_TYPE = "NOT_MICROSERVICE";

    @Test
    public void shouldBuildInstanceProperties() {
        Map<String, Object> properties = appComponentInstancePropertyBuilder.buildInstanceProperties(APP_COMPONENT_TYPE,
                                                                                                     getCompositionElementProperties());
        assertTrue(properties.containsKey(TIMEOUT_KEY));
        assertTrue(properties.containsKey(NAMESPACE_KEY));
        assertTrue(properties.containsKey(USER_DEFINED_HELM_PARAMETERS));

        assertEquals(getCompositionElementProperties().get(TIMEOUT_KEY), properties.get(TIMEOUT_KEY));
        assertEquals(getCompositionElementProperties().get(NAMESPACE_KEY), properties.get(NAMESPACE_KEY));
        assertEquals(getCompositionElementProperties().get(USER_DEFINED_HELM_PARAMETERS), properties.get(USER_DEFINED_HELM_PARAMETERS));
    }

    @Test
    public void shouldBuildInstancePropertiesWithoutNsAndTimeout() {
        Map<String, Object> properties = appComponentInstancePropertyBuilder.buildInstanceProperties(APP_COMPONENT_TYPE,
                                                                                                     getCompositionElementPropertiesWithoutNsAndTimeout());
        assertFalse(properties.containsKey(TIMEOUT_KEY));
        assertFalse(properties.containsKey(NAMESPACE_KEY));
        assertTrue(properties.containsKey(USER_DEFINED_HELM_PARAMETERS));

        assertNotEquals(getCompositionElementProperties().get(TIMEOUT_KEY), properties.get(TIMEOUT_KEY));
        assertNotEquals(getCompositionElementProperties().get(NAMESPACE_KEY), properties.get(NAMESPACE_KEY));
        assertEquals(getCompositionElementProperties().get(USER_DEFINED_HELM_PARAMETERS), properties.get(USER_DEFINED_HELM_PARAMETERS));
    }

    @Test
    public void shouldReturnEmptyMap() {
        Map<String, Object> properties = appComponentInstancePropertyBuilder.buildInstanceProperties(ANOTHER_APP_COMPONENT_TYPE,
                                                                                                     getCompositionElementProperties());
        assertEquals(Collections.emptyMap(), properties);
    }

    private Map<String, Object> getCompositionElementProperties() {
        Map<String, Object> elementProperties = new HashMap<>();
        elementProperties.put(TIMEOUT_KEY, 5000);
        elementProperties.put(NAMESPACE_KEY, "test");
        elementProperties.put(USER_DEFINED_HELM_PARAMETERS, "{\"replicaCount\": 2 }");
        return elementProperties;
    }

    private Map<String, Object> getCompositionElementPropertiesWithoutNsAndTimeout() {
        Map<String, Object> elementProperties = new HashMap<>();
        elementProperties.put(USER_DEFINED_HELM_PARAMETERS, "{\"replicaCount\": 2 }");
        return elementProperties;
    }
}