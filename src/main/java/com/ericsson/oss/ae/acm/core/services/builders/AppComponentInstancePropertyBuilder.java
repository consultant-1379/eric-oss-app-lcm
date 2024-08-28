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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.ae.acm.clients.acmr.common.AppComponentTypeComparator;
import org.springframework.stereotype.Component;

/**
 * Builds the property information for App Component Instances. Only properties that we agree
 * to expose to the App Manager user will be supported.
 */
@Component
public class AppComponentInstancePropertyBuilder {

    private static final String TIMEOUT_PROPERTY = "timeout";
    private static final String NAMESPACE_PROPERTY = "namespace";
    private static final String USER_DEFINED_HELM_PARAMETERS_PROPERTY = "userDefinedHelmParameters";
    private static final String DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID = "iamClientId";
    private static final String DATA_MANAGEMENT_PROPERTY_ARTIFACTS = "artifacts";
    private static final String DATA_MANAGEMENT_PROPERTY_ARTIFACT_NAME = "name";

    /**
     * Extract only the supported properties for the given Type from the Composition Instance properties and return in a Map.
     *
     * @param appComponentType type of the App Component
     * @param compositionElementProperties contains all properties returned from ACM
     * @return a Map with supported properties for the Component Type
     */
    public Map<String, Object> buildInstanceProperties(final String appComponentType, final Map<String, Object> compositionElementProperties) {
         if (AppComponentTypeComparator.isAsdType(appComponentType)) {
             return buildPropertyDataForTypeMicroservice(compositionElementProperties);
         } else if(AppComponentTypeComparator.isDataManagementType(appComponentType)){
             return buildPropertyDataForTypeDataManagement(compositionElementProperties);
         }
         // When new App Components are supported in App Manager add the logic here to build their supported properties
         return Collections.emptyMap();
    }

    private Map<String, Object> buildPropertyDataForTypeMicroservice(final Map<String, Object> compositionElementProperties) {
        final Map<String, Object> properties = new HashMap<>();

        if (compositionElementProperties.containsKey(TIMEOUT_PROPERTY)) {
            properties.put(TIMEOUT_PROPERTY, compositionElementProperties.get(TIMEOUT_PROPERTY));
        }

        if (compositionElementProperties.containsKey(NAMESPACE_PROPERTY)) {
            properties.put(NAMESPACE_PROPERTY, compositionElementProperties.get(NAMESPACE_PROPERTY));
        }

        if (compositionElementProperties.containsKey(USER_DEFINED_HELM_PARAMETERS_PROPERTY)) {
            properties.put(USER_DEFINED_HELM_PARAMETERS_PROPERTY, compositionElementProperties.get(USER_DEFINED_HELM_PARAMETERS_PROPERTY));
        }
        return properties;
    }

    private Map<String, Object> buildPropertyDataForTypeDataManagement(final Map<String, Object> compositionElementProperties) {
        final Map<String, Object> properties = new HashMap<>();

        if(compositionElementProperties.containsKey(DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID)){
            properties.put(DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID, compositionElementProperties.get(DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID));
        }

        if(compositionElementProperties.containsKey(DATA_MANAGEMENT_PROPERTY_ARTIFACTS)){
            final List<Map<String, Object>> artifacts = (List<Map<String, Object>>) compositionElementProperties.get(DATA_MANAGEMENT_PROPERTY_ARTIFACTS);
            List<Map<String, String>> artifactNames = extractArtifactNames(artifacts);
            if(!artifactNames.isEmpty()){
                properties.put(DATA_MANAGEMENT_PROPERTY_ARTIFACTS, artifactNames);
            }
        }
        return properties;
    }

    private List<Map<String, String>> extractArtifactNames(final List<Map<String, Object>> artifacts) {
        final List<Map<String, String>> artifactNames = new ArrayList<>();
        for (Map<String, Object> artifact : artifacts) {
            final Object nameObject = artifact.get(DATA_MANAGEMENT_PROPERTY_ARTIFACT_NAME);
            if (nameObject instanceof String) {
                final String name = (String) artifact.get(DATA_MANAGEMENT_PROPERTY_ARTIFACT_NAME);
                artifactNames.add(Map.of(DATA_MANAGEMENT_PROPERTY_ARTIFACT_NAME, name));
            }
        }
        return artifactNames;
    }
}
