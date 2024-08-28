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

import static com.ericsson.oss.ae.acm.clients.keycloak.common.RoleUtil.ADD;
import static com.ericsson.oss.ae.acm.clients.keycloak.common.RoleUtil.DEL;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.BDR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KAFKA;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_CLIENT_AUTHENTICATOR_TYPE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_PROTOCOL;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_ID_FROM_CLIENT_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_ROLES_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_SERVICE_ACCOUNT_ERROR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.acm.clients.keycloak.KeycloakClientService;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.acm.clients.keycloak.model.Client;
import com.ericsson.oss.ae.acm.clients.keycloak.model.ProtocolMapperEntry;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.Role;
import com.ericsson.oss.ae.acm.utils.JsonParser;

/**
 * Handler for keycloak operations {@link KeycloakHandler}.
 * <p>
 * Contains methods to create keycloak credentials.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakHandler {

    @Autowired
    private final KeycloakClientService keycloakClientService;

    @Autowired
    private final KeycloakUrlGenerator keycloakUrlGenerator;

    /**
     * Generate keycloak credentials based on permissions and roles
     *
     * @param app app details.
     * @return client credentials.
     */
    public ClientCredential generateKeycloakCredentials(final App app) {
        log.info("Generate keycloak credentials");
        ClientCredential clientCredential = null;
        try {
            clientCredential = createKeycloakCredentials(app);
            if (Optional.ofNullable(app.getRoles()).isPresent() && !app.getRoles().isEmpty()) {
                createKeycloakRoles(app, clientCredential.getClientId());
            } else {
                log.debug("No roles were extracted for app id: {}", app.getId());
            }
        } catch (AppLcmException exception) {
            log.error("Keycloak exception while generating credentials", exception);
            rollBackCreatedKeycloakCredentials(clientCredential);
            throw exception;
        }
        return clientCredential;
    }

    /**
     * Generate keycloak credentials based on permissions and roles
     *
     * @param clientCredential keycloak client credential.
     * @return keycloak parameters map.
     */
    public Map<String, String> mapKeycloakParams(final ClientCredential clientCredential) {
        log.info("Generate keycloak properties map");
        final Map<String, String> keycloakParameters = new HashMap<>();
        keycloakParameters.put("keycloakClientId", clientCredential.getClientId());
        keycloakParameters.put("keycloakClientSecret", clientCredential.getClientSecret());
        keycloakParameters.put("keycloakClientUrl", keycloakUrlGenerator.generateBasePath());
        return keycloakParameters;
    }

    /**
     * Keycloak rollback logic for created credentials
     *
     * @param clientCredential keycloak client credential.
     */
    public void rollBackCreatedKeycloakCredentials(final ClientCredential clientCredential) {
        if (clientCredential != null && clientCredential.getClientId() != null) {
            log.debug("Delete Keycloak resources for Client ID: {}", clientCredential.getClientId());
            Arrays.stream((ClientDto[]) Objects.requireNonNull(Optional.ofNullable(keycloakClientService.getClients())
                            .orElseThrow(() -> {
                                log.error("Failure to retrieve client id from keycloak. Client ID:{}", clientCredential.getClientId());
                                return new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_ERROR);
                            }).getBody()))
                    .filter(c -> c.getClientId().equals(clientCredential.getClientId()))
                    .findAny()
                    .ifPresent(clientDto -> keycloakClientService.deleteClient(clientDto.getId()));
        }
    }

    /**
     * Update roles for the existing client id for upgrade
     *
     * @param appInstance app instance details
     * @param appInstance details of app to upgrade
     */
    public void updateExistingClientRoles(final AppInstances appInstance, final App appToUpgrade) {
        final Map<String, List<Role>> roleDiffMap = RoleUtil.roleDiff(Optional.ofNullable(appToUpgrade).orElse(new App()), Optional.ofNullable(appInstance.getApp()).orElse(new App()));
        if (!appInstance.getClientCredentials().isEmpty()) {
            final ClientCredential clientCredential = appInstance.getClientCredentials().get(0);
            if (!(roleDiffMap.get(ADD).isEmpty() && roleDiffMap.get(DEL).isEmpty())) {
                log.debug("Role difference found, removing following roles: {}", roleDiffMap.get(DEL));
                log.debug("Role difference found, adding following roles: {}", roleDiffMap.get(ADD));
                final String clientId = clientCredential.getClientId();
                final List<Role> rolesToDeAssociate = roleDiffMap.get(DEL);
                final List<Role> rolesToAssociate = roleDiffMap.get(ADD);
                final ResponseEntity<ClientDto[]> clientArray = keycloakClientService.getClients();
                final String Id = Arrays.stream(Optional.ofNullable(clientArray.getBody()).orElse(new ClientDto[]{})).filter(cl -> clientId.equals(cl.getClientId())).map(ClientDto::getId).findFirst().orElse("");
                final ResponseEntity<ClientRoleDTO[]> clientRoleArrayResponseEntity = keycloakClientService.extractRoles();
                final List<ClientRoleDTO> clientRoleToDeAssociateList = RoleUtil.validateAndExtractRoles(rolesToDeAssociate, clientRoleArrayResponseEntity);
                final List<ClientRoleDTO> clientRoleToAssociateList = RoleUtil.validateAndExtractRoles(rolesToAssociate, clientRoleArrayResponseEntity);
                final ResponseEntity<ServiceAccountDto> serviceAccountDtoResponseEntity = keycloakClientService.getServiceAccount(Id);
                final String serviceAccount = Optional.ofNullable(serviceAccountDtoResponseEntity.getBody()).orElseThrow(() -> new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_SERVICE_ACCOUNT_ERROR)).getId();
                if (!clientRoleToAssociateList.isEmpty()) {
                    keycloakClientService.associateRoles(clientRoleToAssociateList, serviceAccount);
                }
                if (!rolesToDeAssociate.isEmpty()) {
                    keycloakClientService.deassociateRoles(clientRoleToDeAssociateList, serviceAccount);
                }
            }
        }
    }

    private ClientCredential createKeycloakCredentials(final App app) {
        log.debug("Credential Event record not found in DB.");
        final ClientCredential clientCredential = new ClientCredential();
        log.debug("Create new client Id");
        createClientId(app, clientCredential);
        try {
            final ResponseEntity<CredentialDto> credentialDto = createSecret(clientCredential.getClientId());
            clientCredential.setClientSecret(Objects.requireNonNull(credentialDto.getBody()).getValue());
        } catch (AppLcmException exception) {
            log.error("Keycloak exception while generating credentials", exception);
            rollBackCreatedKeycloakCredentials(clientCredential);
            throw exception;
        }
        return clientCredential;
    }

    private void createClientId(final App app, final ClientCredential clientCredential) {
        final String appId = app.getId().toString();
        final String clientId = keycloakClientService.createClientId(appId);
        final Client client = createDefaultPropertiesClient(clientId);
        configureClientPermissions(client, app);
        final Map<String, Object> map = new HashMap<>();
        map.put("ExternalClient", "True");
        client.setAttributes(map);
        keycloakClientService.createClient(client);
        clientCredential.setClientId(clientId);
    }

    private Client createDefaultPropertiesClient(final String clientId) {
        log.debug("Create Client for client ID: {}", clientId);
        final Client client = (Client) JsonParser.getObjectFromJsonFile("ClientJsonV1.json", Client.class);
        client.setClientId(clientId);
        client.setClientAuthenticatorType(KEYCLOAK_CLIENT_AUTHENTICATOR_TYPE);
        client.setProtocol(KEYCLOAK_PROTOCOL);
        return client;
    }

    private void configureClientPermissions(final Client client, final App app) {
        final List<ProtocolMapperEntry> protocolMapperEntryList = new ArrayList<>();
        app.getPermissions().forEach(
                permission -> {
                    log.info("Analyzing claim  '{}' with scope '{}'", permission.getResource(), permission.getScope());
                    switch (permission.getResource().toLowerCase(Locale.ROOT)) {
                        case KAFKA:
                            protocolMapperEntryList.add(createGenericClaim());
                            protocolMapperEntryList.add(createCustomClaim("oauth\\.username\\.claim", client.getClientId(), KAFKA));
                            break;
                        case BDR:
                            protocolMapperEntryList.add(createCustomClaim("policy", permission.getScope(), BDR));
                            client.getDefaultClientScopes().add(BDR);
                            break;
                        default:
                            break;
                    }
                }
        );
        client.setProtocolMappers(protocolMapperEntryList);
    }

    private ProtocolMapperEntry createCustomClaim(final String claimConfigName, final String claimValue, final String constants) {
        final ProtocolMapperEntry claimEntry = (ProtocolMapperEntry) JsonParser.getObjectFromJsonFile("CustomOIDCClaim.json", ProtocolMapperEntry.class);
        claimEntry.setName(claimEntry.getName().concat(constants));
        claimEntry.getConfig().put("claim.value", claimValue);
        claimEntry.getConfig().put("claim.name", claimConfigName);
        return claimEntry;
    }

    private ProtocolMapperEntry createGenericClaim() {
        return (ProtocolMapperEntry) JsonParser.getObjectFromJsonFile("StandardOIDCClaim.json", ProtocolMapperEntry.class);
    }

    private ResponseEntity<CredentialDto> createSecret(final String clientId) {
        log.debug("Create Secret client ID: {}", clientId);
        final String id = getIdFromClient(clientId, (ClientDto[]) keycloakClientService.getClients().getBody());
        return keycloakClientService.createSecret(id);
    }

    private String getIdFromClient(final String clientId, final ClientDto[] body) {
        log.debug("Get ID From Client");
        return Arrays.stream(body)
                .filter(x -> clientId.equals(x.getClientId())).map(ClientDto::getId)
                .findAny()
                .orElseThrow(() -> {
                    log.error("Could not find Client ID: {} ", clientId);
                    return new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_ID_FROM_CLIENT_NOT_FOUND_ERROR);
                });
    }

    private void createKeycloakRoles(final App app, final String clientId) {
        final ResponseEntity<ClientRoleDTO[]> rolesInKeycloak = keycloakClientService.extractRoles();
        if (RoleUtil.areAllRolesPresent(Optional.ofNullable(rolesInKeycloak.getBody()).orElse(new ClientRoleDTO[]{}), app)) {
            final List<ClientRoleDTO> clientRoleDTOS = RoleUtil.validateAndExtractRoles(app.getRoles(), rolesInKeycloak);
            final ResponseEntity<ClientDto[]> clientArray = keycloakClientService.getClients();
            final String id = Arrays.stream(Optional.ofNullable(clientArray.getBody()).orElse(new ClientDto[]{})).filter(cl -> clientId.equals(cl.getClientId())).map(ClientDto::getId).findFirst().orElse("");
            final ResponseEntity<ServiceAccountDto> serviceAccount = keycloakClientService.getServiceAccount(id);
            keycloakClientService.associateRoles(clientRoleDTOS, Optional.ofNullable(serviceAccount.getBody()).map(ServiceAccountDto::getId).orElse(""));
        } else {
            throw new AppLcmException(HttpStatus.BAD_REQUEST, KEYCLOAK_ROLES_NOT_FOUND_ERROR);
        }
    }
}
