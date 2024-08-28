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

package com.ericsson.oss.ae.participant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.model.participant.RestRequest;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { AppLcmApplication.class, RestService.class })
public class RestServiceTest {

    private static final String HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL = "http://localhost:8080/cnwlcm/v1/workload_instances";

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private RestService restService;

    @BeforeAll
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
    }

    @Test
    public void givenGetMethod_WhenCallValidRestEndpoint_ThenRetrieveValidResponse() throws URISyntaxException {
        final RestRequest restRequest = createRestRequest(HttpMethod.GET, HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL);

        // given:
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL))).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON));

        // when:
        final ResponseEntity response = restService.callRestEndpoint(restRequest);

        mockServer.verify();

        // then:
        BDDAssertions.then(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void givenDeleteMethod_WhenCallValidRestEndpoint_ThenRetrieveValidResponse() throws URISyntaxException {
        final RestRequest restRequest = createRestRequest(HttpMethod.DELETE, HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL);

        // given:
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL))).andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON));

        // when:
        final ResponseEntity response = restService.callRestEndpoint(restRequest);

        mockServer.verify();

        // then:
        BDDAssertions.then(response.getStatusCodeValue()).isEqualTo(HttpStatus.ACCEPTED.value());
    }

    @Test
    public void givenGetMethod_WhenCallBadRestEndpoint_ThenRetrieveBadResponse() throws URISyntaxException {

        // given:
        final RestRequest restRequest = createRestRequest(HttpMethod.GET, HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL))).andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        // when:
        final RestClientException exception = assertThrows(RestClientException.class, () -> {
            restService.callRestEndpoint(restRequest);
        });

        // then:
        BDDAssertions.then(exception.getMessage()).contains("400 Bad Request: [no body]");
    }

    @Test
    public void givenDeleteMethod_WhenCallBadRestEndpoint_ThenRetrieveBadResponse() throws URISyntaxException {
        final RestRequest restRequest = createRestRequest(HttpMethod.DELETE, HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL);

        // given:
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(HELM_ORCHESTRATOR_WORKLOAD_INSTANCES_URL))).andExpect(method(HttpMethod.DELETE))
                .andRespond(withBadRequest());

        // when:
        final RestClientException exception = assertThrows(RestClientException.class, () -> {
            restService.callRestEndpoint(restRequest);
        });

        // then:
        BDDAssertions.then(exception.getMessage()).contains("400 Bad Request: [no body]");
    }

    private RestRequest createRestRequest(final HttpMethod httpMethod, final String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final HttpEntity<String> request = new HttpEntity<>(headers);
        final RestRequest restRequest = new RestRequest();
        restRequest.setRequestMethod(httpMethod);
        restRequest.setUrl(url);
        restRequest.setRequest(request);
        return restRequest;

    }
}
