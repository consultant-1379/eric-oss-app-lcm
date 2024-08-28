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

import static com.ericsson.oss.ae.acm.TestConstants.EEFD_0035;
import static com.ericsson.oss.ae.acm.TestConstants.HTTP_SEC_ACCESS_MGMT;
import static com.ericsson.oss.ae.acm.TestConstants.ID_1;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_ID_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_SECRET_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_URL_KEY;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_ID_NAME;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_SECRET_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientScopeDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;

@ExtendWith(MockitoExtension.class)
public class KeycloakHandlerTest {

    private static final String SECRET_TEXT = "secret-text";
    private static final String USER2 = "USER2";

    @Mock
    private KeycloakClientService mockKeycloakClientService;
    @Mock
    private KeycloakUrlGenerator mockKeycloakUrlGenerator;

    private KeycloakHandler keycloakHandlerUnderTest;

    @BeforeEach
    public void setUp() {
        keycloakHandlerUnderTest = new KeycloakHandler(mockKeycloakClientService, mockKeycloakUrlGenerator);
    }

    @Test
    public void testGenerateKeycloakCredentials() {
        // Setup
        final App app = TestUtils.generateAppResponseForCreateInstance();

        final ResponseEntity responseEntity1 = new ResponseEntity<>(TestUtils.getClientDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.getClients()).thenReturn(responseEntity1);
        when(mockKeycloakClientService.createClientId(EEFD_0035)).thenReturn(KEYCLOAK_CLIENT_ID_KEY);

        final CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue(SECRET_TEXT);
        final ResponseEntity responseEntity2 = new ResponseEntity<>(credentialDto, HttpStatus.OK);
        when(mockKeycloakClientService.createSecret(any())).thenReturn(responseEntity2);
        final ResponseEntity responseEntity3 = new ResponseEntity<>(TestUtils.getClientRolesDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.extractRoles()).thenReturn(responseEntity3);

        // Configure KeycloakClientService.getServiceAccount(...).
        final ResponseEntity responseEntity4 = new ResponseEntity<>(TestUtils.getServiceAccount(), HttpStatus.OK);
        when(mockKeycloakClientService.getServiceAccount(any())).thenReturn(responseEntity4);

        final ClientCredential result = keycloakHandlerUnderTest.generateKeycloakCredentials(app);

        assertThat(result.getClientId()).isEqualTo(KEYCLOAK_CLIENT_ID_KEY);
        assertThat(result.getClientSecret()).isEqualTo(SECRET_TEXT);
        verify(mockKeycloakClientService).createClient(any());
        verify(mockKeycloakClientService).associateRoles(any(), any());
    }

    @Test
    public void testCreateAppInstance_success_with_no_roles() throws Exception {
        // Setup
        final CreateAppInstanceRequest createAppInstanceRequest = new CreateAppInstanceRequest();
        // Configure AppRepository.findById(...).
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setRoles(null);
        when(mockKeycloakClientService.createClientId(EEFD_0035)).thenReturn(KEYCLOAK_CLIENT_ID_KEY);

        // Configure KeycloakClientService.getClients(...).
        final ResponseEntity responseEntity1 = new ResponseEntity<>(TestUtils.getClientDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.getClients()).thenReturn(responseEntity1);

        // Configure KeycloakClientService.createSecret(...).
        final CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue(SECRET_TEXT);
        final ResponseEntity responseEntity2 = new ResponseEntity<>(credentialDto, HttpStatus.OK);
        when(mockKeycloakClientService.createSecret(any())).thenReturn(responseEntity2);
        final ResponseEntity responseEntity5 = new ResponseEntity<>(HttpStatus.OK);
        when(mockKeycloakClientService.createClient(any())).thenReturn(responseEntity5);
        final ClientCredential result = keycloakHandlerUnderTest.generateKeycloakCredentials(app);

        // Verify the results
        assertThat(result.getClientId()).isEqualTo(KEYCLOAK_CLIENT_ID_KEY);
        assertThat(result.getClientSecret()).isEqualTo(SECRET_TEXT);
    }


    @Test
    public void testCreateAppInstance_ThrowsAppLcmException_ClientId_Not_Found() throws URISyntaxException {
        final App app = TestUtils.generateAppResponseForCreateInstance();
        when(mockKeycloakClientService.createClientId(EEFD_0035)).thenReturn(KEYCLOAK_CLIENT_ID_KEY);

        // Configure KeycloakClientService.getClients(...).
        final ResponseEntity responseEntity1 = new ResponseEntity<>(TestUtils.getClientDtos(), HttpStatus.OK);
        final ClientDto clientDto = TestUtils.getClientDto();
        clientDto.setId("3");
        clientDto.setClientId(CLIENT_ID_NAME);
        final ResponseEntity responseEntity6 = new ResponseEntity<>(new ClientDto[]{clientDto}, HttpStatus.OK);
        when(mockKeycloakClientService.getClients()).thenReturn(responseEntity6);
        // Configure KeycloakClientService.createSecret(...).
        final CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue(SECRET_TEXT);

        final ResponseEntity responseEntity5 = new ResponseEntity<>(HttpStatus.OK);
        when(mockKeycloakClientService.createClient(any())).thenReturn(responseEntity5);

        // Run the test
        assertThatThrownBy(() -> keycloakHandlerUnderTest.generateKeycloakCredentials(app))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testCreateAppInstance_no_matching_roles_throwsAppLcmException() throws URISyntaxException {
        // Configure AppRepository.findById(...).
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.getRoles().get(0).setName(USER2);

        when(mockKeycloakClientService.createClientId(EEFD_0035)).thenReturn(KEYCLOAK_CLIENT_ID_KEY);

        // Configure KeycloakClientService.getClients(...).
        final ResponseEntity responseEntity1 = new ResponseEntity<>(TestUtils.getClientDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.getClients()).thenReturn(responseEntity1);
        // Configure KeycloakClientService.createSecret(...).
        final CredentialDto credentialDto = new CredentialDto();
        credentialDto.setValue(SECRET_TEXT);
        final ResponseEntity responseEntity2 = new ResponseEntity<>(credentialDto, HttpStatus.OK);
        when(mockKeycloakClientService.createSecret(any())).thenReturn(responseEntity2);

        // Configure KeycloakClientService.extractRoles(...).

        final ResponseEntity responseEntity3 = new ResponseEntity<>(TestUtils.getClientRolesDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.extractRoles()).thenReturn(responseEntity3);
        final ResponseEntity responseEntity5 = new ResponseEntity<>(HttpStatus.OK);
        when(mockKeycloakClientService.createClient(any())).thenReturn(responseEntity5);

        // Run the test
        assertThatThrownBy(() -> keycloakHandlerUnderTest.generateKeycloakCredentials(app))
                .isInstanceOf(AppLcmException.class);
        verify(mockKeycloakClientService).deleteClient(any());
    }

    @Test
    public void testMapKeycloakParams() {
        // Setup
        final ClientCredential clientCredential = TestUtils.getClientCredential();
        final Map<String, String> expectedResult = Map.ofEntries(Map.entry(KEYCLOAK_CLIENT_ID_KEY, KEYCLOAK_CLIENT_ID_KEY),
                Map.entry(KEYCLOAK_CLIENT_SECRET_KEY, CLIENT_SECRET_NAME), Map.entry(KEYCLOAK_CLIENT_URL_KEY, HTTP_SEC_ACCESS_MGMT));
        when(mockKeycloakUrlGenerator.generateBasePath()).thenReturn(HTTP_SEC_ACCESS_MGMT);

        final Map<String, String> result = keycloakHandlerUnderTest.mapKeycloakParams(clientCredential);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testRollBackCreatedKeycloakCredentials() {
        // Setup
        final ClientCredential clientCredential = TestUtils.getClientCredential();
        final ResponseEntity responseEntity1 = new ResponseEntity<>(TestUtils.getClientDtos(), HttpStatus.OK);
        when(mockKeycloakClientService.getClients()).thenReturn(responseEntity1);

        // Run the test
        keycloakHandlerUnderTest.rollBackCreatedKeycloakCredentials(clientCredential);

        // Verify the results
        verify(mockKeycloakClientService).deleteClient(ID_1);
    }

    @Test
    public void testRollBackCreatedKeycloakCredentials_KeycloakClientServiceGetClientsReturnsNull() {
        // Setup
        final ClientCredential clientCredential = TestUtils.getClientCredential();
        when(mockKeycloakClientService.getClients()).thenReturn(null);
        // Run the test
        assertThatThrownBy(
                () -> keycloakHandlerUnderTest.rollBackCreatedKeycloakCredentials(clientCredential))
                .isInstanceOf(AppLcmException.class);
    }

}
