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

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import com.ericsson.oss.ae.acm.common.exception.RestRequestException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestRetryException;

public interface RestClientService<T> {

    /**
     * Call rest end point
     *
     * @param restRequest - request body for sending rest request
     * @return ResponseEntity
     */

    @Retryable(retryFor = { RestRequestRetryException.class},
            maxAttemptsExpression = "${lcmRetry.maxAttempts:3}",
            backoff = @Backoff(delayExpression = "${lcmRetry.delay:2000}"),
        noRetryFor = { RestRequestFailedException.class })
    ResponseEntity<T> callRestEndpoint(final RestRequest restRequest);

    /**
     * Recovery method if all the spring retry fails
     *
     * @param ex - RestClientException details
     * @param restRequest - request body for sending rest request
     * @return ResponseEntity
     */
    @Recover
    ResponseEntity<T> generateErrorResponse(final RestRequestException ex, final RestRequest restRequest);

}
