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

package com.ericsson.oss.ae.acm.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestRequestRetryException extends RestRequestException {

    public RestRequestRetryException(HttpStatus status, String errorDetails) {
        super(status, errorDetails);
    }
}
