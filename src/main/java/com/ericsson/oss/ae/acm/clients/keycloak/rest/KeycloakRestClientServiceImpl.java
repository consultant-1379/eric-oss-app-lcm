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

package com.ericsson.oss.ae.acm.clients.keycloak.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.ae.acm.common.rest.RestRequest;
import com.ericsson.oss.ae.acm.common.exception.RestRequestException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestRetryException;
import com.ericsson.oss.ae.acm.common.rest.RestClientRetryHandler;
import com.ericsson.oss.ae.acm.common.rest.RestClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Qualifier("keycloakRestClientService")
public class KeycloakRestClientServiceImpl<T> extends RestClientRetryHandler implements RestClientService {

    @Autowired
    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity<T> callRestEndpoint(final RestRequest requestBody) {
        log.info("Call rest endpoint URL: {}", requestBody.getUrl());
        try {
            return restTemplate.exchange(requestBody.getUrl(), requestBody.getRequestMethod(), requestBody.getRequest(),
                    requestBody.getResponseType());
        } catch (final HttpClientErrorException ex) {
            final HttpStatus httpStatus = findStatusCodeFromException(ex);
            log.error("Keycloak Exception when calling the endpoint: {}, response status: {}, error detail: {}", requestBody.getUrl(), httpStatus, ex.getMessage(), ex);
            throw new RestRequestFailedException(httpStatus, ex.getMessage());
        } catch (final HttpServerErrorException ex) {
            final HttpStatus httpStatus = findStatusCodeFromException(ex);
            checkForRetryRequest(ex, ex.getMessage());
            log.error("Keycloak Exception when calling the endpoint: {}, response status: {}, error detail: {}", requestBody.getUrl(), httpStatus, ex.getMessage(), ex);
            throw new RestRequestFailedException(httpStatus, ex.getMessage());
        } catch (final ResourceAccessException ex) {
            log.error("ResourceAccessException when calling the endpoint: {} error detail: {}", requestBody.getUrl(), ex.getMessage(), ex);
            throw new RestRequestRetryException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        }
    }

    @Override
    public ResponseEntity<T> generateErrorResponse(final RestRequestException ex, final RestRequest requestBody) {
        log.info("Retry attempts for rest request have failed. Detail: {}", ex.getMessage());
        throw new RestRequestFailedException(ex.getHttpStatus(), ex.getErrorDetails());
    }

}
