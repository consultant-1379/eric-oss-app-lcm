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
package com.ericsson.oss.ae.clients.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientAttributes {
    private String backchannelLogoutSessionRequired;
    private String backchannelLogoutRevokeOfflineTokens;
    private Object requestUris;
    private Object frontchannelLogoutUrl;
    private String samlArtifactBinding;
    private String samlServerSignature;
    private String samlServerSignatureKeyinfoExt;
    private String samlAssertionSignature;
    private String samlClientSignature;
    private String samlEncrypt;
    private String samlAuthnstatement;
    private String samlOnetimeuseCondition;
    private String samlForceNameIdFormat;
    private String samlMultivaluedRoles;
    private String samlForcePostBinding;
    private String excludeSessionStateFromAuthResponse;
    private String oauth2DeviceAuthorizationGrantEnabled;
    private String oidcCibaGrantEnabled;
    private String useRefreshTokens;
    private String idTokenAsDetachedSignature;
    private String tlsClientCertificateBoundAccessTokens;
    private String requirePushedAuthorizationRequests;
    private String clientCredentialsUseRefreshToken;
    private String tokenResponseTypeBearerLowerCase;
    private String displayOnConsentScreen;
    private String acrLoaMap;
    private Object oauth2DevicePollingInterval;

}
