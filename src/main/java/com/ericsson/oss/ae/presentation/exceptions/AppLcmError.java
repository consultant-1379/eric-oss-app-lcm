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

package com.ericsson.oss.ae.presentation.exceptions;

/**
 * Represents error codes and messages for the exception that can occur in <code>eric-oss-app-lcm</code>.
 */
public enum AppLcmError {

    DEFAULT_APP_LCM_ERROR_CODE(1000, "Internal app lcm server error."),
    APP_INSTANCE_NOT_FOUND(10, "App Instances not found"),
    SPECIFIED_APP_INSTANCE_NOT_FOUND(1001, "Could not find the app instance specified."),
    APP_ONBOARDING_APP_NOT_FOUND(1002, "App onboarding could not find the app."),
    APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND(1003, "App onboarding could not find the app artifact(s)."),
    HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP(1004, "Helm Orchestrator failed to instantiate app."),
    ARTIFACT_INSTANCE_NOT_FOUND(1005, "Could not find the artifact instance specified."),
    ARTIFACT_INSTANCE_FILE_ERROR(1006, "Could not download the helm source file for artifact instance."),
    APP_INSTANCE_ALREADY_TERMINATED(1007, "Instance already terminated."),
    APP_INSTANCE_NOT_INSTANTIATED(1008, "App Instance is not instantiated."),
    HELM_ORCHESTRATOR_RESOURCE_NOT_FOUND(1009, "Helm Orchestrator Resource Not Found."),
    HELM_ORCHESTRATOR_OPERATION_ERROR(1010, "Helm Orchestrator failed to retrieve Operation."),
    HELM_ORCHESTRATOR_OPERATION_LOGS_ERROR(1011, "Helm Orchestrator failed to retrieve Operation Logs."),
    HELM_ORCHESTRATOR_TERMINATION_ERROR(1012, "Helm Orchestrator failed to put application instance in TERMINATE state."),
    HELM_ORCHESTRATOR_DELETE_ERROR(1013, "Helm Orchestrator failed to delete application instance."),
    FAILURE_TO_TERMINATE_APP(1014, "App LCM failed to terminate app."),
    APP_LCM_DATA_ACCESS_ERROR(1015,  "Error accessing app_lcm database."),
    FAILURE_TO_RETRIEVE_ARTIFACT(1016, "App LCM failed to retrieve artifact."),
    INVALID_INPUT_EXCEPTION(1017, "Invalid input."),
    HELM_ORCHESTRATOR_FAILURE_TO_UPDATE_APP(1018, "Helm Orchestrator failed to update app."),
    FAILURE_TO_UPDATE_APP(1019, "App LCM failed to update app."),
    FAILURE_TO_DELETE(1020, "App Lcm failed to delete"),
    APP_ON_BOARDING_UPDATE_DELETING_FAILED(1021, "App OnBoarding failed to update delete process"),
    FAILURE_TO_CREATE_CLIENT(1022, "App LCM failed to create clientId."),
    FAILURE_TO_CREATE_SECRET(1023, "App LCM failed to create secret."),
    FAILURE_TO_RETRIEVE_ID_FROM_CLIENT(1024, "App LCM failed to retrieve Id from client."),
    FAILURE_TO_RETRIEVE_TOKEN(1025, "App LCM failed to retrieve token from keycloak."),
    ID_FROM_CLIENT_NOT_FOUND(1026, "App LCM could not find Id from client."),
    FAILURE_TO_CREATE_CLIENT_SCOPE(1027, "App LCM failed to create client scope from keycloak." ),
    APP_LCM_PARTIAL_DELETE_FAILURE(1028, "The multiple delete request has partially succeeded."),
    APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED(1029, "Requested app instance is not in TERMINATE or FAILED state."),
    ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED(1030, "Requested artifact instance is not in TERMINATE or FAILED state."),
    ARTIFACT_INSTANCE_NOT_DELETED(1031, "Requested artifact instance request has not succeeded."),
    FAILURE_TO_RETRIEVE_ID_FROM_CLIENT_SCOPE(1032, "App LCM failed to retrieve Id from client scope."),
    FAILURE_TO_RETRIEVE_ID_FROM_CLIENT_WITH_ATTRIBUTE(1033, "App LCM failed to create client with attribute."),
    APP_ON_BOARDING_DELETE_ERROR(1034, "Requested app was not deleted by OnBoarding"),
    APP_ON_BOARDING_DISABLED(1035, "Requested app is not enabled"),
    APP_ON_BOARDING_ENABLED(1036, "Requested app is not disabled"),
    APP_LCM_ERROR_CLIENT_DELETION(1050, "Error deleting client"),
    APP_LCM_ERROR_SCOPE_DELETION(1051, "Error deleting scope"),



    HELM_EXECUTOR_SERVICE_UNAVAILABLE(2001, "Helm File Executor Service Unavailable"),
    APP_ON_BOARDING_SERVICE_UNAVAILABLE(2002, "App-OnBoarding Service Unavailable"),

    FAILURE_TO_RETRIEVE_REALM_ROLES(2003, "Error while extracting realm roles"),
    APP_LCM_GET_SERVICE_ACCOUNT(2004, "Error retrieving Service account"),
    APP_LCM_ASSOCIATE_ROLES(2005, "Error while associating roles"),

    APP_LCM_ROLE_NOT_FOUND_IN_KEYCLOAK(2006, "Error while checking roles"),

    APP_LCM_DEASSOCIATE_ROLES(2007, "Error while deassociating roles"),

    APP_LCM_CLIENT_NOT_FOUND(2008, "Client not found in keycloak, cannot extract client id"),
    APP_LCM_ROLE_CONVERTED_NOT_FOUND(2009, "Error, some roles to be associated/deassociated not found in keycloak");

    private final int errorCode;
    private final String errorMessage;

    AppLcmError(final int errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorCodeAsString() {
        return Integer.toString(errorCode);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
