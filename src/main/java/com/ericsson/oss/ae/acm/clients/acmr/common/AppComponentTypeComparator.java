/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
package com.ericsson.oss.ae.acm.clients.acmr.common;

import static com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType.ASD;
import static com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType.DATAMANAGEMENT;
import static com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType.MICROSERVICE;

import java.util.Locale;

/**
 * Checks component type string values against the valid supported types in LCM.
 */
public class AppComponentTypeComparator {
    private AppComponentTypeComparator(){

    }
    public static boolean isAsdType(final String componentType) {
        final String type = componentType.toUpperCase(Locale.ROOT);
        return (type.equals(MICROSERVICE.name()) || type.equals(ASD.name()));
    }

    public static boolean isDataManagementType(final String componentType) {
        final String type = componentType.toUpperCase(Locale.ROOT);
        return (type.equals(DATAMANAGEMENT.name()));
    }
}
