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

package com.ericsson.oss.ae.constants.helmorchestrator;

import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;

/**
 * Constants for helm orchestrator.
 */
public final class HelmOrchestratorConstants {
    public static final String HELM_ORCHESTRATOR_CURRENT_VERSION = "v1";
    public static final String HELM_ORCHESTRATOR_CNWLCM = SLASH + "cnwlcm" + SLASH + HELM_ORCHESTRATOR_CURRENT_VERSION;
    public static final String HELM_ORCHESTRATOR_WORKLOAD_INSTANCES = "workload_instances";
    public static final String HELM_ORCHESTRATOR_OPERATIONS = "operations";
    public static final String HELM_ORCHESTRATOR_OPERATIONS_LOGS = "logs";
    public static final String HELM_SOURCE_FILE = "helmsource.tgz";
    public static final String HELM_VALUES_FILE = "values.yaml";

    //Error messages
    public static final String HELM_WORKLOAD_INSTANCE_ERROR = "Artifact workload instance ";

    private HelmOrchestratorConstants() {
    }
}