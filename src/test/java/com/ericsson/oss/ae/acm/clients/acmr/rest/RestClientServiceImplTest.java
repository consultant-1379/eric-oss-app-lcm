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

package com.ericsson.oss.ae.acm.clients.acmr.rest;

import static com.ericsson.oss.ae.acm.TestConstants.COMPOSITION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.common.rest.RestRequest;
import com.ericsson.oss.ae.acm.presentation.controller.AppsController;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AppLcmApplication.class, AppsController.class})
@EnableRetry
class RestClientServiceImplTest {

    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AcmRestClientServiceImpl restClientServiceUnderTest;

    @Autowired
    private AcmUrlGenerator acmUrlGenerator;
    RestRequest acmRequest;


    @BeforeEach
    void setUp() {
        String url = acmUrlGenerator.getAcmCompositionUrl();
        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> model = new HttpEntity<>("", headers);
        acmRequest = new RestRequest(url, HttpMethod.POST, model, AcCommissionResponse.class);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
    }

    @Test
    public void testCallRestEndpoint() throws Exception {
        String url = acmUrlGenerator.getAcmCompositionUrl();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST)).andRespond(
                withSuccess(new ObjectMapper().writeValueAsString(TestUtils.generateCreateAutomationCompositionResponse()), MediaType.APPLICATION_JSON));

        ResponseEntity message = restClientServiceUnderTest.callRestEndpoint(acmRequest);
        assertThat(message.getStatusCode()).isEqualTo(HttpStatus.OK);
        AcCommissionResponse acmCreateCompositionResponseResponse = (AcCommissionResponse) message.getBody();
        assertThat(message.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acmCreateCompositionResponseResponse.getCompositionId()).isEqualTo(COMPOSITION_ID);
    }
}
