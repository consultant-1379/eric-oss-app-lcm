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

package com.ericsson.oss.ae.clients.keycloak;

import com.ericsson.oss.ae.clients.keycloak.dto.*;
import com.ericsson.oss.ae.clients.keycloak.model.Client;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface specifying Keycloak client calls.
 * <p>
 * Implementation of this interface {@link KeycloakClientImpl}.
 */
public interface KeycloakClient {

    ResponseEntity<TokenDto> getAuthenticationToken(String target,String refreshToken);
    ResponseEntity<Void> createClient(Client client);
    ResponseEntity<CredentialDto> createSecret(String id);
    ResponseEntity<ClientDto[]> getClients();
    String createClientId(String appOnBoardingId);
    ResponseEntity<Void> deleteClient(String id);
    void deleteClientByClientId(String clientId);
    ResponseEntity<ClientRoleDTO[]> extractRoles();
    ResponseEntity<ServiceAccountDto> getServiceAccount(String clientId);
    ResponseEntity<Void> associateRoles(List<ClientRoleDTO> roleList, String serviceAccountId);
    ResponseEntity<Void> deassociateRoles(List<ClientRoleDTO> roleList, String serviceAccountId);
}
