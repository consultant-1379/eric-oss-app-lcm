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

package com.ericsson.oss.ae.acm.common.constant;

import freemarker.template.Configuration;
import freemarker.template.Version;
import lombok.experimental.UtilityClass;

/**
 * Class for App LCM constants.
 */
@UtilityClass
public class AppLcmConstants {
    // App LCM REST path constants.
    public static final String SLASH = "/";
    public static final String APP_LCM = "app-lifecycle-management";
    public static final String CURRENT_VERSION = "v3";
    public static final String APPS = "/apps";
    public static final String APP_INSTANCES = "/app-instances";
    public static final String APP_LCM_RESOURCE_PATH_V3 = SLASH + APP_LCM + SLASH + CURRENT_VERSION;
    public static final String APP_ID_PARENTHESIS = "{appId}";
    public static final String APP_ID = "appId";
    public static final String INSTANCE_ID = "{instanceId}";
    public static final String APPS_APP_ID = APPS + SLASH + APP_ID_PARENTHESIS;
    public static final String APP_INSTANCES_INSTANCE_ID = APP_INSTANCES + SLASH + INSTANCE_ID;
    public static final String APP_INSTANCES_COMPONENT_INSTANCE = "/component-instances";

    // File extension constants.
    public static final String FTL_FILE_EXTENSION = ".ftl";
    public static final String YAML_FILE_EXTENSION = ".yaml";

    // ACM file generation constants.
    public static final String PATH_TO_FREEMARKER_TEMPLATES_DIRECTORY = "templates";
    public static final String TOSCA_SERVICE_TEMPLATE_FILE_NAME = "ToscaServiceTemplate";
    public static final String AC_INSTANCE_PROPERTIES_FILE_NAME = "AutomationCompositionInstanceProperties";
    public static final String ACM_GENERATION_OBJECT_KEY = "acmGenerationObject";
    public static final Version ACM_FILE_GENERATOR_VERSION = Configuration.VERSION_2_3_31;
    public static final String OBJECT_MAPPER_SHARED_VARIABLE = "ObjectMapper";
    public static final int UUID_FIELD_LENGTH = 36;
    public static final String DB_ACM_SCHEMA = "acm_schema";

    // App ACM Model constants
    public static final String APP_INSTANCE_REPLICA_COUNT_KEY = "replicaCount";
    public static final int APP_INSTANCE_REPLICA_COUNT_VALUE = 2;
    public static final String AUTOMATION_COMPOSITION_ELEMENT = "com.ericsson.oss.app.mgr.ac.element.AppLcmMicroserviceAutomationCompositionElement";
    public static final String AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT = "com.ericsson.oss.app.mgr.ac.element.DataManagementAutomationCompositionElement";

    // Component Type
    public static final String MICROSERVICE = "Microservice";
    public static final String DATAMANAGEMENT = "DataManagement";

    // ACM API constants
    public static final char COMMA = ',';
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String ACM_BASE_URL = "/onap/policy/clamp/acm/v2";
    public static final String ACM_COMPOSITION = "compositions";

    // Create App Request constants,
    public static final String CREATE_APP_REQUEST_NULL_APP_COMPONENT_ERR_MESSAGE = "App Component must be provided";
    public static final String CREATE_APP_REQUEST_NULL_ARTIFACT_ERR_MESSAGE = "Artifact must be provided";
    public static final String CREATE_APP_REQUEST_MISSING_APP_NAME_ERR_MESSAGE = "Name must not be Null or empty";
    public static final String CREATE_APP_REQUEST_MISSING_APP_VERSION_ERR_MESSAGE = "Version must not be Null or empty";
    public static final String CREATE_APP_REQUEST_INVALID_APP_VERSION_ERR_MESSAGE = "The App Component version provided must follow the Semantic Versioning scheme";
    public static final String CREATE_APP_REQUEST_MISSING_APP_TYPE_ERR_MESSAGE = "Type must not be Null or empty";
    public static final String CREATE_APP_REQUEST_MISSING_APP_PROVIDER_ERR_MESSAGE = "Provider must not be Null or empty";
    public static final String CREATE_APP_REQUEST_MISSING_APP_COMPONENT_NAME_ERR_MESSAGE = "App Component name must not be Null or empty";
    public static final String CREATE_APP_REQUEST_MISSING_APP_COMPONENT_VERSION_ERR_MESSAGE = "App Component version must not be Null or empty";
    public static final String CREATE_APP_REQUEST_INVALID_APP_COMPONENT_VERSION_ERR_MESSAGE = "App Component version must be semantic, semantic version of 2.0.0 is referenced";
    public static final String CREATE_APP_REQUEST_MISSING_APP_COMPONENT_TYPE_ERR_MESSAGE = "App Component type must not be Null or empty";
    public static final String CREATE_APP_REQUEST_APP_COMPONENT_TYPE_UNSUPPORTED_ERR_MESSAGE = "The current supported App Component Types are ASD, Microservice and DataManagement";
    public static final String CREATE_APP_REQUEST_ARTIFACT_MISSING_NAME_ERR_MESSAGE = "Artifact Name must not be Null or empty";
    public static final String CREATE_APP_REQUEST_ARTIFACT_MISSING_TYPE_ERR_MESSAGE = "Artifact Type must not be Null or empty";
    public static final String CREATE_APP_REQUEST_ARTIFACT_MISSING_LOCATION_ERR_MESSAGE = "Artifact Location must not be Null or empty";
    public static final String CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE = "Artifact location needs to consist of the bucket name and location that are separated by the separator '/'";

    // User messages
    public static final String AC_RUNTIME_ERROR_TITLE = "AC Runtime request failure";
    public static final String APP_ACTION_DEPLOY_ERROR_TITLE = "Deploy App Action request failure";
    public static final String APP_ACTION_UPDATE_ERROR_TITLE = "Update App Action request failure";
    public static final String APP_ACTION_INITIALIZE_ERROR_TITLE = "Initialize App Action request failure";
    public static final String APP_ACTION_DEINITIALIZE_ERROR_TITLE = "De-Initialize App Action request failure";
    public static final String APP_ACTION_UPGRADE_ERROR_TITLE = "Upgrade App Action request failure";
    public static final String APP_ACTION_DELETE_ERROR_TITLE = "Delete App Action failure";
    public static final String APP_ACTION_INSTANTIATE_ERROR_TITLE = "Instantiate App Action failure";

    public static final String ACM_INSTANCE = "instances";
    public static final String NAMESPACE = "namespace";
    public static final String TIMEOUT = "timeout";
    public static final int DEFAULT_TIMEOUT_VALUE = 5;
    public static final String DEPLOY = "deploy";
    public static final String KEYCLOAK_CLIENT_ID = "admin-cli";
    public static final String KEYCLOAK_REALM_MASTER = "master";
    public static final String KEYCLOAK_GRANT_TYPE_PASSWORD = "password";
    public static final String KEYCLOAK_CLIENT_AUTHENTICATOR_TYPE = "client-secret";
    public static final String KEYCLOAK_PROTOCOL = "openid-connect";
    public static final String KAFKA = "kafka";
    public static final String AUTH = "auth";
    public static final String REALMS = "realms";
    public static final String PROTOCOLS = "protocol";
    public static final String TOKEN = "token";
    public static final String ADMIN = "admin";
    public static final String KEYCLOAK_REALMS_PATH = SLASH + AUTH + SLASH + ADMIN + SLASH + REALMS + SLASH;
    public static final String BDR = "bdr";
    public static final String CLIENTS = "clients";
    public static final String USERS = "users";
    public static final String ROLES = "roles";
    public static final String CLIENT_SECRET = "client-secret";
    public static final String CLIENT_SCOPES = "client-scopes";
    public static final String SERVICE_ACCOUNT_USER = "service-account-user";
    public static final String ROLE_MAPPING_REALM = "/role-mappings/realm";
}