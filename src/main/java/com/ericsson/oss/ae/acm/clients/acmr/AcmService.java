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

import java.util.UUID;

import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcInstanceResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionDefinition;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployOrderType;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployRequest;
import com.ericsson.oss.ae.acm.clients.acmr.dto.PrimeOrderType;
import com.ericsson.oss.ae.acm.clients.acmr.dto.PrimeRequest;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.rest.RequestBuilder;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;
import com.ericsson.oss.ae.acm.common.rest.RestRequest;
import com.ericsson.oss.ae.acm.utils.MapperUtil;

/**
 * Provides access to the ACM-R services and handles the response as needed for LCM.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AcmService {

    @Autowired
    private final AcmUrlGenerator acmUrlGenerator;

    @Autowired
    private final RequestBuilder requestBodyBuilder;

    @Autowired
    private final RestClientService restClientService;

    @Autowired
    private final MapperUtil mapperUtil;

    @Value("${ACM_AUTH_TOKEN:runtimeUser:none}")
    private String authToken;


    /**
     * Commission the AC Type definition in AM runtime.
     *
     * @param requestContent
     * @return
     */
    public AcCommissionResponse commissionAutomationCompositionType(final String requestContent) throws RestRequestFailedException{
        log.info("Call ACM-R to create composition details");
        final String url = acmUrlGenerator.getAcmCompositionUrl();
        final ResponseEntity<Object> responseEntity = callRestEndpoint(url, requestContent, AcCommissionResponse.class,
                new MediaType("application", "yaml"), HttpMethod.POST);
        return (AcCommissionResponse) responseEntity.getBody();
    }

    /**
     * Gets the AutomationCompositionDefinition for a given compositionId from ACM-R.
     *
     * @param compositionId identity of the composition
     * @return the AC Type definition
     */
    public AutomationCompositionDefinition getAutomationCompositionType(final UUID compositionId) throws RestRequestFailedException{
        log.info("Call ACM-R to Get the composition details for compositionId {}", compositionId);

        final String url = String.format("%s/%s", acmUrlGenerator.getAcmCompositionUrl(), compositionId);
        final ResponseEntity<Object> responseEntity = callRestEndpointWithoutRequestBody(url, AutomationCompositionDefinition.class, HttpMethod.GET);
        return (AutomationCompositionDefinition) responseEntity.getBody();
    }

    public AcInstanceResponse commissionAutomationCompositionInstance(final String requestContent, final UUID acmCompositionId) throws RestRequestFailedException{
        log.info("Call ACM-R to create composition instance");
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(acmCompositionId);
        final ResponseEntity<Object> responseEntity = callRestEndpoint(url, requestContent, AcInstanceResponse.class, new MediaType("application", "yaml"), HttpMethod.POST);
        return (AcInstanceResponse) responseEntity.getBody();
    }

    public AutomationCompositionInstance getAutomationCompositionInstance(final UUID compositionId, final UUID compositionInstanceId) throws RestRequestFailedException {
        log.info("Call ACM-R to get composition instance details");
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(compositionId, compositionInstanceId);
        final ResponseEntity<Object> responseEntity = callRestEndpointWithoutRequestBody(url, AutomationCompositionInstance.class, HttpMethod.GET);
        return (AutomationCompositionInstance) responseEntity.getBody();
    }

    /**
     * Request to PRIME the AC Type Definition for the given compositionId in ACM-R. ACM-R will respond with 202 Accepted and will then continue
     * processing the request. The LCM polling functionality will poll ACM-R to determine the overall result of PRIME request in ACM-R and the
     * Participant.
     *
     * @param compositionId identity of the composition to be Primed
     */
    public void primeAutomationCompositionType(final UUID compositionId) throws RestRequestFailedException{
        log.info("Requesting ACM-R to prime the composition type with id {}", compositionId);
        final String url = acmUrlGenerator.getAcmCompositionUrlWithCompositionId(compositionId);
        final PrimeRequest primeRequest = new PrimeRequest();
        primeRequest.setPrimeOrder(PrimeOrderType.PRIME);
        final ResponseEntity<Object> response = callRestEndpoint(url, mapperUtil.parseObjectToString(primeRequest), null, MediaType.APPLICATION_JSON, HttpMethod.PUT);
        log.info("Received successful response from ACM-R for prime request, composition id {}, status {}", compositionId,
                response.getStatusCode());
    }

    /**
     * Request to De-PRIME the AC Type Definition for the given compositionId in ACM-R. ACM-R will respond with 202 Accepted and will then continue
     * processing the request. The LCM polling functionality will poll ACM_R to determine the overall result of DE-PRIME request in ACM-R and the
     * Participant.
     *
     * @param compositionId identity of the composition to be De-Primed
     */
    public void dePrimeAutomationCompositionType(final UUID compositionId) throws RestRequestFailedException{
        log.info("Requesting ACM-R to de-prime the composition type with id {}", compositionId);
        final String compositionUrl = acmUrlGenerator.getAcmCompositionUrlWithCompositionId(compositionId);
        final PrimeRequest primeRequest = new PrimeRequest();
        primeRequest.setPrimeOrder(PrimeOrderType.DEPRIME);
        final ResponseEntity<Object> response = callRestEndpoint(compositionUrl, mapperUtil.parseObjectToString(primeRequest), null, MediaType.APPLICATION_JSON, HttpMethod.PUT);
        log.info("Received successful response from ACM-R for De-prime request, composition id: {}, status: {}", compositionId, response.getStatusCode());
    }

    /**
     * Deletes the AutomationCompositionDefinition for a given compositionId from ACM-R.
     *
     * @param compositionId identity of the composition
     */
    public void deleteAutomationCompositionType(final UUID compositionId) throws RestRequestFailedException{
        log.info("Call ACM-R to delete the composition details for compositionId {}", compositionId);
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + compositionId;
        callRestEndpointWithoutRequestBody(url, AcCommissionResponse.class, HttpMethod.DELETE);
    }

    /**
     * Request to DEPLOY the AutomationCompositionInstance for the given compositionId and instanceId in ACM-R. ACM-R will respond with 202 Accepted and will then continue
     * processing the request. The LCM polling functionality will poll ACM-R to determine the overall result of DEPLOY request in ACM-R and the
     * Participant.
     *
     * @param compositionId Automation Composition definition id
     * @param instanceId Automation Composition instance id
     */
    public void deployAutomationCompositionInstance(final UUID compositionId, final UUID instanceId) throws RestRequestFailedException{
        log.info("Requesting ACM-R to deploy the instance with instance id: {}", instanceId);
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(compositionId, instanceId);
        final DeployRequest deployRequest = new DeployRequest();
        deployRequest.setDeployOrder(DeployOrderType.DEPLOY);
        final ResponseEntity<Object> response = callRestEndpoint(url, mapperUtil.parseObjectToString(deployRequest), null, MediaType.APPLICATION_JSON, HttpMethod.PUT);
        log.info("Received successful response from ACM-R for deploy request, instance id: {}, status: {}", instanceId,
                response.getStatusCode());
    }

    /**
     * Request to UPDATE the AutomationCompositionInstance for the given compositionId and instanceId in ACM-R. ACM-R will respond with 200 Ok.
     *
     * @param requestContent Request content
     * @param acmCompositionId Automation Composition definition id
     */
    public AcInstanceResponse updateAutomationCompositionInstance(final String requestContent, final UUID acmCompositionId) throws RestRequestFailedException{
        log.info("Requesting ACM-R to update the instance with composition id: {}", acmCompositionId);
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(acmCompositionId);
        final ResponseEntity<Object> responseEntity = callRestEndpoint(url, requestContent, AcInstanceResponse.class, new MediaType("application", "yaml"), HttpMethod.POST);
        return (AcInstanceResponse) responseEntity.getBody();
    }

    /**
     * Request to UNDEPLOY the AutomationCompositionInstance for the given compositionId and instanceId in ACM runtime. ACM will respond with 202 Accepted and will then continue
     * processing the request. The LCM polling functionality will poll ACM to determine the overall result of UNDEPLOY request in ACM and the
     * Participant.
     *
     * @param compositionId Automation Composition definition id
     * @param instanceId Automation composition instance id
     */
    public void undeployAutomationCompositionInstance(final UUID compositionId, final UUID instanceId) throws RestRequestFailedException {
        log.info("Requesting ACM-R to undeploy the instance with instance id {}", instanceId);
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(compositionId, instanceId);
        final DeployRequest deployRequest = new DeployRequest();
        deployRequest.setDeployOrder(DeployOrderType.UNDEPLOY);
        final ResponseEntity<Object> response = callRestEndpoint(url, mapperUtil.parseObjectToString(deployRequest), null, MediaType.APPLICATION_JSON, HttpMethod.PUT);
        log.info("Received successful response from ACM for undeploy request, instance id {}, status {}", instanceId,
                response.getStatusCode());
    }

    /**
     * Deletes the AutomationCompositionInstance for a given compositionId and acmInstanceId from ACM-R.
     *
     * @param compositionId
     *     identity of the composition
     * @param acmInstanceId
     *     identity of the composition instance
     */
    public AcInstanceResponse deleteAutomationCompositionInstance(final UUID compositionId, final UUID acmInstanceId) throws RestRequestFailedException {
        log.info("deleteAutomationCompositionInstance() Call ACM-R to delete the acm instance details for compositionId {} and acmInstanceId {}", compositionId, acmInstanceId);
        final String url = acmUrlGenerator.generateUrlForSpecificAcmCompositionInstance(compositionId, acmInstanceId);
        final ResponseEntity<Object> response = callRestEndpointWithoutRequestBody(url, AcInstanceResponse.class, HttpMethod.DELETE);
        return (AcInstanceResponse) response.getBody();
    }

    /**
     * Get the AutomationCompositionInstances for a given compositionId from ACM runtime.
     *
     * @param compositionId
     *     identity of the composition
     */
    public AutomationCompositions getAllAutomationCompositionInstancesForCompositionId(final UUID compositionId) throws RestRequestFailedException {
        log.info("Call ACM-R to get all composition instance details");
        final String url = acmUrlGenerator.generateAcmCompositionUrlWithInstance(compositionId);
        final ResponseEntity<Object> responseEntity = callRestEndpointWithoutRequestBody(url, AutomationCompositions.class, HttpMethod.GET);
        return (AutomationCompositions) responseEntity.getBody();
    }

    private ResponseEntity<Object> callRestEndpoint(final String url, final String requestBody, final Class responseType, final MediaType contentMediaType, final HttpMethod method) {
        final RestRequest restRequest = requestBodyBuilder.createRequestContent(url, requestBody, responseType, contentMediaType, method, null, authToken);
        final ResponseEntity<Object> response = restClientService.callRestEndpoint(restRequest);
        log.info("Response from ACM-R with status {} and body {}", response.getStatusCode(), mapperUtil.parseObjectToString(response.getBody()));
        return response;
    }

    private ResponseEntity<Object> callRestEndpointWithoutRequestBody(final String url, final Class responseType, final HttpMethod method) {
        final RestRequest restRequest = requestBodyBuilder.createRequestContentWithNoBody(url, responseType, MediaType.APPLICATION_JSON, method, null, authToken);
        final ResponseEntity<Object> response = restClientService.callRestEndpoint(restRequest);
        log.info("Response from ACM-R with status {} and body {}", response.getStatusCode(), mapperUtil.parseObjectToString(response.getBody()));
        return response;
    }

}
