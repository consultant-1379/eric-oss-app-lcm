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

package com.ericsson.oss.ae.acm.common.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.ericsson.oss.ae.acm.common.exception.RestRequestRetryException;
import lombok.extern.slf4j.Slf4j;

/**
 * Common util class for acm rest service and keycloak rest service.{@link RestClientRetryHandler}
 */
@Slf4j
public class RestClientRetryHandler {

    protected HttpStatus findStatusCodeFromException(final RestClientException ex) {
        if (ex instanceof HttpStatusCodeException) {
            final HttpStatusCodeException exception = (HttpStatusCodeException) ex;
            return HttpStatus.valueOf(exception.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    protected void checkForRetryRequest(final HttpStatusCodeException ex, final String errorMessage) {
        final List<HttpStatus> retryStatusCodes = List.of(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
        final HttpStatusCode status = ex.getStatusCode();
        log.info("Checking for retry");
        if (retryStatusCodes.contains(status)) {
            // Throw exception for spring retry.
            throw new RestRequestRetryException(HttpStatus.valueOf(ex.getStatusCode().value()), errorMessage);
        }
    }
}
