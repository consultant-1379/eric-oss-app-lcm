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

package com.ericsson.oss.ae.presentation.exceptions;

/**
 * Exception for failing to instantiate an app.
 */
public class FailureToInstantiateException extends AppLcmException {

    public FailureToInstantiateException(final AppLcmError code, final String message, final String url, final Throwable e) {
        super(code, message, url, e);
    }

    public FailureToInstantiateException(final AppLcmError code, final String message, final String url) {
        super(code, message, url);
    }
}