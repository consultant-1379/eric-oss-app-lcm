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

package com.ericsson.oss.ae.acm.clients.keycloak.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class for client details from keycloak.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {

    private String id;

    private String clientId;

    private Boolean surrogateAuthRequired;

    private Boolean enabled;

    private Boolean alwaysDisplayInConsole;

    private String clientAuthenticatorType;

    private List<Object> redirectUris = new ArrayList<>();

    private List<Object> webOrigins = new ArrayList<>();

    private Integer notBefore;

    private Boolean bearerOnly;

    private Boolean consentRequired;

    private Boolean standardFlowEnabled;

    private Boolean implicitFlowEnabled;

    private Boolean directAccessGrantsEnabled;

    private Boolean serviceAccountsEnabled;

    private Boolean publicClient;

    private Boolean frontchannelLogout;

    private String protocol;

    private List<ProtocolMapperEntry> protocolMappers = new ArrayList<>();

    private Map<String, Object> attributes;

    private Boolean fullScopeAllowed;

    private Integer nodeReRegistrationTimeout;

    private List<Object> optionalClientScopes = new ArrayList<>();

    private Access access;

    private String authorizationServicesEnabled;

    private List<String> defaultClientScopes = new ArrayList<>();

}
