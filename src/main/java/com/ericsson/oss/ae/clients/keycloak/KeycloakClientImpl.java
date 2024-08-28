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

import com.ericsson.oss.ae.clients.helmorchestrator.mapper.EnvironmentHolder;
import com.ericsson.oss.ae.clients.keycloak.dto.*;
import com.ericsson.oss.ae.clients.keycloak.model.Client;
import com.ericsson.oss.ae.clients.keycloak.model.GrantUserToken;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.KeycloakException;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.rest.RequestHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.*;

import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_LCM_ERROR_CLIENT_DELETION;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT;


/**
 * Implementation of App Onboarding REST client class {@link KeycloakClient}.
 * <p>
 * Contains methods used to make REST requests to App Onboarding service.
 */
@Service
@Slf4j
public class KeycloakClientImpl implements KeycloakClient {

    @Autowired
    private EnvironmentHolder environmentHolder;
    private final RequestHandler requestHandler;

    private final UrlGenerator urlGenerator;


    public KeycloakClientImpl(RequestHandler requestHandler, UrlGenerator urlGenerator) {
        this.requestHandler = requestHandler;
        this.urlGenerator = urlGenerator;
    }


//this client request will provide token within target status, if the target is password , it will create a new token, if target is refresh_token, it will refresh
    // the token, be aware that on first creation you receive a refresh_token along the other parameters, btw: even when refreshing we ought to use the main token provided
    @Override
    public ResponseEntity<TokenDto> getAuthenticationToken(String target,String refreshToken) {
        final String url = urlGenerator.generateBearerTokenKeycloakUrl();

        try {

            return requestHandler.sendRestRequestToKeycloak(generateMultiValueMapUsingDto(generateGrantUserTokenByTarget(target,refreshToken)), url,
                    TokenDto.class,MediaType.APPLICATION_FORM_URLENCODED, HttpMethod.POST);
        } catch (final RestClientException exception) {
            final String message = "Error extracting or refreshing token ";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.FAILURE_TO_RETRIEVE_TOKEN, message, url, exception);
        }

    }


    @Override
    public ResponseEntity<Void> createClient(Client client) {
        log.info("Create Keycloak Client for client Id:{}", client.getClientId());
        String token = Objects.requireNonNull(
            getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER);
        log.debug("Send Rest Request To Keycloak Url: {}", url);
        try {
            return requestHandler.sendRestRequestToKeycloak(client, url,
                    ClientDto.class,MediaType.APPLICATION_JSON, HttpMethod.POST,token);
        } catch (final RestClientException exception) {
            final String message = "Error creating client ";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.FAILURE_TO_CREATE_CLIENT, message, url, exception);
        }
    }

    @Override
    public ResponseEntity<CredentialDto> createSecret(String id) {
        String token = Objects.requireNonNull(
            getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateSecretKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER,id);
        try {
            return requestHandler.sendRestRequestToKeycloak(null,url, CredentialDto.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        } catch (final RestClientException exception) {
            final String message = "Error extracting or creating secret ";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.FAILURE_TO_CREATE_SECRET, message, url, exception);
        }

    }

    @Override
    public ResponseEntity<ClientDto[]> getClients() {

        String token = Objects.requireNonNull(
            getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER);

        try {
            return requestHandler.createAndSendRestRequest(url,ClientDto[].class, MediaType.APPLICATION_JSON,HttpMethod.GET,token);
        } catch (final RestClientException exception) {
            final String message = "Error extracting clients ";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT, message, url, exception);
        }

    }

    public  String createClientId(String appOnBoardingId) {
        log.info("Create Client Id");
        UUID uuid=UUID.randomUUID();
        return "rappid--" + appOnBoardingId + "--" + uuid;
    }

    @Override
        public ResponseEntity<Void> deleteClient(String id) {
                    String token = Objects.requireNonNull(
                        getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
                    final String url = urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER) + SLASH + id ;
                    try {
                          return requestHandler.createAndSendRestRequest(url,Object.class, MediaType.APPLICATION_JSON,HttpMethod.DELETE,token);
                        } catch (final RestClientException exception) {
                            final String message = "Error deleting client with id " + id;
                            log.error(message, exception.getMessage());
                           throw new KeycloakException(APP_LCM_ERROR_CLIENT_DELETION, message, url, exception);
                        }
               }

    @Override
    public void deleteClientByClientId(String clientId) {
        Arrays.stream(Objects.requireNonNull(this.getClients().getBody()))
                .filter(c -> c.getClientId().equals(clientId))
                .findAny()
                .ifPresentOrElse(clientDto -> this.deleteClient(clientDto.getId()), () -> {
                    log.error("Error while deleting client with client-id {}, not found ",APP_LCM_ERROR_CLIENT_DELETION.getErrorMessage());
                    throw new ResourceNotFoundException(APP_LCM_ERROR_CLIENT_DELETION,
                            "Error client not found'", urlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER));
                });
    }

    private GrantUserToken generateGrantUserTokenByTarget(String target,String refreshToken){
        GrantUserToken grantUserToken = new GrantUserToken();
        grantUserToken.setGrantType(target);
        grantUserToken.setClientId(KeycloakConstants.KEYCLOAK_CLIENT_ID);
        grantUserToken
                .setPassword(environmentHolder.getIamAdminP());
        grantUserToken.setRefreshToken(refreshToken);
        grantUserToken.setUsername(environmentHolder.getIamAdminUser());
        return grantUserToken;
    }
    private MultiValueMap<String, Object> generateMultiValueMapUsingDto(Object dto){
        MultiValueMap<String,Object> valueMap = new LinkedMultiValueMap<>();
        Map<Object, Object> map =  new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(dto, Map.class);
        for (Map.Entry<Object, Object> attribute :map.entrySet()) {

            valueMap.add(attribute.getKey().toString(), attribute.getValue());
        }
        return valueMap;
    }

    public ResponseEntity<ClientRoleDTO[]> extractRoles(){
        String token = Objects.requireNonNull(
                getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateClientRealmRoleUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER);
        try {
            return requestHandler.createAndSendRestRequest(url, ClientRoleDTO[].class, MediaType.APPLICATION_JSON,HttpMethod.GET,token);
        } catch (final RestClientException exception) {
            final String message = "Error extracting realm level roles";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.FAILURE_TO_RETRIEVE_REALM_ROLES, message, url, exception);
        }
    }

    @Override
    public ResponseEntity<ServiceAccountDto> getServiceAccount(String clientId) {
        String token = Objects.requireNonNull(
                getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateServiceAccountUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER, clientId);
        try {
            return requestHandler.createAndSendRestRequest(url, ServiceAccountDto.class, MediaType.APPLICATION_JSON,HttpMethod.GET,token);
        } catch (final RestClientException exception) {
            final String message = "Error extracting realm level roles";
            log.error(message, exception.getMessage());
            throw new KeycloakException(AppLcmError.APP_LCM_GET_SERVICE_ACCOUNT, message, url, exception);
        }
    }


    @Override
    public ResponseEntity<Void> associateRoles(List<ClientRoleDTO> roleList, String serviceAccountId) {
        log.info("Associate roles {} for service account  {}", roleList, serviceAccountId);
        return commonRoleAssociationCall(roleList, serviceAccountId, HttpMethod.POST, "Error while associating roles", AppLcmError.APP_LCM_ASSOCIATE_ROLES);
    }

    @Override
    public ResponseEntity<Void> deassociateRoles(List<ClientRoleDTO> roleList, String serviceAccountId) {
        log.info("DeAssociate roles {} for service account  {}", roleList, serviceAccountId);
        return commonRoleAssociationCall(roleList, serviceAccountId, HttpMethod.DELETE, "Error while deassociating roles", AppLcmError.APP_LCM_DEASSOCIATE_ROLES);
    }

    private ResponseEntity<Void> commonRoleAssociationCall(List<ClientRoleDTO> roleList, String serviceAccountId, HttpMethod httpMethod, final String message, AppLcmError appLcmError) {
        String token = Objects.requireNonNull(
                getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD,"").getBody()).getAccessToken();
        final String url = urlGenerator.generateAssociateRoleUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER, serviceAccountId);
        log.debug("Send Request to Keycloak. URL:{}", url);
        try {
            return requestHandler.sendRestRequestToKeycloak(roleList, url,
                    List.class, MediaType.APPLICATION_JSON,httpMethod,token);
        } catch (final RestClientException exception) {
            log.error(message, exception.getMessage());
            throw new KeycloakException(appLcmError, message, url, exception);
        }
    }
}
