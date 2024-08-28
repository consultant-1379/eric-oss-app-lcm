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

package com.ericsson.oss.ae.utils;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_LCM_URL;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_LCM_URL_V2;
import static com.ericsson.oss.ae.constants.AppLcmConstants.ARTIFACT_INSTANCES;
import static com.ericsson.oss.ae.constants.AppLcmConstants.COLON;
import static com.ericsson.oss.ae.constants.AppLcmConstants.HTTP;
import static com.ericsson.oss.ae.constants.AppLcmConstants.HTTPS;
import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_APPS_URL;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_ARTIFACTS;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_ARTIFACT_FILE;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENTS;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KEYCLOAK_PROTOCOL;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KEYCLOAK_URL;
import static com.ericsson.oss.ae.constants.KeycloakConstants.USERS;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_CNWLCM;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_OPERATIONS;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_OPERATIONS_LOGS;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_ORCHESTRATOR_WORKLOAD_INSTANCES;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;

/**
 * This class is responsible for reading service hostnames/ports and generating urls.
 * <p>
 * This class reads the hostnames and ports from the environment. These values are defined in values.yaml.
 */
@Component
@Slf4j
public class UrlGenerator {
    @Value("${APP_ONBOARDING_SERVICE_HOSTNAME:eric-oss-app-onboarding}")
    private String appOnboardingHostname;

    @Value("${APP_ONBOARDING_SERVICE_PORT:8080}")
    private String appOnboardingPort;

    @Value("${HELM_ORCHESTRATOR_SERVICE_HOSTNAME:eric-lcm-helm-executor}")
    private String helmOrchestratorHostname;

    @Value("${HELM_ORCHESTRATOR_SERVICE_PORT:8888}")
    private String helmOrchestratorPort;

    @Value("${APP_LCM_SERVICE_HOSTNAME:eric-oss-app-lcm}")
    private String appLcmHostname;

    @Value("${APP_LCM_SERVICE_PORT:8080}")
    private String appLcmPort;

    @Value("${KEYCLOAK_SERVICE_PORT:8080}")
    private String keycloakPort;

    @Value("${KEYCLOAK_SERVICE_HOSTNAME:eric-sec-access-mgmt-http}")
    private String keycloakHostname;

    @Value("${APP_MANAGER_APP_LCM_ROUTE_PATH:/app-manager/lcm}")
    private String appManagerAppLcmRoutePath;

    @Value("${APP_MANAGER_APP_ONBOARDING_ROUTE_PATH:/app-manager/onboarding}")
    private String appManagerAppOnboardingRoutePath;

    @Value("${KEYCLOAK_TLS_HOSTNAME:}")
    private String keycloakTLSHostname;

    /**
     * Composes base URL for App Onboarding Service.
     *
     * @return Returns App Onboarding base URL including hostname and port
     */
    public String getAppOnboardingHostAndPort() {
        return HTTP + appOnboardingHostname + COLON + appOnboardingPort;
    }

    /**
     * Composes base URL for Helm Orchestrator Service.
     *
     * @return Returns Helm Orchestrator base URL including hostname and port
     */
    public String getHelmOrchestratorHostAndPort() {
        return HTTP + helmOrchestratorHostname + COLON + helmOrchestratorPort;
    }

    /**
     * Composes base URL for App LCM Service.
     *
     * @return Returns App LCM base URL including hostname and port
     */
    public String getAppLcmHostAndPort() {
        return HTTP + appLcmHostname + COLON + appLcmPort;
    }

    /**
     * Composes base URL for Keycloak Service.
     *
     * @return Returns Keycloak base URL including hostname and port
     */
    public String getKeycloakHostAndPort() {

        String url = StringUtil.isNullOrEmpty(keycloakTLSHostname) ? (HTTP + keycloakHostname + COLON + keycloakPort) : (HTTPS +keycloakTLSHostname);
        log.info("Url generated for keycloak: " + url);
        return url;
    }

    /**
     * Composes route path for App LCM in App Manager.
     *
     * @return Returns App LCM route path in App Manager (/app/manager/lcm)
     */
    public String getAppManagerAppLcmRoutePath() {
        return appManagerAppLcmRoutePath;
    }

    /**
     * Composes route path for App Onboarding in App Manager.
     *
     * @return Returns App Onboarding route path in App Manager (/app/manager/onboarding)
     */
    public String getAppManagerAppOnboardingRoutePath() {
        return appManagerAppOnboardingRoutePath;
    }

    // Generate URLs for App Onboarding Service

    /**
     * Composes URL for App Onboarding Service '/v1/apps' endpoint.
     *
     * @return Returns App Onboarding URL for '/v1/apps' endpoint
     */
    public String generateAppsUrl() {
        return getAppOnboardingHostAndPort() + APP_ONBOARDING_APPS_URL;
    }

    /**
     * Composes URL for App Onboarding Service '/v1/apps/{appId}' endpoint.
     *
     * @param appId path parameter of the ID for the requested {@link AppDto} resource instance
     * @return Returns App Onboarding URL for '/v1/apps/{appId}' endpoint with appId
     */
    public String generateAppByIdUrl(final Long appId) {
        return generateAppsUrl() + SLASH + appId;
    }

    /**
     * Composes URL for App Onboarding Service '/v1/apps/{appId}/artifacts' endpoint.
     *
     * @param appId path parameter of the ID for the {@link AppDto} that the {@link ArtifactDto}s belong to
     * @return Returns App Onboarding URL for '/v1/apps/{appId}/artifacts' endpoint with appId included
     */
    public String generateArtifactsByAppIdUrl(final Long appId) {
        return generateAppByIdUrl(appId) + SLASH + APP_ONBOARDING_ARTIFACTS;
    }

    /**
     * Composes URL for App Onboarding Service '/v1/apps/{appId}/artifacts/{artifactId}' endpoint.
     *
     * @param appId      path parameter of the ID for the {@link AppDto} that the {@link ArtifactDto} belongs to
     * @param artifactId path parameter of the ID for the {@link ArtifactDto}
     * @return Returns App Onboarding URL for '/v1/apps/{appId}/artifacts/{artifactId}' endpoint with appId and artifactId included
     */
    public String generateArtifactByAppIdAndArtifactIdUrl(final Long appId, final Long artifactId) {
        return generateArtifactsByAppIdUrl(appId) + SLASH + artifactId;
    }

    /**
     * Composes URL for App Onboarding Service '/v1/apps/{appId}/artifacts/{artifactId}/file' endpoint.
     *
     * @param appId      path parameter of the ID for the {@link AppDto} that the {@link ArtifactDto} belongs to
     * @param artifactId path parameter of the ID for the {@link ArtifactDto}
     * @return Returns App Onboarding URL for '/v1/apps/{appId}/artifacts/{artifactId}/file' endpoint with appId and artifactId included
     */
    public String generateAppArtifactFileUrl(final Long appId, final Long artifactId) {
        return generateArtifactByAppIdAndArtifactIdUrl(appId, artifactId) + SLASH + APP_ONBOARDING_ARTIFACT_FILE;
    }

    // Generate URLs for Helm Orchestrator

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/workload_instances' endpoint.
     *
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/workload_instances' endpoint
     */
    public String generateWorkloadInstancesUrl() {
        return getHelmOrchestratorHostAndPort() + HELM_ORCHESTRATOR_CNWLCM + SLASH + HELM_ORCHESTRATOR_WORKLOAD_INSTANCES;
    }

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/workload_instances/{workloadInstanceId}' endpoint.
     *
     * @param workloadInstanceId path parameter of the ID for the requested {@link WorkloadInstanceDto} resource instance
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/workload_instances/{workloadInstanceId}' endpoint
     */
    public String generateWorkloadInstanceByIdUrl(final String workloadInstanceId) {
        return generateWorkloadInstancesUrl() + SLASH + workloadInstanceId;
    }

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/workload_instances/{workloadInstanceId}/operations' endpoint.
     *
     * @param workloadInstanceId path parameter of the ID for the requested {@link WorkloadInstanceDto} that operations are available for
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/workload_instances/{workloadInstanceId}/operations' endpoint
     */
    public String generateWorkloadInstanceOperationsByIdUrl(final String workloadInstanceId) {
        return generateWorkloadInstanceByIdUrl(workloadInstanceId) + SLASH + HELM_ORCHESTRATOR_OPERATIONS;
    }

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/operations' endpoint.
     *
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/operations' endpoint
     */
    public String generateOperationsInstanceUrl() {
        return getHelmOrchestratorHostAndPort() + HELM_ORCHESTRATOR_CNWLCM + SLASH + HELM_ORCHESTRATOR_OPERATIONS;
    }

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/operations/{operationId}' endpoint.
     *
     * @param operationId path parameter of the ID for the requested {@link OperationDto} resource instance
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/operations/{operationId}' endpoint
     */
    public String generateOperationsByIdUrl(final String operationId) {
        return generateOperationsInstanceUrl() + SLASH + operationId;
    }

    /**
     * Composes URL for Helm Orchestrator Service 'cnwlcm/v1/operations/{operationId}/logs' endpoint.
     *
     * @param operationId path parameter of the ID for the requested {@link OperationDto} resource instance
     * @return Returns Helm Orchestrator URL for 'cnwlcm/v1/operations/{operationId}logs' endpoint
     */
    public String generateOperationsLogsByIdUrl(final String operationId) {
        return generateOperationsByIdUrl(operationId) + SLASH + HELM_ORCHESTRATOR_OPERATIONS_LOGS;
    }

    // Generate Link Relation URLs for App LCM

    /**
     * Composes URL for App LCM Service '/app-lcm/v1/app-instances' endpoint.
     *
     * @return Returns App LCM URL for '/app-lcm/v1/app-instances' endpoint
     */
    public String generateAppInstancesUrl() {
        return getAppLcmHostAndPort() + APP_LCM_URL + SLASH + APP_INSTANCES;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v2/app-instances' endpoint.
     *
     * @return Returns App LCM URL for '/app-lcm/v2/app-instances' endpoint
     */
    public String generateAppInstancesV2Url() {
        return getAppLcmHostAndPort() + APP_LCM_URL_V2 + SLASH + APP_INSTANCES;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v1/app-instances/{appInstanceId}' endpoint.
     *
     * @param appInstanceId path parameter of the ID for the requested {@link AppInstanceDto} resource instance
     * @return Returns App LCM URL for '/app-lcm/v1/app-instances/{appInstanceId}' endpoint
     */
    public String generateAppInstanceByIdUrl(final Long appInstanceId) {
        return generateAppInstancesUrl() + SLASH + appInstanceId;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v2/app-instances/{appInstanceId}' endpoint.
     *
     * @param appInstanceId path parameter of the ID for the requested {@link AppInstanceDto} resource instance
     * @return Returns App LCM URL for '/app-lcm/v2/app-instances/{appInstanceId}' endpoint
     */
    public String generateAppInstanceByIdV2Url(final Long appInstanceId) {
        return generateAppInstancesV2Url() + SLASH + appInstanceId;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances' endpoint.
     *
     * @param appInstanceId path parameter of the ID for the requested {@link AppInstanceDto} that the {@link ArtifactInstanceDto}s belong to
     * @return Returns App LCM URL for '/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances' endpoint
     */
    public String generateArtifactInstancesByAppIdUrl(final Long appInstanceId) {
        return generateAppInstanceByIdUrl(appInstanceId) + SLASH + ARTIFACT_INSTANCES;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v2/app-instances/{appInstanceId}/artifact-instances' endpoint.
     *
     * @param appInstanceId path parameter of the ID for the requested {@link AppInstanceDto} that the {@link ArtifactInstanceDto}s belong to
     * @return Returns App LCM URL for '/app-lcm/v2/app-instances/{appInstanceId}/artifact-instances' endpoint
     */
    public String generateArtifactInstancesByAppIdV2Url(final Long appInstanceId) {
        return generateAppInstanceByIdV2Url(appInstanceId) + SLASH + ARTIFACT_INSTANCES;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances/{artifactInstanceId}' endpoint.
     *
     * @param appInstanceId      path parameter of the ID for the requested {@link AppInstanceDto} that the {@link ArtifactInstanceDto} belongs to
     * @param artifactInstanceId path parameter of the ID for the requested {@link ArtifactInstanceDto}
     * @return Returns App LCM URL for '/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances/{artifactInstanceId}' endpoint
     */
    public String generateArtifactInstanceByAppIdAndArtifactIdUrl(final Long appInstanceId, final Long artifactInstanceId) {
        return generateArtifactInstancesByAppIdUrl(appInstanceId) + SLASH + artifactInstanceId;
    }

    /**
     * Composes URL for App LCM Service '/app-lcm/v2/app-instances/{appInstanceId}/artifact-instances/{artifactInstanceId}' endpoint.
     *
     * @param appInstanceId      path parameter of the ID for the requested {@link AppInstanceDto} that the {@link ArtifactInstanceDto} belongs to
     * @param artifactInstanceId path parameter of the ID for the requested {@link ArtifactInstanceDto}
     * @return Returns App LCM URL for '/app-lcm/v2/app-instances/{appInstanceId}/artifact-instances/{artifactInstanceId}' endpoint
     */
    public String generateArtifactInstanceByAppIdAndArtifactIdV2Url(final Long appInstanceId, final Long artifactInstanceId) {
        return generateArtifactInstancesByAppIdV2Url(appInstanceId) + SLASH + artifactInstanceId;
    }

    /**
     * Generate bearer token keycloak url string.
     *
     * @return the string
     */
    public String generateBearerTokenKeycloakUrl(){
        return getKeycloakHostAndPort() + SLASH + "auth" + SLASH +  "realms" + SLASH + KEYCLOAK_REALM_MASTER + SLASH + "protocol" + SLASH + KEYCLOAK_PROTOCOL + SLASH + "token";
    }

    /**
     * Generate client keycloak url string.
     *
     * @param realm the realm
     * @return the string
     */
    public String generateClientKeycloakUrl(final String realm){
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + CLIENTS;
    }

    /**
     * Generate secret keycloak url string.
     *
     * @param realm the realm
     * @param id    the id
     * @return the string
     */
    public String generateSecretKeycloakUrl(final String realm,final String id){
        return generateClientKeycloakUrl(realm) + SLASH + id + SLASH + "client-secret";
    }

    /**
     * Generate client scope keycloak url string.
     *
     * @param realm the realm
     * @return the string
     */
    public String generateClientScopeKeycloakUrl(final String realm){
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + "client-scopes";

    }

    public String generateClientWithAttributeKeycloakUrl(final String realm){
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + CLIENTS;

    }

    public String generateClientRealmRoleUrl(final String realm) {
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + "roles";
    }

    public String generateServiceAccountUrl(final String realm, String clientId) {
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + CLIENTS + SLASH + clientId + SLASH + "service-account-user";
    }

    public String generateAssociateRoleUrl(final String realm, final String serviceAccountId) {
        return getKeycloakHostAndPort() + KEYCLOAK_URL + realm + SLASH + USERS + SLASH + serviceAccountId + "/role-mappings/realm";
    }

    /**
     * Generate update status for deletion url string.
     *
     * @param appId the app id
     * @return the string
     */
    public String generateAppOnBoardingWithIdUrl(Long appId) {
        return generateAppByIdUrl(appId);
    }
}
