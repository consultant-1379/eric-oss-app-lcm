/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.presentation.exceptions;

/**
 * Exception class for Invalid Input.
 */
public class InvalidInputException extends AppLcmException {

    public InvalidInputException(final AppLcmError invalidInputException, final String message) {
        super(invalidInputException, message);
    }

    public InvalidInputException(final AppLcmError invalidInputException, final String message, final Throwable throwable) {
        super(invalidInputException, message, throwable);
    }
}
