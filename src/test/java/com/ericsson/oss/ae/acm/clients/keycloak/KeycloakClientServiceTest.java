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

import static com.ericsson.oss.ae.acm.TestConstants.ID_1;
import static com.ericsson.oss.ae.acm.TestConstants.INTERNAL_SERVER_ERROR;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_ID_KEY;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_ID_PARENTHESIS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.TOKEN;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.acm.clients.keycloak.model.Client;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.rest.RequestBuilder;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;
import com.ericsson.oss.ae.acm.presentation.controller.AppInstancesController;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AppLcmApplication.class, AppInstancesController.class})
@EnableRetry
public class KeycloakClientServiceTest {

    private static final String ACCESS_TOKEN = "accessToken";

    private MockMvc mvc;
    private MockRestServiceServer mockServer;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestClientService mockRestClientService;
    @Autowired
    private KeycloakUrlGenerator mockKeycloakUrlGenerator;
    @Autowired
    private RequestBuilder mockRequestBodyBuilder;

    private KeycloakClientService<String> keycloakClientServiceUnderTest;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        keycloakClientServiceUnderTest = new KeycloakClientService<>(mockRestClientService, mockKeycloakUrlGenerator,
                mockRequestBodyBuilder);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        final String authUrl = mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl();
        TokenDto token = new TokenDto();
        token.setAccessToken(ACCESS_TOKEN);
        mockServer.expect(requestTo(authUrl)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(token), MediaType.APPLICATION_JSON));
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
    }

    @Test
    void testGetClientScope() throws JsonProcessingException {
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()), MediaType.APPLICATION_JSON));
        final ResponseEntity<ClientDto[]> result = keycloakClientServiceUnderTest.getClientScope();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new ObjectMapper().writeValueAsString(result.getBody())).isEqualTo(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()));
    }

    @Test
    void testGetClientScope_throwsAppLcmException() throws JsonProcessingException {
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.getClientScope())
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testGetClientScope_throwsAppLcmException_authentication() throws JsonProcessingException {
        mockServer.reset();
        final String url = mockKeycloakUrlGenerator.generateBearerTokenKeycloakUrl();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.getClientScope())
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testCreateClientScope() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientScopeDto()), MediaType.APPLICATION_JSON));

        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.createClientScope(TestUtils.getClientScopeDto());
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateClientScope_throwsAppLcmException() throws JsonProcessingException {
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.createClientScope(TestUtils.getClientScopeDto()))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testGetClients() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()), MediaType.APPLICATION_JSON));

        // Run the test
        final ResponseEntity<ClientDto[]> result = keycloakClientServiceUnderTest.getClients();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new ObjectMapper().writeValueAsString(result.getBody())).isEqualTo(new ObjectMapper().writeValueAsString(TestUtils.getClientDtos()));
    }

    @Test
    void testGetClients_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.getClients())
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testCreateSecret() throws JsonProcessingException {
        final String url = mockKeycloakUrlGenerator.generateSecretKeycloakUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(new CredentialDto()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<CredentialDto> result = keycloakClientServiceUnderTest.createSecret(ID_1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new ObjectMapper().writeValueAsString(result.getBody())).isEqualTo(new ObjectMapper().writeValueAsString(new CredentialDto()));
    }

    @Test
    void testCreateSecret_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateSecretKeycloakUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.createSecret(ID_1))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testCreateClient() throws JsonProcessingException {
        // Setup
        final Client client = Client.builder().clientId(CLIENT_ID_NAME).build();
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientScopeDto()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.createClient(client);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateClient_throwsAppLcmException() {
        final Client client = Client.builder().clientId(CLIENT_ID_NAME).build();
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.createClient(client))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testCreateClientId() {
        assertThat(keycloakClientServiceUnderTest.createClientId(APP_ID_PARENTHESIS)).isNotNull();
    }

    @Test
    void testExtractRoles() throws JsonProcessingException {
        final String url = mockKeycloakUrlGenerator.generateClientRealmRoleUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientRolesDtos()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<ClientRoleDTO[]> result = keycloakClientServiceUnderTest.extractRoles();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new ObjectMapper().writeValueAsString(result.getBody())).isEqualTo(new ObjectMapper().writeValueAsString(TestUtils.getClientRolesDtos()));
    }

    @Test
    void testExtractRoles_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateClientRealmRoleUrl(KEYCLOAK_REALM_MASTER);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.extractRoles())
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testGetServiceAccount() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateServiceAccountUrl(KEYCLOAK_REALM_MASTER, KEYCLOAK_CLIENT_ID_KEY);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getServiceAccount()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<ServiceAccountDto> result = keycloakClientServiceUnderTest.getServiceAccount(KEYCLOAK_CLIENT_ID_KEY);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new ObjectMapper().writeValueAsString(result.getBody())).isEqualTo(new ObjectMapper().writeValueAsString(TestUtils.getServiceAccount()));
    }

    @Test
    void testGetServiceAccount_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateServiceAccountUrl(KEYCLOAK_REALM_MASTER, KEYCLOAK_CLIENT_ID_KEY);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.getServiceAccount(KEYCLOAK_CLIENT_ID_KEY))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testAssociateRoles() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientRolesDtos()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.associateRoles(Arrays.stream(TestUtils.getClientRolesDtos()).collect(Collectors.toList()), ID_1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testAssociateRoles_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.associateRoles(Arrays.stream(TestUtils.getClientRolesDtos()).collect(Collectors.toList()), ID_1))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testDeassociateRoles() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.getClientRolesDtos()), MediaType.APPLICATION_JSON));
        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.deassociateRoles(Arrays.stream(TestUtils.getClientRolesDtos()).collect(Collectors.toList()), ID_1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDeassociateRoles_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateAssociateRoleUrl(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.deassociateRoles(Arrays.stream(TestUtils.getClientRolesDtos()).collect(Collectors.toList()), ID_1))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testDeleteClient() throws JsonProcessingException {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrlById(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess());
        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.deleteClient(ID_1);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDeleteClient_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateClientKeycloakUrlById(KEYCLOAK_REALM_MASTER, ID_1);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.deleteClient(ID_1))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testDeleteScope() {
        // Setup
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrlByScope(KEYCLOAK_REALM_MASTER, TOKEN);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withSuccess());
        // Run the test
        final ResponseEntity<Void> result = keycloakClientServiceUnderTest.deleteScope(TOKEN);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDeleteScope_throwsAppLcmException() {
        final String url = mockKeycloakUrlGenerator.generateClientScopeKeycloakUrlByScope(KEYCLOAK_REALM_MASTER, TOKEN);
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(
                withServerError().body(INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> keycloakClientServiceUnderTest.deleteScope(TOKEN))
                .isInstanceOf(AppLcmException.class);
    }
}

