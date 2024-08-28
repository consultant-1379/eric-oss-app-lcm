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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AC_RUNTIME_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_DEINITIALIZE_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_DELETE_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_DEPLOY_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_INITIALIZE_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_INSTANTIATE_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_UPDATE_ERROR_TITLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ACTION_UPGRADE_ERROR_TITLE;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum class for the AppLcmError.
 */
@AllArgsConstructor
@Getter
public enum AppLcmError {
    // Errors and their corresponding messages.
    DEFAULT_APP_LCM_ERROR("Internal Server Error", "Internal App LCM server error."),
    APP_LCM_URI_GENERATION_ERROR("Internal Server Error", "Error while generating URI path."),
    ACM_URL_GENERATION_ERROR("Internal Server Error", "Error generating ACM URL. Invalid format."),
    TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR("TOSCA Service Template Generation Error",
        "Error while generating the TOSCA Service Template for ACM-R."),
    AC_INSTANCE_PROPERTIES_GENERATION_ERROR("AC Instance Properties Generation Error",
        "Error while generating the AC instance properties for ACM-R"),
    BAD_REQUEST_ERROR("Bad Request", "Invalid Input."),
    APP_NOT_FOUND_ERROR("Not Found", "Could not find the App specified."),
    VALIDATION_UUID_ERROR("Bad Request", "Invalid UUID."),
    INITIALIZE_APP_TIMEOUT_ERROR("Initialize App Timeout Error",
        "Initialize app has failed due to a timeout error."),
    DEPLOY_APP_INSTANCE_TIMEOUT_ERROR("Deploy App Instance Timeout Error",
        "Deploy app instance has failed due to a timeout error."),
    DEPLOY_APP_INSTANCE_COMPOSITION_TYPE_ERROR(APP_ACTION_DEPLOY_ERROR_TITLE, "Error while Deploying Automation Composition Type definition in ACM-R."),
    ACM_CREATE_COMPOSITION_ERROR("Automation Composition Definition Commission Error", "Error while commissioning AC definition in ACM-R"),

    DELETE_AC_INSTANCE_ERROR("AC Instance Deletion Error", "Error during deleting Automation Composition Instance in ACM-R"),
    VALIDATION_FAILED("Validation Error", "Validation Failed"),
    CREATE_APP_REQUEST_VALIDATION_FAILED("Validation Error", "Create app request validation failed due to %s."),
    CREATE_APP_INSTANCE_REQUEST_VALIDATION_FAILED("Validation Error", "Create app instance request validation failed due to %s."),
    LCM_APP_MODE_VALIDATION_ERROR("Bad Request", "App mode cannot be %s for %s operation - must be %s."),
    LCM_STATUS_VALIDATION_ERROR("Bad Request", "Status cannot be %s for %s operation - must be %s."),
    APP_ACTION_INVALID_TYPE_ERROR(APP_ACTION_INITIALIZE_ERROR_TITLE, "Action type provided is not a valid action."),
    APP_INSTANCE_ACTION_INVALID_TYPE_ERROR("%s action request failure", "Action type provided is not a valid action."),
    INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR(APP_ACTION_INITIALIZE_ERROR_TITLE, "Error while Priming AC Type definition in ACM-R."),
    DEINITIALIZE_APP_DEPRIME_COMPOSITION_TYPE_ERROR(APP_ACTION_DEINITIALIZE_ERROR_TITLE, "Error while De-Priming Composition in ACM-R for reason: %s"),
    GET_AUTOMATION_COMPOSITION_TYPE_ERROR(AC_RUNTIME_ERROR_TITLE, "Error during GET AC Type definition in ACM-R."),
    INITIALIZE_APP_COMPOSITION_TYPE_INVALID_STATE_ERROR(APP_ACTION_INITIALIZE_ERROR_TITLE, "Failure during Automation Composition Priming, AC Composition Type is not in state COMMISSIONED in ACM-R."),
    DEINITIALIZE_APP_COMPOSITION_TYPE_INVALID_STATE_ERROR(APP_ACTION_DEINITIALIZE_ERROR_TITLE, "Failure during Automation Composition De-Priming, AC Composition Type is not in state PRIMED in ACM-R."),
    DEINITIALIZE_APP_TIMEOUT_ERROR("DeInitialize App Timeout Error", "DeInitialize App has failed due to a timeout error."),
    DEINITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR(APP_ACTION_INITIALIZE_ERROR_TITLE, "Error while Priming Automation Composition Type definition in ACM-R."),
    DEINITIALIZE_APP_DEPRIMING_COMPOSITION_TYPE_ERROR(APP_ACTION_DEINITIALIZE_ERROR_TITLE, "Error while De-Priming Automation Composition Type definition in ACM-R."),
    APP_STATE_INVALID_FOR_INITIALISE(APP_ACTION_INITIALIZE_ERROR_TITLE, "App cannot be Initialized in current status - must be one of CREATED or INITIALIZE_ERROR and Mode = DISABLED."),
    APP_STATE_INVALID_FOR_DEINITIALIZE(APP_ACTION_DEINITIALIZE_ERROR_TITLE, "App cannot be De-Initialized in current status - must be one of INITIALIZED or DEINITIALIZE_ERROR and Mode = DISABLED."),

    DEPLOY_APP_INSTANCE_ERROR("Automation Composition Instance Deploy Error",
                                      "Error while deploying automation composition instance in ACM-R. Reason: %s"),
    UPDATE_APP_INSTANCE_ERROR("Automation Composition Instance Update Error",
                                      "Error while updating automation composition instance in ACM-R. Reason: %s"),
    UPDATE_APP_INSTANCE_TIMEOUT_ERROR("Update App Timeout Error",
                                              "Update App has failed due to a timeout error."),
    UPDATE_APP_INSTANCE_COMPOSITION_TYPE_ERROR( APP_ACTION_UPDATE_ERROR_TITLE, "Error while Priming AC Type definition in ACM-R."),
    APP_INSTANCE_COMPONENT_MISMATCH_ERROR("Bad Request",
            "Provided component name does not match with any of the created app components"),
    APP_INSTANCE_COMPONENT_TYPE_MISMATCH_ERROR("Bad Request",
            "Provided component type does not match with App component"),
    APP_INSTANCE_COMPONENT_VERSION_MISMATCH_ERROR("Bad Request",
            "Provided component version does not match with App component"),
    APP_INSTANCE_COMPONENT_STATUS_MISMATCH_ERROR("Bad Request",
            "Provided status does not match with App component"),
    APP_INSTANCE_COMPONENT_INSTANCE_REQUEST_ERROR("Bad Request",
            "No component instance request data found"),
    UPGRADE_APP_INSTANCE_ERROR("App Instance Upgrade Error",
        "Error while upgrading app instance. Reason: %s"),
    UPGRADE_APP_INSTANCE_COMPONENT_MISMATCH_ERROR("App Instance Upgrade Error",
            "Component mismatch with deployed App and target App. %s component not found"),
    UPGRADE_APP_INSTANCE_TIMEOUT_ERROR("Upgrade App Timeout Error",
        "Upgrade App has failed due to a timeout error."),
    UPGRADE_APP_INSTANCE_COMPOSITION_TYPE_ERROR( APP_ACTION_UPGRADE_ERROR_TITLE, "Error while Priming AC Type definition in ACM-R."),
    DELETE_APP_INSTANCE_TIMEOUT_ERROR("Delete App Instance Timeout Error",
        "Delete app instance has failed due to a timeout error."),
    DELETE_APP_INSTANCE_COMPOSITION_TYPE_ERROR(APP_ACTION_DELETE_ERROR_TITLE, "Error while Deleting Automation Composition Type definition in ACM-R."),
    BAD_REQUEST_ERROR_DELETE_APP_INSTANCES_EXIST(APP_ACTION_DELETE_ERROR_TITLE, "App has Instances."),
    BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST(APP_ACTION_INSTANTIATE_ERROR_TITLE, "App Instances already exist for provided App Id."),
    BAD_REQUEST_ERROR_DELETE_APP_NOT_EXIST(APP_ACTION_DELETE_ERROR_TITLE, "App does not exist."),
    SERVER_ERROR_ACM_COMPOSITION_DEF_DELETE(APP_ACTION_DELETE_ERROR_TITLE, "Error during DELETE AC Definition from ACM-R."),
    SERVER_ERROR_OBJECT_STORE_ARTIFACT_DELETE(APP_ACTION_DELETE_ERROR_TITLE, "Failed to remove artifacts"),
    ACM_CREATE_COMPOSITION_INSTANCE_ERROR("Automation Composition Instance Creation Error", "Error while creating AC instance in ACM-R."),
    INSTANCE_PROPERTY_ERROR("Bad Request", "Error while parsing property values."),
    BAD_REQUEST_TARGET_APP_INVALID_STATE("Bad Request", "The Target App is disabled. Please enable the app to proceed."),
    APP_INSTANCE_ENTITY_NOT_FOUND("App Instance Retrieval Error", "App Instance Details for given ID was not found in the DB"),
    APP_COMPONENT_ENTITY_NOT_FOUND("App Instance Retrieval Error", "App Component for given instance was not found in the DB"),
    CLIENT_CREDENTIAL_ENTITY_FOR_APP_INSTANCE_NOT_FOUND("Client Credential not found", "Client credential information for App Instance was not found in the DB"),
    COMPOSITION_ELEMENT_INSTANCE_NOT_EXISTING("Error getting App instance data",
        "ACM-R did not return a Composition Element Instance for the compositionElementId value stored in the App Component Instance in App LCM"),
    COMPOSITION_INSTANCE_NOT_FOUND("Error getting App instance data",
        "ACM-R did not return an Automation Composition Instance for the associated App Component Instance in App LCM."),

    //Keycloak relates Errors
    KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_SCOPE_ERROR("Keycloak Client Error", "App LCM failed to retrieve Id from client scope."),
    KEYCLOAK_RETRIEVE_TOKEN_ERROR("Keycloak Token Error", "App LCM failed to retrieve token from keycloak."),
    KEYCLOAK_CREATE_CLIENT_SCOPE_ERROR("Keycloak Client Scope Error", "App LCM failed to create client scope from keycloak."),
    KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_ERROR("Keycloak Client Id Retrieval Error", "App LCM failed to retrieve Id from client scope."),
    KEYCLOAK_ID_FROM_CLIENT_NOT_FOUND_ERROR("Keycloak Client Id Error", "App LCM could not find Id from client."),
    KEYCLOAK_CREATE_SECRET_ERROR("Keycloak Secret Error", "App LCM failed to create secret."),
    KEYCLOAK_CREATE_CLIENT_ERROR("Keycloak Client Creation Error", "App LCM failed to create client scope from keycloak."),
    KEYCLOAK_RETRIEVE_REALM_ROLES_ERROR("Keycloak Realm Roles Error", "Error while extracting realm roles from keycloak."),
    KEYCLOAK_SERVICE_ACCOUNT_ERROR("Keycloak Service Account Error", "Error while retrieving Service account from keycloak."),
    KEYCLOAK_ASSOCIATE_ROLES_ERROR("Keycloak Associated Roles Error", "Error while associating roles from keycloak."),
    KEYCLOAK_ROLES_NOT_FOUND_ERROR("Keycloak Roles Not Found Error", "Error, some roles to be associated/de-associated not found in keycloak."),
    KEYCLOAK_DEASSOCIATE_ROLES_ERROR("Keycloak De-Associated Roles Error", "Error while de-associating roles in keycloak."),
    KEYCLOAK_CLIENT_DELETION_ERROR("Keycloak Client Deletion Error", "Error while deleting client from keycloak."),
    KEYCLOAK_SCOPE_DELETION_ERROR("Keycloak Scope Deletion Error", "Error while deleting scope from keycloak."),
    KEYCLOAK_CREDENTIALS_NOT_FOUND_ERROR("Keycloak Credentials Not Found", "Unable to find keycloak credentials for the app instance id: %s"),
    UNDEPLOY_APP_INSTANCE_ERROR("Undeploy App Instance Error", "Error while undeploying App Instance from ACM-R"),
    UNDEPLOY_APP_INSTANCE_TIMEOUT_ERROR("Undeploy App Instance Timeout Error", "Undeploy App Instance has failed due to a timeout error."),
    APP_COMPONENT_INSTANCE_TIMEOUT_REQUEST_ERROR("Bad Request", "Timeout value exceeds the maximum allowed duration of %s minutes"),

    APP_COMPONENT_INSTANCE_UNSUPPORTED_TIMEOUT_TYPE_ERROR_MESSAGE ("Bad Request", "Unsupported timeout type");

    // Instance variables for an AppLcmError.
    private final String errorTitle;
    private final String errorMessage;
}