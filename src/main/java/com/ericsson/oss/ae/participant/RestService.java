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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.*;

import com.ericsson.oss.ae.model.participant.RestRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class used to call Rest endpoint.
 */
@Service
@Slf4j
public class RestService<T> {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Calls a rest endpoint.
     *
     * @param restRequest
     *            request to be sent.
     * @return Returns response from given request.
     */
    @ResponseBody
    public ResponseEntity<T> callRestEndpoint(final RestRequest restRequest) {
        log.info("Making Rest API {} call for url: {}", restRequest.getRequestMethod(), restRequest.getUrl());
        try {
            ResponseEntity<T> restResponse = restTemplate.exchange(restRequest.getUrl(), restRequest.getRequestMethod(), 
                                                                   restRequest.getRequest(), restRequest.getResponseType());
            log.info("Response received for {} request to url {}. Response [{}]", 
                     restRequest.getRequestMethod(), restRequest.getUrl(), restResponse.getBody());
            return restResponse;
        } catch (final RestClientException exception) {
            final String message = "Call to Rest Endpoint has failed. RestClientException message: " 
              + exception.getMessage();
            log.error(message, exception);
            throw exception;
        }
    }
}
