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

package com.ericsson.oss.ae.acm.clients.keycloak;

import static com.ericsson.oss.ae.acm.TestConstants.HTTP_SEC_ACCESS_MGMT;
import static com.ericsson.oss.ae.acm.TestConstants.ID_1;
import static com.ericsson.oss.ae.acm.TestConstants.PORT_VALUE;
import static com.ericsson.oss.ae.acm.TestConstants.REALM;
import static com.ericsson.oss.ae.acm.TestConstants.SCOPE;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

@ExtendWith(MockitoExtension.class)
public class KeycloakUrlGeneratorTest {

    private static final String ADMIN_REALMS_REALM_CLIENT_SCOPES = "http://eric-sec-access-mgmt-http:8080/auth/admin/realms/realm/client-scopes";
    private static final String ACCESS_MGMT_HTTP = ":eric-sec-access-mgmt-http";
    private static final String KEYCLOAK_HOST_NAME_VALUE = "eric-sec-access-mgmt-http";
    private static final String KEYCLOAK_HOST_NAME = "keycloakHostname";
    private static final String KEYCLOAK_PORT = "keycloakPort";
    private static final String KEYCLOAK_TLS_HOST_NAME = "keycloakTLSHostname";
    private static final String REALM_CLIENT_SCOPES_SCOPE = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/client-scopes/scope";
    private static final String REALMS_MASTER_PROTOCOL_OPENID_CONNECT_TOKEN = HTTP_SEC_ACCESS_MGMT + "/auth/realms/master/protocol/openid-connect/token";
    private static final String REALM_CLIENTS = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/clients";
    private static final String REALM_CLIENTS_1 = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/clients/1";
    private static final String REALM_CLIENTS_ID_CLIENT_SECRET = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/clients/id/client-secret";
    private static final String REALM_ROLES = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/roles";
    private static final String REALM_CLIENTS_CLIENT_ID_SERVICE_ACCOUNT_USER = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/clients/clientId/service-account-user";
    private static final String REALM_USERS_1_ROLE_MAPPINGS_REALM = HTTP_SEC_ACCESS_MGMT + "/auth/admin/realms/realm/users/1/role-mappings/realm";

    private KeycloakUrlGenerator keycloakUrlGeneratorUnderTest;

    @BeforeEach
    void setUp() {
        keycloakUrlGeneratorUnderTest = new KeycloakUrlGenerator();
        ReflectionTestUtils.setField(keycloakUrlGeneratorUnderTest, KEYCLOAK_PORT, PORT_VALUE);
        ReflectionTestUtils.setField(keycloakUrlGeneratorUnderTest, KEYCLOAK_HOST_NAME, KEYCLOAK_HOST_NAME_VALUE);
        ReflectionTestUtils.setField(keycloakUrlGeneratorUnderTest, KEYCLOAK_TLS_HOST_NAME, "");
    }

    @Test
    void testGenerateBasePath() {
        assertThat(keycloakUrlGeneratorUnderTest.generateBasePath()).isEqualTo(HTTP_SEC_ACCESS_MGMT);
    }

    @Test
    void testGenerateClientScopeKeycloakUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateClientScopeKeycloakUrl(REALM)).isEqualTo(ADMIN_REALMS_REALM_CLIENT_SCOPES);
    }

    @Test
    void testGenerateClientScopeKeycloakUrl_throws_lcm_exception() {
        ReflectionTestUtils.setField(keycloakUrlGeneratorUnderTest, KEYCLOAK_HOST_NAME, ACCESS_MGMT_HTTP);
        assertThatThrownBy(() -> keycloakUrlGeneratorUnderTest.generateClientScopeKeycloakUrl(REALM))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testGenerateClientScopeKeycloakUrlByScope() {
        assertThat(keycloakUrlGeneratorUnderTest.generateClientScopeKeycloakUrlByScope(REALM, SCOPE))
                .isEqualTo(REALM_CLIENT_SCOPES_SCOPE);
    }

    @Test
    void testGenerateBearerTokenKeycloakUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateBearerTokenKeycloakUrl()).isEqualTo(REALMS_MASTER_PROTOCOL_OPENID_CONNECT_TOKEN);
    }

    @Test
    void testGenerateClientKeycloakUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateClientKeycloakUrl(REALM)).isEqualTo(REALM_CLIENTS);
    }

    @Test
    void testGenerateClientKeycloakUrlById() {
        assertThat(keycloakUrlGeneratorUnderTest.generateClientKeycloakUrlById(REALM, ID_1)).isEqualTo(REALM_CLIENTS_1);
    }

    @Test
    void testGenerateSecretKeycloakUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateSecretKeycloakUrl(REALM, "id")).isEqualTo(REALM_CLIENTS_ID_CLIENT_SECRET);
    }

    @Test
    void testGenerateClientRealmRoleUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateClientRealmRoleUrl(REALM)).isEqualTo(REALM_ROLES);
    }

    @Test
    void testGenerateServiceAccountUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateServiceAccountUrl(REALM, CLIENT_ID_NAME)).isEqualTo(REALM_CLIENTS_CLIENT_ID_SERVICE_ACCOUNT_USER);
    }

    @Test
    void testGenerateAssociateRoleUrl() {
        assertThat(keycloakUrlGeneratorUnderTest.generateAssociateRoleUrl(REALM, ID_1)).isEqualTo(REALM_USERS_1_ROLE_MAPPINGS_REALM);
    }

}

