/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;

/**
 * Constants class for Keycloak.
 */
public final class KeycloakConstants {
    public static final String KEYCLOAK_CLIENT_ID = "admin-cli";
    public static final String KEYCLOAK_REALM_MASTER = "master";
    public static final String KEYCLOAK_GRANT_TYPE_PASSWORD = "password";
    public static final String KEYCLOAK_CLIENT_AUTHENTICATOR_TYPE = "client-secret";
    public static final String KEYCLOAK_PROTOCOL = "openid-connect";
    public static final String CLIENT_ID_NAME = "clientId";
    public static final String CLIENT_SECRET_NAME = "clientSecret";
    public static final String KEYCLOAK_URL_NAME = "baseUrl";
    public static final String KEYCLOAK_URL = SLASH + "auth" + SLASH + "admin" + SLASH + "realms"+SLASH;
    public static final String KAFKA = "kafka";
    public static final String SCOPE_GLOBAL = "GLOBAL";
    public static final String CLIENTS = "clients";
    public static final String USERS = "users";

    private KeycloakConstants() {
    }
}
