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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_CLIENT_ID;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_GRANT_TYPE_PASSWORD;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_ASSOCIATE_ROLES_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_CLIENT_DELETION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_CREATE_CLIENT_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_CREATE_CLIENT_SCOPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_CREATE_SECRET_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_DEASSOCIATE_ROLES_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_SCOPE_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_REALM_ROLES_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_RETRIEVE_TOKEN_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_SCOPE_DELETION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_SERVICE_ACCOUNT_ERROR;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientScopeDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.acm.clients.keycloak.model.Client;
import com.ericsson.oss.ae.acm.clients.keycloak.model.GrantUserToken;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.rest.RequestBuilder;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;
import com.ericsson.oss.ae.acm.common.rest.RestRequest;


/**
 * Implementation of keycloak service implementation {@link KeycloakClientService}.
 * <p>
 * Contains methods used to make REST requests to get keycloak data.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakClientService<T> {

    @Value("${KEYCLOAK_ADMIN_USER:admin}")
    private String iamAdminUser;

    @Value("${KEYCLOAK_ADMIN_P:test}")
    private String iamAdminP;

    @Autowired
    @Qualifier("keycloakRestClientService")
    private final RestClientService restClientService;

    @Autowired
    private final KeycloakUrlGenerator keycloakUrlGenerator;

    @Autowired
    private final RequestBuilder requestBodyBuilder;

    /**
     * Get client scopes from keycloak
     *
     * @return response entity contains list of client scopes
     */
    public ResponseEntity<ClientDto[]> getClientScope() {
        final String url = keycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        try {
            return (ResponseEntity<ClientDto[]>) callKeycloakClientWithNoRequestBody(url, ClientDto[].class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while calling Keycloak client", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_SCOPE_ERROR);
        }
    }

    /**
     * Create client scope in keycloak
     *
     * @param clientScopeDto Scope details
     * @return response entity with status success
     */
    public ResponseEntity<Void> createClientScope(final ClientScopeDto clientScopeDto) {
        log.info("Create Client Scope");
        final String url = keycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        log.debug("Send Request to Keycloak. URL: {}", url);
        try {
            return (ResponseEntity<Void>) callKeycloakClient(url, new ObjectMapper().writeValueAsString(clientScopeDto), ClientScopeDto.class, MediaType.APPLICATION_JSON, HttpMethod.POST);
        } catch (final RestRequestFailedException | JsonProcessingException exception) {
            log.error("Exception while creating client scope", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_CREATE_CLIENT_SCOPE_ERROR);
        }
    }

    /**
     * Get clients in keycloak
     *
     * @return response entity contains list of clients
     */
    public ResponseEntity<ClientDto[]> getClients() {
        final String url = keycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        try {
            return (ResponseEntity<ClientDto[]>) callKeycloakClientWithNoRequestBody(url, ClientDto[].class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while getting client list from keycloak", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_RETRIEVE_ID_FROM_CLIENT_ERROR);
        }

    }

    /**
     * Create client secret in keycloak
     *
     * @param id Client id
     * @return response entity with credential data
     */
    public ResponseEntity<CredentialDto> createSecret(final String id) {
        final String url = keycloakUrlGenerator.generateSecretKeycloakUrl(KEYCLOAK_REALM_MASTER, id);
        try {
            return (ResponseEntity<CredentialDto>) callKeycloakClientWithNoRequestBody(url, CredentialDto.class, MediaType.APPLICATION_JSON, HttpMethod.POST);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while creating keycloak secret", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_CREATE_SECRET_ERROR);
        }

    }

    /**
     * Create client in keycloak
     *
     * @param client Client details
     * @return response entity with status success
     */
    public ResponseEntity<Void> createClient(final Client client) {
        log.info("Create Keycloak Client for client ID: {}", client.getClientId());
        final String url = keycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        log.debug("Send Rest Request To Keycloak URL: {}", url);
        try {
            return (ResponseEntity<Void>) callKeycloakClient(url, new ObjectMapper().writeValueAsString(client), ClientDto.class, MediaType.APPLICATION_JSON, HttpMethod.POST);
        } catch (final RestRequestFailedException | JsonProcessingException exception) {
            log.error("Exception while creating keycloak client", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_CREATE_CLIENT_ERROR);
        }
    }

    /**
     * Create random client id
     *
     * @param appId App id
     * @return client id
     */
    public String createClientId(final String appId) {
        log.info("Create Client ID");
        final UUID uuid = UUID.randomUUID();
        return "rappid-" + appId + "-" + new Timestamp(new Date().getTime()).getTime() + "-" + uuid;
    }

    /**
     * Get roles from keycloak
     *
     * @return response entity contains list of client roles
     */
    public ResponseEntity<ClientRoleDTO[]> extractRoles() {
        final String url = keycloakUrlGenerator.generateClientRealmRoleUrl(KEYCLOAK_REALM_MASTER);
        try {
            return (ResponseEntity<ClientRoleDTO[]>) callKeycloakClientWithNoRequestBody(url, ClientRoleDTO[].class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while extracting realm level roles", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_RETRIEVE_REALM_ROLES_ERROR);
        }
    }

    /**
     * Get service account from keycloak
     *
     * @param
     * @return response entity with service account details
     */
    public ResponseEntity<ServiceAccountDto> getServiceAccount(final String clientId) {
        final String url = keycloakUrlGenerator.generateServiceAccountUrl(KEYCLOAK_REALM_MASTER, clientId);
        try {
            return (ResponseEntity<ServiceAccountDto>) callKeycloakClientWithNoRequestBody(url, ServiceAccountDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while getting service accounts", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_SERVICE_ACCOUNT_ERROR);
        }
    }

    /**
     * Associate roles in keycloak based on service account id
     *
     * @param roleList         list of roles
     * @param serviceAccountId service account id
     * @return response entity with service account details
     */
    public ResponseEntity<Void> associateRoles(final List<ClientRoleDTO> roleList, final String serviceAccountId) {
        log.info("Associate roles: {} for service account: {}", roleList, serviceAccountId);
        final String url = keycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, serviceAccountId);
        log.debug("Send Request to Keycloak. URL: {}", url);
        try {
            return (ResponseEntity<Void>) callKeycloakClient(url, new ObjectMapper().writeValueAsString(roleList), List.class, MediaType.APPLICATION_JSON, HttpMethod.POST);
        } catch (final RestRequestFailedException | JsonProcessingException exception) {
            log.error("Exception while associating roles", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_ASSOCIATE_ROLES_ERROR);
        }
    }

    /**
     * De-associate roles in keycloak based on service account id
     *
     * @param roleList         list of roles
     * @param serviceAccountId service account id
     * @return response entity with success status
     */
    public ResponseEntity<Void> deassociateRoles(final List<ClientRoleDTO> roleList, final String serviceAccountId) {
        log.info("DeAssociate roles: {} for service account:  {}", roleList, serviceAccountId);
        final String url = keycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, serviceAccountId);
        log.debug("Send Request to Keycloak. URL: {}", url);
        try {
            return (ResponseEntity<Void>) callKeycloakClient(url, new ObjectMapper().writeValueAsString(roleList), List.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE);
        } catch (final RestRequestFailedException | JsonProcessingException exception) {
            log.error("Exception while de-associating roles", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_DEASSOCIATE_ROLES_ERROR);
        }
    }


    /**
     * Delete client from keycloak
     *
     * @param id client id
     * @return response entity with success status
     */
    public ResponseEntity<Void> deleteClient(final String id) {
        final String url = keycloakUrlGenerator.generateClientKeycloakUrlById(KEYCLOAK_REALM_MASTER, id);
        try {
            return (ResponseEntity<Void>) callKeycloakClientWithNoRequestBody(url, Object.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while deleting keycloak client", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_CLIENT_DELETION_ERROR);
        }
    }

    /**
     * Delete scope from keycloak
     *
     * @param scope scope to delete
     * @return response entity with success status
     */
    public ResponseEntity<Void> deleteScope(final String scope) {
        final String url = keycloakUrlGenerator.generateClientScopeKeycloakUrlByScope(KEYCLOAK_REALM_MASTER, scope);
        try {
            return (ResponseEntity<Void>) callKeycloakClientWithNoRequestBody(url, Object.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while deleting scope from keycloak", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_SCOPE_DELETION_ERROR);
        }
    }

    private MultiValueMap<String, Object> generateMultiValueMap(final Object dto) {
        final MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        final Map<Object, Object> map = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(dto, Map.class);
        for (Map.Entry<Object, Object> attribute : map.entrySet()) {
            valueMap.add(attribute.getKey().toString(), attribute.getValue());
        }
        return valueMap;
    }

    private GrantUserToken generateGrantUserTokenByTarget(final String target, final String refreshToken) {
        return GrantUserToken.builder()
                .clientId(KEYCLOAK_CLIENT_ID)
                .grantType(target)
                .refreshToken(refreshToken)
                .password(iamAdminP)
                .username(iamAdminUser).build();
    }

    private ResponseEntity<T> callKeycloakClient(final String url, final String request, final Class responseType, final MediaType contentMediaType, final HttpMethod httpMethod) {
        final String bearerToken = Objects.requireNonNull(getAuthenticationToken(KEYCLOAK_GRANT_TYPE_PASSWORD, "").getBody()).getAccessToken();
        final RestRequest requestBody = requestBodyBuilder.createRequestContent(url, request, responseType, contentMediaType, httpMethod, bearerToken, null);
        return restClientService.callRestEndpoint(requestBody);
    }

    private ResponseEntity<T> callKeycloakClientWithNoRequestBody(final String url, final Class responseType, final MediaType acceptedMediaType, final HttpMethod httpMethod) {
        final String bearerToken = Objects.requireNonNull(getAuthenticationToken(KEYCLOAK_GRANT_TYPE_PASSWORD, "").getBody()).getAccessToken();
        final RestRequest requestBody = requestBodyBuilder.createRequestContentWithNoBody(url, responseType, acceptedMediaType, httpMethod, bearerToken, null);
        return restClientService.callRestEndpoint(requestBody);
    }

    /**
     * This client request will provide token within target status, if the target is password , it will create a new token, if target is refresh_token, it will refresh
     * the token, be aware that on first creation you receive a refresh_token along the other parameters,
     * btw: even when refreshing we ought to use the main token provided
     */
    private ResponseEntity<TokenDto> getAuthenticationToken(final String target, final String refreshToken) {
        final String url = keycloakUrlGenerator.generateBearerTokenKeycloakUrl();
        try {
            final MultiValueMap<String, Object> multiValueMap = generateMultiValueMap(generateGrantUserTokenByTarget(target, refreshToken));
            final RestRequest requestBody = requestBodyBuilder.createMultiValueRequestContent(url, multiValueMap, TokenDto.class, MediaType.APPLICATION_JSON, HttpMethod.POST);
            return restClientService.callRestEndpoint(requestBody);
        } catch (final RestRequestFailedException exception) {
            log.error("Exception while retrieving authentication token from Keycloak client", exception);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, KEYCLOAK_RETRIEVE_TOKEN_ERROR);
        }
    }
}
