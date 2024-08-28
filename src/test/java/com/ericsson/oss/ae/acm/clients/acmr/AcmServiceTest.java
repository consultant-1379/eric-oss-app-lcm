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

package com.ericsson.oss.ae.acm.clients.acmr;

import static com.ericsson.oss.ae.acm.TestConstants.APP_VERSION_1_1_1;
import static com.ericsson.oss.ae.acm.TestConstants.COMPOSITION_ID;
import static com.ericsson.oss.ae.acm.TestConstants.FILE;
import static com.ericsson.oss.ae.acm.TestConstants.POLICY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.acm.TestConstants;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcInstanceResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployOrderType;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployRequest;
import com.ericsson.oss.ae.acm.clients.acmr.dto.ToscaIdentifier;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.rest.RequestBuilder;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;
import com.ericsson.oss.ae.acm.common.rest.RestRequest;
import com.ericsson.oss.ae.acm.utils.MapperUtil;

@ExtendWith(MockitoExtension.class)
public class AcmServiceTest {

    private static final String ACM_V_2_COMPOSITIONS = "http://localhost:8080/onap/policy/clamp/acm/v2/compositions/";
    @Mock
    private AcmUrlGenerator mockAcmUrlGenerator;
    @Mock
    private RequestBuilder mockRestHandler;
    @Mock
    private RestClientService mockRestClientService;

    @Mock
    private MapperUtil mockMapperUtil;

    @InjectMocks
    private AcmService acmServiceUnderTest;
    private String url;
    private UUID INSTANCE_ID;
    @BeforeEach
    public void setUp() {
        url = "http://localhost:8080/onap/policy/clamp/acm/v2/compositions/";
        INSTANCE_ID = UUID.randomUUID();
    }
    @Test
    public void testCreateAcmComposition() throws RestRequestFailedException {
        when(mockAcmUrlGenerator.getAcmCompositionUrl()).thenReturn(ACM_V_2_COMPOSITIONS);

        final ToscaIdentifier toscaIdentifier = new ToscaIdentifier(TestConstants.COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_NAME, TestConstants.COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_VERSION);
        final List<ToscaIdentifier> affectedAutomationCompositions = List.of(toscaIdentifier);
        final AcCommissionResponse acmCreateCompositionResponse = new AcCommissionResponse(COMPOSITION_ID, affectedAutomationCompositions);
        final ResponseEntity responseEntity = new ResponseEntity<>(acmCreateCompositionResponse, HttpStatus.OK);
        when(mockRestClientService.callRestEndpoint(any(RestRequest.class))).thenReturn(responseEntity);

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> model = new HttpEntity<>("", headers);
        when(mockRestHandler.createRequestContent(any(), any(), any(), any(), any(), any(), any())).thenReturn(new RestRequest(ACM_V_2_COMPOSITIONS, HttpMethod.POST, model, AcCommissionResponse.class));

        final AcCommissionResponse result = acmServiceUnderTest.commissionAutomationCompositionType(FILE);

        assertThat(result).isEqualTo(acmCreateCompositionResponse);
    }

    @Test
    public void testCreateAcmCompositionInstance() throws RestRequestFailedException {
        final String url2 = url + COMPOSITION_ID + "/instances";
        when(mockAcmUrlGenerator.generateAcmCompositionUrlWithInstance(COMPOSITION_ID)).thenReturn(url2);

        final ToscaIdentifier toscaIdentifier = new ToscaIdentifier(TestConstants.COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_NAME, TestConstants.COMPOSITION_INSTANCE_TOSCA_IDENTIFIER_VERSION);
        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(INSTANCE_ID, toscaIdentifier);
        final ResponseEntity responseEntity = new ResponseEntity<>(acInstanceResponse, HttpStatus.OK);
        when(mockRestClientService.callRestEndpoint(any(RestRequest.class))).thenReturn(responseEntity);

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> model = new HttpEntity<>("", headers);
        when(mockRestHandler.createRequestContent(any(), any(), any(), any(), any(), any(), any())).thenReturn(new RestRequest(url2, HttpMethod.POST, model, AcCommissionResponse.class));

        final AcInstanceResponse result = acmServiceUnderTest.commissionAutomationCompositionInstance("", COMPOSITION_ID);

        assertThat(result).isEqualTo(acInstanceResponse);
    }

    @Test
    public void testUpdateAcmCompositionInstance() throws RestRequestFailedException, IOException {
        final String url2 = url + COMPOSITION_ID + "/instances";
        when(mockAcmUrlGenerator.generateAcmCompositionUrlWithInstance(COMPOSITION_ID)).thenReturn(url2);

        final ToscaIdentifier affectedAutomationComposition = new ToscaIdentifier(POLICY, APP_VERSION_1_1_1);

        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(INSTANCE_ID, affectedAutomationComposition);

        final ResponseEntity responseEntity = new ResponseEntity<>(acInstanceResponse, HttpStatus.OK);
        when(mockRestClientService.callRestEndpoint(any(RestRequest.class))).thenReturn(responseEntity);

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> model = new HttpEntity<>("", headers);
        when(mockRestHandler.createRequestContent(any(), any(), any(), any(), any(), any(), any())).thenReturn(new RestRequest(url2, HttpMethod.POST, model, AcCommissionResponse.class));

        final String expectedAcmInstance = new String(
                Files.readAllBytes(Paths.get(
                        "src/test/resources/acmfiles/AutomationCompositionInstancePropertiesWithInstanceId.yaml"
                )));

        final AcInstanceResponse result = acmServiceUnderTest.updateAutomationCompositionInstance(expectedAcmInstance, COMPOSITION_ID);

        assertThat(result).isEqualTo(acInstanceResponse);

    }

    @Test
    public void testDeployAcmCompositionInstance() throws RestRequestFailedException, IOException {
        final String url2 = url + COMPOSITION_ID + "/instances/" + INSTANCE_ID;
        when(mockAcmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(COMPOSITION_ID, INSTANCE_ID)).thenReturn(url2);

        final ToscaIdentifier affectedAutomationComposition = new ToscaIdentifier(POLICY, APP_VERSION_1_1_1);

        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(INSTANCE_ID, affectedAutomationComposition);

        final ResponseEntity responseEntity = new ResponseEntity<>(acInstanceResponse, HttpStatus.OK);
        when(mockRestClientService.callRestEndpoint(any(RestRequest.class))).thenReturn(responseEntity);

        final DeployRequest deployRequest = new DeployRequest();
        deployRequest.setDeployOrder(DeployOrderType.DEPLOY);
        when(mockMapperUtil.parseObjectToString(any())).thenReturn(new ObjectMapper().writeValueAsString(deployRequest));

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> model = new HttpEntity<>("", headers);
        when(mockRestHandler.createRequestContent(any(), any(), any(), any(), any(), any(), any())).thenReturn(new RestRequest(url2, HttpMethod.PUT, model, null));


        acmServiceUnderTest.deployAutomationCompositionInstance(COMPOSITION_ID, INSTANCE_ID);

        verify(mockRestClientService).callRestEndpoint(any(RestRequest.class));
        assertThat(acmServiceUnderTest).isNotNull();

    }
}
