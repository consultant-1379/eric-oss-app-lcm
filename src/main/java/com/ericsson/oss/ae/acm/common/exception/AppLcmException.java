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

import org.springframework.http.HttpStatus;

import lombok.Getter;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;

/**
 * Generic exception to capture error details related to App LCM use cases.
 */
@Getter
public class AppLcmException extends RuntimeException {
    private final AppLcmError appLcmError;

    private final HttpStatus httpStatus;

    private final String[] arguments;

    /**
     * Constructor for a AppLcmException which takes an exception message, httpStatus and an AppLcmError.
     *
     * @param httpStatus
     *     The HTTP status.
     * @param appLcmError
     *     The App LCM error.
     */
    public AppLcmException(final HttpStatus httpStatus, final AppLcmError appLcmError) {
        this.appLcmError = appLcmError;
        this.httpStatus = httpStatus;
        this.arguments = null;
    }

    /**
     * Constructor for a AppLcmException which takes an exception message, httpStatus, AppLcmError and arguments.
     *
     * @param httpStatus
     *     The HTTP status.
     * @param appLcmError
     *     The App LCM error.
     * @param arguments
     *     To handle error message arguments.
     */
    public AppLcmException(final HttpStatus httpStatus, final AppLcmError appLcmError, final String[] arguments) {
        this.appLcmError = appLcmError;
        this.httpStatus = httpStatus;
        this.arguments = arguments.clone();
    }

    /**
     * This method is used for generating error messages with arguments.
     */
    public String generateErrorMessage() {
        if (this.getArguments() != null) {
            return String.format(this.appLcmError.getErrorMessage(), this.getArguments());
        } else {
            return this.appLcmError.getErrorMessage();
        }
    }
}
