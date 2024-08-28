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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.ae.acm.clients.acmr.dto.AcmErrorDetails;
import com.ericsson.oss.ae.acm.common.rest.RestRequest;
import com.ericsson.oss.ae.acm.common.exception.RestRequestException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestRetryException;
import com.ericsson.oss.ae.acm.common.rest.RestClientRetryHandler;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;

/**
 * Class to handle REST requests towards ACM and handle responses.
 */
@Component("acmRestClientService")
@RequiredArgsConstructor
@Slf4j
@Primary
public class AcmRestClientServiceImpl<T> extends RestClientRetryHandler implements RestClientService<T> {

    @Autowired
    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity<T> callRestEndpoint(final RestRequest restRequest) {
        log.info("Sending {} request to ACM-R with path {}", restRequest.getRequestMethod(), restRequest.getUrl());
        try {
            return restTemplate.exchange(restRequest.getUrl(), restRequest.getRequestMethod(), restRequest.getRequest(),
                    restRequest.getResponseType());
        } catch (final HttpClientErrorException ex) {
            final HttpStatus httpStatus = findStatusCodeFromException(ex);
            final String acmErrorMessage = getAcmErrorMessage(ex);
            log.error("ACM-R Exception when calling the endpoint: {}, response status {}, error detail: {}", restRequest.getUrl(), httpStatus, acmErrorMessage, ex);
            throw new RestRequestFailedException(httpStatus, acmErrorMessage);

        } catch (final HttpServerErrorException ex) {
            final HttpStatus httpStatus = findStatusCodeFromException(ex);
            final String acmErrorMessage = getAcmErrorMessage(ex);
            checkForRetryRequest(ex, acmErrorMessage);
            log.error("ACM-R Exception when calling the endpoint: {}, response status {}, error detail: {}", restRequest.getUrl(), httpStatus, acmErrorMessage, ex);
            throw new RestRequestFailedException(httpStatus, acmErrorMessage);

        } catch (final ResourceAccessException ex) {
            log.error("ResourceAccessException when calling the endpoint: {} error detail: {}", restRequest.getUrl(), ex.getMessage(), ex);
            throw new RestRequestRetryException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        } catch (final Exception ex) {
            log.error("Exception when calling the endpoint: {} error detail: {}", restRequest.getUrl(), ex.getMessage(), ex);
            throw new RestRequestFailedException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }

    @Override
    public ResponseEntity<T> generateErrorResponse(final RestRequestException ex, final RestRequest acmRequest) {
        log.info("Retry attempts for rest request have failed. Detail: {}", ex.getMessage());
        throw new RestRequestFailedException(ex.getHttpStatus(), ex.getErrorDetails());
    }

    private String getAcmErrorMessage(final HttpStatusCodeException ex) {
        final String errorResponseBodyString = ex.getResponseBodyAsString();
        if (!errorResponseBodyString.isEmpty()) {
            final AcmErrorDetails acmErrorDetails = getAcmErrorDetails(errorResponseBodyString);
            return acmErrorDetails.getErrorDetails();
        } else {
            return ex.getMessage();
        }
    }

    private AcmErrorDetails getAcmErrorDetails(final String responseString) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(responseString, AcmErrorDetails.class);
        } catch (final JsonProcessingException e) {
            // default message
            log.error("Exception thrown while parsing response body from ACM-R: {}", e.getMessage(), e);
            AcmErrorDetails acmErrorDetails = new AcmErrorDetails();
            acmErrorDetails.setErrorDetails("Error occurred in ACM-R. Could not map error details message from response.");
            return acmErrorDetails;
        }
    }
}
