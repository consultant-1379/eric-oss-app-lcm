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

package com.ericsson.oss.ae.acm.clients.keycloak.common;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTH;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CLIENTS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CLIENT_SCOPES;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CLIENT_SECRET;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.HTTP;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.HTTPS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_PROTOCOL;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALMS_PATH;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.PROTOCOLS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.REALMS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ROLES;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ROLE_MAPPING_REALM;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.SERVICE_ACCOUNT_USER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.TOKEN;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.USERS;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

/**
 * URL Generator for Keycloak endpoints
 */
@Component
@Slf4j
public class KeycloakUrlGenerator {

    @Value("${KEYCLOAK_SERVICE_PORT:8080}")
    private String keycloakPort;

    @Value("${KEYCLOAK_SERVICE_HOSTNAME:eric-sec-access-mgmt-http}")
    private String keycloakHostname;

    @Value("${KEYCLOAK_TLS_HOSTNAME:}")
    private String keycloakTLSHostname;

    /**
     * Composes base URL for Keycloak Service.
     *
     * @return Returns Keycloak base URL including hostname and port
     */
    public String generateBasePath() {
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), null);
    }

    /**
     * Generate client scope keycloak url string.
     *
     * @param realm the realm
     * @return the string
     */
    public String generateClientScopeKeycloakUrl(final String realm) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENT_SCOPES;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate client scope keycloak url string by scope.
     *
     * @param realm the realm
     * @param scope the scope
     * @return the string
     */
    public String generateClientScopeKeycloakUrlByScope(final String realm, final String scope) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENT_SCOPES + SLASH + scope;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate bearer token keycloak url string.
     *
     * @return the string
     */
    public String generateBearerTokenKeycloakUrl() {
        final String path = SLASH + AUTH + SLASH + REALMS + SLASH + KEYCLOAK_REALM_MASTER + SLASH + PROTOCOLS + SLASH + KEYCLOAK_PROTOCOL + SLASH + TOKEN;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate client keycloak url string.
     *
     * @param realm the realm
     * @return the string
     */
    public String generateClientKeycloakUrl(final String realm) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENTS;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate client keycloak url string by id.
     *
     * @param realm the realm
     * @param id    the client id
     * @return the string
     */
    public String generateClientKeycloakUrlById(final String realm, final String id) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENTS + SLASH + id;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate secret keycloak url string.
     *
     * @param realm the realm
     * @param id    the id
     * @return the string
     */
    public String generateSecretKeycloakUrl(final String realm, final String id) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENTS + SLASH + id + SLASH + CLIENT_SECRET;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Extracting realm level roles.
     *
     * @param realm the realm
     * @return the string
     */
    public String generateClientRealmRoleUrl(final String realm) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + ROLES;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate service account.
     *
     * @param realm    the realm
     * @param clientId the client id
     * @return the string
     */
    public String generateServiceAccountUrl(final String realm, String clientId) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + CLIENTS + SLASH + clientId + SLASH + SERVICE_ACCOUNT_USER;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    /**
     * Generate associated role.
     *
     * @param realm            the realm
     * @param serviceAccountId the service account id
     * @return the string
     */
    public String generateAssociateRoleUrl(final String realm, final String serviceAccountId) {
        final String path = KEYCLOAK_REALMS_PATH + realm + SLASH + USERS + SLASH + serviceAccountId + ROLE_MAPPING_REALM;
        return generateUriPath(getKeycloakProtocol(), getKeycloakHost(), getKeycloakPort(), path);
    }

    private String generateUriPath(final String protocol, final String hostName, final String port, final String path) {
        log.info("generating URL for hostName: {}, port{} and path {}", hostName, port, path);
        try {
            return new URI(protocol, null, hostName, Integer.parseInt(port), path, null, null).toString();
        } catch (final URISyntaxException ex) {
            log.error("Error generating URL for hostName: {} for reason: {}", hostName, ex.getMessage());
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.APP_LCM_URI_GENERATION_ERROR);
        }
    }

    private String getKeycloakHost() {
        return StringUtil.isNullOrEmpty(keycloakTLSHostname) ? keycloakHostname : keycloakTLSHostname;
    }

    private String getKeycloakPort() {
        return StringUtil.isNullOrEmpty(keycloakTLSHostname) ? keycloakPort : "-1";
    }

    private String getKeycloakProtocol() {
        return StringUtil.isNullOrEmpty(keycloakTLSHostname) ? HTTP : HTTPS;
    }

}
