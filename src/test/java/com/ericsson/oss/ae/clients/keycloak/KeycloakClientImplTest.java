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

package com.ericsson.oss.ae.clients.keycloak;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.clients.keycloak.dto.*;
import com.ericsson.oss.ae.clients.keycloak.model.Client;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.KeycloakException;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.json.JsonUtils;
import com.ericsson.oss.ae.utils.rest.RequestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, KeycloakClientImpl.class })
class KeycloakClientImplTest {

    @Autowired
    private KeycloakClientImpl objectUnderTest;
    @Autowired
    private UrlGenerator urlGenerator;
    @MockBean
    private RequestHandler requestHandler;

    @SpyBean
    private KeycloakClientImpl keycloakClient;

    @Test
    public void givenAValidTargetForToken_whenABadRequestStatusIsReturnedFromKeycloakForAuthToken_thenExceptionCaughtExpectedErrorResponseReturned() {

        when(requestHandler.sendRestRequestToKeycloak(any(),any(),any(), any(), any(HttpMethod.class),any()))
                .thenThrow(new RestClientException("test"));

        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD, null);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_RETRIEVE_TOKEN);
        assertThat(actualException.getMessage()).isEqualTo("Error extracting or refreshing token ");
    }



    @Test
    public void givenAValidTarget_whenAnOkStatusIsReturnedFromKeycloakForAuthToken_thenResponseEntityReturnedWithCorrectValues() {

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken("ey1234");

        when(requestHandler.sendRestRequestToKeycloak(any(),any(),any(), any(), any(HttpMethod.class),any()))
                .thenReturn(new ResponseEntity<>(tokenDto,
                HttpStatus.OK));

        final ResponseEntity<TokenDto> actualOperationResponse = objectUnderTest.getAuthenticationToken(KeycloakConstants.KEYCLOAK_GRANT_TYPE_PASSWORD, null);
        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualOperationResponse.getBody().getAccessToken()).isEqualTo("ey1234");

    }

    @Test
    public void givenAValidGrant_whenAnOkStatusIsReturnedFromKeycloakForClientCreation_thenKeycloakReturnsCorrectResponse() {

        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        Client client = new Client();
        when(requestHandler.sendRestRequestToKeycloak(eq(client),any(),any(), any(),any(),any()))
                .thenReturn(new ResponseEntity(HttpStatus.OK));
        final ResponseEntity actualOperationResponse = objectUnderTest.createClient(client);

        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void givenAValidGrant_whenABadRequestStatusIsReturnedFromKeycloakForClientCreation_thenExceptionCaughtExpectedErrorResponseReturned() {
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),any(), any(), any(HttpMethod.class),eq(token))).thenThrow(new RestClientException("test"));
        Client client = new Client();
        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.createClient(client);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_CREATE_CLIENT);
        assertThat(actualException.getMessage()).isEqualTo("Error creating client ");
    }

    @Test
    public void givenGetRequestForKeycloakId_whenRequestProcessedSuccessfully_thenClientsReturned() {

        ClientDto clientDto = new ClientDto();
        clientDto.setClientId("rapp_12345");
        clientDto.setId("1234");
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any())).thenReturn(new ResponseEntity(clientDto,HttpStatus.OK));
        final ResponseEntity<ClientDto[]> actualOperationResponse = objectUnderTest.getClients();


        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);


    }

    @Test
    public void givenGetRequestForKeycloakId_whenRequestProcessedSuccessfully_thenKeycloakReturnsCorrectResponse() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any()))
                .thenReturn(new ResponseEntity(HttpStatus.OK));
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));

        final ResponseEntity actualOperationResponse = objectUnderTest.getClients();

        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenGetRequestForKeycloakId_whenABadRequestStatusIsReturnedFromKeycloakForGetClients_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any())).thenThrow(new RestClientException("test"));
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.getClients();
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT);
        assertThat(actualException.getMessage()).isEqualTo("Error extracting clients ");
    }

    @Test
    public void givenAValidId_whenABadRequestStatusIsReturnedFromKeycloakForCreateSecret_thenExceptionCaughtExpectedErrorResponseReturned() {
        String token = "ey12234";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.sendRestRequestToKeycloak(eq(null),any(),any(), any(), any(),any())).thenThrow(new RestClientException("test"));
        String id = "234543";

        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.createSecret(id);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_CREATE_SECRET);
        assertThat(actualException.getMessage()).isEqualTo("Error extracting or creating secret ");
    }


    @Test
    public void givenAValidId_whenAnOkStatusIsReturnedFromKeycloakForCreateSecret_thenResponseEntityReturnedWithCorrectValues() {

        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue("secret12345");
        String token = "ey12234";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        String id = "234543";
        final ResponseEntity expectedOperationResponse = new ResponseEntity(
                credentialDto, HttpStatus.OK);
        when(requestHandler.sendRestRequestToKeycloak(eq(null),any(),any(), any(), any(),any())).thenReturn(expectedOperationResponse);

        final ResponseEntity<CredentialDto> actualOperationResponse = objectUnderTest.createSecret(id);
        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualOperationResponse.getBody().getValue()).isEqualTo("secret12345");

    }

    @Test
    public void givenAValidId_whenCreatingClientId_thenReturnedWithCorrectValues() {

       String prefix = "rappid--";
       String clientId =  "ClientId";
       String appOnBoardingId =  "1234";

       final String actualOperationResponse = objectUnderTest.createClientId(appOnBoardingId);
       assertThat(actualOperationResponse).contains(prefix + appOnBoardingId);
       assertThat(actualOperationResponse.length()).isNotEqualTo((prefix+clientId).length());

    }

    @Test
    public void givenAValidClient_whenCreatingClientWithAttribute_thenReturnedWithCorrectValues() {

        Client client = (Client) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",Client.class);
        String token = "ey12234";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        final ResponseEntity<Object> expectedOperationResponse = new ResponseEntity<>(HttpStatus.OK);
        String url = "http://eric-sec-access-mgmt-http:8080/admin/realms/master/clients";
        when(requestHandler.sendRestRequestToKeycloak(eq(client),any(),any(), any(), any(HttpMethod.class),any())).thenReturn(expectedOperationResponse);

        final ResponseEntity actualOperationResponse = objectUnderTest.createClient(client);
        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenAnInvalidClient_whenCreatingClientWithAttribute_thenExceptionCaughtExpectedErrorResponseReturned() {

        Client client = (Client) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientJsonOk.json",Client.class);
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.sendRestRequestToKeycloak(eq(client),any(),any(), any(), any(HttpMethod.class),any())).thenThrow(new RestClientException("test"));

        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.createClient(client);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_CREATE_CLIENT);
        assertThat(actualException.getMessage()).isEqualTo("Error creating client ");
    }


    @Test
    public void givenAValidClient_whenDeleting_thenReturnedWithCorrectValues() {

        ClientScopeDto clientScopeDto = (ClientScopeDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientScopeDto.json",ClientScopeDto.class);
        final ResponseEntity<Object> expectedOperationResponse = new ResponseEntity<>(HttpStatus.OK);
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        String url = "http://eric-sec-access-mgmt-http:8080/admin/realms/master/client-scopes";
        when(requestHandler.createAndSendRestRequest(any(),any(),any(), any(), any())).thenReturn(expectedOperationResponse);

        final ResponseEntity actualOperationResponse = objectUnderTest.deleteClient(clientScopeDto.getName());
        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenAnInvalidClient_whenDeleting_thenExceptionCaughtExpectedErrorResponseReturned() {
        ClientScopeDto clientScopeDto = (ClientScopeDto) JsonUtils.getObjectFromJsonFile("expectedresponses/keycloak/ClientScopeDto.json",ClientScopeDto.class);
        when(requestHandler.createAndSendRestRequest(any(),any(), any(), any(),any())).thenThrow(new RestClientException("test"));
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        final KeycloakException actualException = assertThrows(KeycloakException.class, () -> {
            objectUnderTest.deleteClient(clientScopeDto.getName());
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.APP_LCM_ERROR_CLIENT_DELETION);
        assertThat(actualException.getMessage()).isEqualTo("Error deleting client with id "+clientScopeDto.getName());

    }

    @Test
    public void givenGetRequestForRealmRoles_whenRequestProcessedSuccessfully_thenKeycloakReturnsCorrectResponse() {
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any()))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        final ResponseEntity actualOperationResponse = objectUnderTest.extractRoles();

        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenGetRequestForServiceAccount_whenRequestProcessedSuccessfully_thenKeycloakReturnsCorrectResponse() {
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        String clientId = "clientIdTests";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any()))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        final ResponseEntity actualOperationResponse = objectUnderTest.getServiceAccount(clientId);

        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenAListOfRoles_whenAssociatingRoles_thenReturn200Ok() {
        ClientRoleDTO clientRoleDto = new ClientRoleDTO();
        clientRoleDto.setName("RoleTest");
        clientRoleDto.setId("testId");
        final ResponseEntity<Void> expectedOperationResponse = new ResponseEntity<>(HttpStatus.OK);
        String token = "ey12234";
        String saId = "serviceAccountIdTest";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(token);
        List<ClientRoleDTO> clientRoleDTOList = new ArrayList<>();
        clientRoleDTOList.add(clientRoleDto);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
        when(requestHandler.sendRestRequestToKeycloak(eq(clientRoleDTOList),any(),any(), any(), any(HttpMethod.class),any())).thenReturn(expectedOperationResponse);
        final ResponseEntity actualOperationResponse = objectUnderTest.associateRoles(clientRoleDTOList, saId);
        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenEmptyClientsInKeycloak_ThrowException() {
        mocktokenExtraction();
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any())).thenReturn(new ResponseEntity<>(new ClientDto[]{},HttpStatus.OK));

        final ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
            objectUnderTest.deleteClientByClientId("clientId");
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.APP_LCM_ERROR_CLIENT_DELETION);
    }

    @Test
    public void givenNullClientsInKeycloak_ThrowException() {
        mocktokenExtraction();
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any())).thenReturn(new ResponseEntity<>(null,HttpStatus.OK));
        assertThrows(NullPointerException.class, () -> {
            objectUnderTest.deleteClientByClientId("clientId");
        });
    }
    @Test
    public void whenClientIsPresent_deleteClientNyClientId() {
        ClientDto[] clientDtos = new ClientDto[1];
        ClientDto clientDto = new ClientDto();
        clientDto.setName("namex");
        clientDto.setId("id");
        clientDto.setClientId("clientId");
        clientDtos[0] = clientDto;
        mocktokenExtraction();
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any(),any())).thenReturn(new ResponseEntity<>(clientDtos,HttpStatus.OK));
        objectUnderTest.deleteClientByClientId("clientId");
        verify(keycloakClient, times(1)).deleteClient("id");
    }
    private void mocktokenExtraction() {
        TokenDto tokenDto = new TokenDto();
        String token = "ey12234";
        tokenDto.setAccessToken(token);
        when(requestHandler.sendRestRequestToKeycloak(any(),any(),eq(TokenDto.class),any(),any())).thenReturn(new ResponseEntity<>(tokenDto,HttpStatus.OK));
    }
}