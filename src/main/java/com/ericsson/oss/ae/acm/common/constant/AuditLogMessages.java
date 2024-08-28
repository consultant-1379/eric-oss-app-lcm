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
package com.ericsson.oss.ae.acm.common.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditLogMessages {
    // V1 Keywords
    public static final String V1_DELETE_APP_INSTANCES_KEYWORD = "DELETED requested App Instances of App {%s}";
    public static final String V1_DELETE_ALL_APP_INSTANCES_KEYWORD = "DELETE App {%s} and all App Instances";
    public static final String V1_TERMINATE_APP_INSTANCE_KEYWORD = "TERMINATE App Instance {%s}";
    public static final String V1_INSTANTIATE_APP_INSTANCE_KEYWORD = "INSTANTIATE App Instance {%s}";
    public static final String V1_UPDATE_APP_INSTANCE_KEYWORD = "UPDATE App Instance  {%s}";

    // V3 Keywords
    public static final String V3_INITIALIZE_APP_KEYWORD = "INITIALIZE App {%s}";
    public static final String V3_DEINITIALIZE_APP_KEYWORD = "DEINITIALIZE App {%s}";
    public static final String V3_ENABLE_APP_KEYWORD = "ENABLE App {%s}";
    public static final String V3_DISABLE_APP_KEYWORD = "DISABLE App {%s}";
    public static final String V3_DELETE_APP_KEYWORD = "DELETE App {%s}";
    public static final String V3_CREATE_APP_INSTANCE_KEYWORD = "CREATE App Instance {%s}";
    public static final String V3_DEPLOY_APP_INSTANCE_KEYWORD = "DEPLOY App Instance {%s}";
    public static final String V3_UNDEPLOY_APP_INSTANCE_KEYWORD = "UNDEPLOY App Instance {%s}";
    public static final String V3_UPGRADE_APP_INSTANCE_KEYWORD = "UPGRADE App Instance {%s}";
    public static final String V3_UPDATE_APP_INSTANCE_KEYWORD = "UPDATE App Instance {%s}";
    public static final String V3_DELETE_APP_INSTANCE_KEYWORD = "DELETE App Instance {%s}";
}
