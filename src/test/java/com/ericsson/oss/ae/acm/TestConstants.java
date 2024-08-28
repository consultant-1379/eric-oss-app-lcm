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

package com.ericsson.oss.ae.acm;

import java.util.UUID;

/**
 * Class for test case constants.
 */
public class TestConstants {

    public static final String APP_ENTITY_NAME = "eric-oss-hello-world-multiple-microservices-go-app";
    public static final String APP_ENTITY_HELLO_WORLD = "eric-oss-hello-world-go-app";
    public static final String HELLO_WORLD_GO_APP_1 = "eric-oss-hello-world-go-app-1";
    public static final String HELLO_WORLD_GO_APP_3 = "eric-oss-hello-world-go-app-3";
    public static final String HELLO_WORLD_APP = "hello_world_app";
    public static final String HELLO_WORLD_METRICS_GO_APP_COMPONENT_INSTANCE = "eric-oss-hello-metrics-go-app";
    public static final String HELLO_WORLD = "hello_world";
    public static final String APP_ENTITY_VERSION = "1.0.0";
    public static final String APP_ENTITY_MODE_DISABLED = "DISABLED";
    public static final String APP_ENTITY_MODE_ENABLED = "ENABLED";
    public static final String APP_ENTITY_STATUS_CREATED = "CREATED";
    public static final String APP_ENTITY_TYPE = "rApp";
    public static final String APP_PROVIDER = "Ericsson";
    public static final String APP_VERSION_1_1_1 = "1.1.1";
    public static final String VERSION_2_0_0 = "2.0.0";
    public static final String DATA_MANAGEMENT_COMPONENT_NAME = "data-management";
    public static final String DATA_MANAGEMENT_COMPONENT_VERSION = "1.0.0-1";
    public static final String DATA_MANAGEMENT_COMPONENT_HIGHER_VERSION = "1.1.1-0";
    public static final String IDS_FILE = "input-data-specification.json";

    // App Instance Constants
    public static final String APP_INSTANCE_PROPERTIES = "{\"timeout\": 5000, \"userDefinedHelmParameters\": {\"replicaCount\": 2 }}";
    public static final String APP_INSTANCE_NAME = "eric-oss-5gcnr";
    public static final String DEPLOY_ACTION = "deployment-actions";
    public static final UUID COMPOSITION_ID = UUID.fromString("562ed027-2689-481a-b3a5-e284b1fbc33f");

    // Artifact Constants
    public static final String ARTIFACT_VERSION = "1.0.0";

    //Component Properties
    public static final String USER_DEFINED_HELM_PARAMETERS = "userDefinedHelmParameters";
    public static final String TIMEOUT_KEY = "timeout";
    public static final String NAMESPACE_KEY = "namespace";
    public static final String NAMESPACE_VALUE = "hart071-eric-eic-5";
    public static final String MICROSERVICE = "Microservice";
    public static final String DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID = "iamClientId";
    public static final String DATA_MANAGEMENT_PROPERTY_ARTIFACTS = "artifacts";

    // Test constants for additionalParameters.
    public static final String REPLICA_COUNT_KEY = "replicaCount";
    public static final int REPLICA_COUNT_VALUE = 2;
    public static final String DEPLOY_APP_INSTANCE_ACTION = "DEPLOY";
    public static final String UNDEPLOY_APP_INSTANCE_ACTION = "UNDEPLOY";

    public static final String UPGRADE_APP_INSTANCE_ACTION = "UPGRADE";

    public static final String COMPOSITION_INSTANCE_STATUS_DEPLOYED = "DEPLOYED";
    public static  final String COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_NAME = "CloudNativeAcInstance-1";
    public static  final String COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_VERSION = "1.0.0";

    // Errors
    public static final int HTTP_500 = 500;
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String BAD_REQUEST = "Bad Request";
    public static final int HTTP_400 = 400;
    public static final String VALIDATION_ERROR = "Validation Error";
    public static final String VALIDATION_MISSING_APP_NAME_MESSAGE = "Create app request validation failed due to Name must not be Null or empty.";
    public static final String ERROR_WHILE_COMMISSIONING_AC_DEFINITION_IN_ACM_R = "Error while commissioning AC definition in ACM-R";
    public static final String AUTOMATION_COMPOSITION_DEFINITION_COMMISSION_ERROR = "Automation Composition Definition Commission Error";
    public static final String TEST_CODE_REQUESTED_APP_NOT_IN_DB = "Test code: Requested App Not in DB";
    public static final String UPDATE_INSTANCE_FAILED = "Update instance failed";

    // Permissions
    public static final String GLOBAL = "GLOBAL";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String CLIENT_ID = "client-id";

    // Test constants for KeyCloak.
    public static final String KEYCLOAK_CLIENT_ID_KEY = "keycloakClientId";
    public static final String KEYCLOAK_CLIENT_SECRET_KEY = "keycloakClientSecret";
    public static final String KEYCLOAK_CLIENT_URL_KEY = "keycloakClientUrl";
    public static final String KEYCLOAK_SCOPE_KEY = "scope";
    public static final String HTTP_SEC_ACCESS_MGMT = "http://eric-sec-access-mgmt-http:8080";

    // Query Constants
    public static final String PATH_PARAM_START = "?";
    public static final String AMP = "&";
    public static final String COMMA = ",";
    public static final String GET_APPS_NAME_QUERY_FILTER = "name=";
    public static final String GET_APPS_STATUS_QUERY_FILTER = "status=";
    public static final String GET_APPS_MODE_QUERY_FILTER = "mode=";
    public static final String GET_APPS_TYPE_QUERY_FILTER = "type=";
    public static final String GET_APPS_VERSION_QUERY_FILTER = "version=";

    // LCM Constants
    public static final String LCM_HOST_NAME = "appLcmHostname";
    public static final String LCM_PORT = "appLcmPort";
    public static final String LCM_PARTICIPANT = "lcm-participant";
    public static final String LCM_ROUTE_PATH = "appManagerAppLcmRoutePath";
    public static final String PORT_VALUE = "8080";
    public static final String LCM_HOST_NAME_VALUE = "eric-oss-app-lcm";
    public static final String LCM_ROUTE_PATH_VALUE = "/app-manager/lcm";
    public static final String LCM_RESOURCE_PATH = "/app-lifecycle-management/v3/";
    public static final UUID ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED = UUID.fromString("fac50cde-11a2-4915-a49c-709762714a5d");
    public static final UUID ACM_COMPOSITION_INSTANCE_ID_DEPLOYED = UUID.fromString("caf50cde-11a2-4915-a49c-609762714a6f");
    public static final UUID APP_COMPONENT_INSTANCE_ID_1_UNDEPLOYED = UUID.fromString("951b4e6a-d9dd-4df1-a8d8-f76e1862f492");
    public static final UUID APP_COMPONENT_INSTANCE_ID_1_DEPLOYED = UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c23");
    public static final UUID APP_COMPONENT_INSTANCE_ID_2_DEPLOYED = UUID.fromString("709c62b3-8918-41b9-a747-d21eb79c6c24");

    // String Values from which we generate UUID Values
    public static final String EEFD_0035 = "7e151de6-18a9-4770-be4f-354b620f0035";
    public static final String EEFD_22230 = "26471a81-1de4-4ad9-9724-326eefd22230";
    public static final String EEFD_22231 = "26471a81-1de4-4ad9-9724-326eefd22231";
    public static final String EEFD_22232 = "26471a81-1de4-4ad9-9724-326eefd22232";
    public static final String EEFD_22233 = "26471a81-1de4-4ad9-9724-326eefd22233";
    public static final String EEFD_22234 = "26471a81-1de4-4ad9-9724-326eefd22234";
    public static final String EEFD_6C8CC = "cde33b59-07d2-4977-8f65-68956e46c8cc";
    public static final String ELEMENT_INSTANCE_DATA_MANAGEMENT = "efe33b59-07d2-5077-8f65-68956e46c8dd";
    public static final String ACM_C84D = "8ebd7b1b-34ea-4a10-9e91-d3c49b98c84d";
    public static final String ACM_616E = "4a2db724-21f8-4fc3-9fa6-2f9d96b3616e";

    // JSON Path constants
    public static final String AUTOMATION_COMPOSITION_DEFINITION_PRIMING_JSON = "expectedresponses/acm/AutomationCompositionDefinition_PRIMING.json";
    public static final String AUTOMATION_COMPOSITION_DEFINITION_INVALID_BODY_JSON = "expectedresponses/acm/AutomationCompositionDefinition_InvalidBody.json";
    public static final String AUTOMATION_COMPOSITION_DEFINITION_JSON = "expectedresponses/acm/AutomationCompositionDefinition.json";
    public static final String AUTOMATION_COMPOSITION_DEFINITION_PRIMED_JSON = "expectedresponses/acm/AutomationCompositionDefinition_PRIMED.json";
    public static final String ACM_ERROR_DETAILS_JSON = "expectedresponses/acm/AcmErrorDetails.json";
    public static final String DELETE_AUTOMATION_COMPOSITION_INSTANCE_JSON = "expectedresponses/acm/DeleteAutomationCompositionInstance.json";
    public static final String UPDATE_REQUEST_JSON = "requestproperty/acm/updateRequestProperty.json";

    // Miscellaneous
    public static final String ID_1 = "1";
    public static final String HELM = "HELM";
    public static final String IMAGE = "IMAGE";
    public static final String OPAQUE = "OPAQUE";
    public static final String REALM = "realm";
    public static final String DOCKER = "docker";
    public static final String DOCKER_TAR = "docker.tar";
    public static final String NGINX = "nginx";
    public static final String FOO = "foo";
    public static final String ERICSSON_ID = "Ericsson";
    public static final String SCOPE = "scope";
    public static final String POLICY = "policy";
    public static final String FILE = "file";
    public static final String TEST_STRING = "test";
    public static final int TIMEOUT_3000 = 3000;
    public static final String LOCAL_HOSTNAME = "localhost";
    public static final String OBJECT_STORE_ACCESS_KEY = "object-store-user";
    public static final String OBJECT_STORE_SECRET_KEY = "object-store-password";
    public static final String BUCKET_NAME = "app-management";
    public  static final String TEST_ONBOARDING_JOB_ID = "9cc1047a-5aae-4630-893a-1536392cbd2b";
}
