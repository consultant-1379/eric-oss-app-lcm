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

package com.ericsson.oss.ae.constants;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.google.common.collect.ImmutableList;

/**
 * Constants used in App Lcm.
 */
public final class AppLcmConstants {

    public static final char SLASH = '/';
    public static final String DOT_AND_SPACE = ". ";
    public static final String APP = "app";
    public static final String APPS = "apps";
    public static final String APP_INSTANCE = "app-instance";
    public static final String APP_INSTANCES = "app-instances";
    public static final String ARTIFACTS = "artifacts";
    public static final String ARTIFACT_INSTANCES = "artifact-instances";
    public static final String SELF = "self";
    public static final char COLON = ':';
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String APP_LCM = "app-lcm";
    public static final String CURRENT_VERSION = "v1";
    public static final String APP_LCM_URL = SLASH + APP_LCM + SLASH + CURRENT_VERSION;

    public static final String APP_LCM_URL_V2 = SLASH + APP_LCM + SLASH + "v2";
    public static final String APP_INSTANCES_URL = APP_LCM_URL + SLASH + APP_INSTANCES;

    public static final String APP_INSTANCES_V2_URL = APP_LCM_URL_V2 + SLASH + APP_INSTANCES;
    public static final String APPS_URL = APP_LCM_URL + SLASH + APPS + SLASH;
    public static final String APP_LCM_DELETE_PATH = SLASH + APP_LCM + SLASH + CURRENT_VERSION + SLASH + APPS + SLASH;
    public static final String STATUS = "status";
    public static final String DELETING = "deleting";
    private static final List<HealthStatus> UNHEALTHY_STATUS_LIST = Arrays.asList(HealthStatus.TERMINATED, HealthStatus.FAILED,
                                                                                 HealthStatus.DELETED);
    public static final String FAILED_TO_DELETE_MESSAGE = "No App Instances were found for appId: ";
    public static final String TRACING_ENDPOINT = "ericsson.tracing.exporter.endpoint";
    public static final String TRACING_JAEGER_ENDPOINT = "ericsson.tracing.sampler.jaeger-remote.endpoint";
    public static final String TRACING_ENDPOINT_GRPC = "http://eric-dst-collector:4317";
    public static final String TRACING_ENDPOINT_HTTP = "http://eric-dst-collector:4318";
    public static final String TRACING_JAEGER_ENDPOINT_GRPC = "http://eric-dst-collector:14250";
    public static final String TRACING_POLING = "ericsson.tracing.polingInterval";
    public static final String TRACING_POLING_INTERVAL = "30";

    private AppLcmConstants() {
    }

    public static List<HealthStatus> getUnhealthyStatusList() {
        return ImmutableList.copyOf(UNHEALTHY_STATUS_LIST);
    }
}