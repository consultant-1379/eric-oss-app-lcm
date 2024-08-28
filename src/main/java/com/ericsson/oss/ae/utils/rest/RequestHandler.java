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

package com.ericsson.oss.ae.utils.rest;

import com.ericsson.oss.ae.model.participant.RestRequest;
import com.ericsson.oss.ae.participant.RestService;
import com.ericsson.oss.ae.participant.command.ParticipantCommand;
import com.ericsson.oss.ae.participant.concretecommand.HelmParticipantCommand;
import com.ericsson.oss.ae.participant.invoker.ParticipantCommandInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RequestHandler uses ParticipantCommandInvoker to send rest requests.
 */
@RestController
@Slf4j
public class RequestHandler<T> {

    private static final String PROPAGATED_HEADER_KEY = "propagated";

    private static final String PROPAGATED_HEADER_VALUE = "lcm";

    @Autowired
    private RestService restService;

    @Autowired
    private ParticipantCommandInvoker participantCommandInvoker;

    /**
     * Generates and sends a rest request to a given url.
     *
     * @param url
     *            String Url used to build request.
     * @param httpMethod
     *            The Http method used for the request (GET or DELETE).
     * @return Returns get request response
     */
    @ResponseBody
    public ResponseEntity<Object> createAndSendRestRequest(final String url, final Class responseType, final MediaType mediaType,
                                                           final HttpMethod httpMethod,String... bearer) {
        log.debug("Create And Send Rest Request for response type: {}", responseType);
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(mediaType));
        headers.set(PROPAGATED_HEADER_KEY, PROPAGATED_HEADER_VALUE);
        if(bearer!=null && !Arrays.stream(bearer).collect(Collectors.toList()).isEmpty()) {
            Optional.ofNullable(bearer[0]).ifPresent(item ->
                                                         headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearer[0]));
        }
        final HttpEntity<String> model = new HttpEntity<>(headers);
        final RestRequest restRequest = new RestRequest();
        restRequest.setRequestMethod(httpMethod);
        restRequest.setUrl(url);
        restRequest.setRequest(model);
        restRequest.setResponseType(responseType);

        return restService.callRestEndpoint(restRequest);
    }

    /**
     * Generates and sends a Post rest request to a given url using Participant.
     *
     * @param requestBodyMap
     *            Body of post request.
     * @param url
     *            String Url used to build request.
     * @return Returns get request response
     */
    @ResponseBody
    public ResponseEntity<Object> sendRestRequestUsingParticipant(final MultiValueMap<String, Object> requestBodyMap, final String url,
                                                                  final Class responseType, final HttpMethod httpMethod) {
        log.info("Send Rest Request Using Participant, http method: {}, url: {}", httpMethod, url);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final HttpEntity<MultiValueMap<String, Object>> model = new HttpEntity<>(requestBodyMap, headers);
        final RestRequest restRequest = new RestRequest();
        restRequest.setRequestMethod(httpMethod);
        restRequest.setUrl(url);
        restRequest.setRequest(model);
        restRequest.setResponseType(responseType);

        final ParticipantCommand helmParticipantCommand = new HelmParticipantCommand(restService, restRequest);
        participantCommandInvoker = new ParticipantCommandInvoker(helmParticipantCommand);
        return participantCommandInvoker.executeHelmCommand();
    }
    @ResponseBody
    public ResponseEntity<T> sendRestRequestToKeycloak(final Object requestBodyMap, final String url,
                                                            final Class responseType, final MediaType mediaType , final HttpMethod httpMethod, String... bearer) {
        log.info("Send Rest Request To Keycloak, http method: {}", httpMethod);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if(bearer!=null && !Arrays.stream(bearer).collect(Collectors.toList()).isEmpty()) {
            Optional.ofNullable(bearer[0]).ifPresent(item ->
                                                         headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearer[0]));
        }
        final HttpEntity<Object> model = new HttpEntity<>(requestBodyMap, headers);
        final RestRequest restRequest = new RestRequest();
        restRequest.setRequestMethod(httpMethod);
        restRequest.setUrl(url);
        restRequest.setRequest(model);
        restRequest.setResponseType(responseType);

        final ParticipantCommand helmParticipantCommand = new HelmParticipantCommand(restService, restRequest);
        participantCommandInvoker = new ParticipantCommandInvoker(helmParticipantCommand);
        return participantCommandInvoker.executeHelmCommand();
    }

    /**
     * Send simple body rest request response entity.
     *
     * @param body         the body
     * @param url          the url
     * @param responseType the response type
     * @param httpMethod   the http method
     * @param bearer       the bearer
     * @return the response entity
     */
    @ResponseBody
    public ResponseEntity<Object> sendSimpleBodyRestRequest(final Map<String, Object> body, final String url, final Class responseType,
                                                            final HttpMethod httpMethod, String... bearer){
        log.info("Send Simple Body Rest Request for response type: {}", responseType);
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(PROPAGATED_HEADER_KEY, PROPAGATED_HEADER_VALUE);
        if(bearer!=null && !Arrays.stream(bearer).collect(Collectors.toList()).isEmpty()){
            Optional.ofNullable(bearer[0]).ifPresent(item -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearer[0]));
        }
        final HttpEntity<Map<String, Object>> model = new HttpEntity<>(body, headers);
        final RestRequest restRequest = new RestRequest();
        restRequest.setRequestMethod(httpMethod);
        restRequest.setUrl(url);
        restRequest.setRequest(model);
        restRequest.setResponseType(responseType);
        return restService.callRestEndpoint(restRequest);
    }
}
