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
 * Exception class for App Lcm.
 */
public class AppLcmException extends RuntimeException {
    private final String url;
    private final AppLcmError appLcmError;

    /**
     * Constructor used to create exception with internal error code, error message, url and throwable.
     *
     * @param appLcmError
     *            The App LCM Error with internationalized code and error message.
     * @param message
     *            Specific message for error.
     * @param url
     *            URL for error.
     * @param throwable
     *            The Exception to be thrown.
     */
    public AppLcmException(final AppLcmError appLcmError, final String message, final String url, final Throwable throwable) {
        super(message, throwable);
        this.url = url;
        this.appLcmError = appLcmError;
    }

    /**
     * Constructor used to create exception with internal error code, error message and url.
     *
     * @param appLcmError
     *            The App LCM Error with internationalized code and error message.
     * @param message
     *            Specific message for error.
     * @param url
     *            URL for error.
     */
    public AppLcmException(final AppLcmError appLcmError, final String message, final String url) {
        super(message);
        this.url = url;
        this.appLcmError = appLcmError;
    }

    /**
     * Constructor used to create exception with internal error code, error message and url.
     *
     * @param appLcmError
     *            The App LCM Error with internationalized code and error message.
     * @param message
     *            Specific message for error.
     * @param throwable
     *            The Exception to be thrown.
     */
    public AppLcmException(final AppLcmError appLcmError, final String message, final Throwable throwable) {
        super(message, throwable);
        url = null;
        this.appLcmError = appLcmError;
    }

    /**
     * Constructor used to create exception with internal error code and error message.
     *
     * @param appLcmError
     *            The App LCM Error with internationalized code and error message.
     * @param message
     *            Specific message for error.
     */
    public AppLcmException(final AppLcmError appLcmError, final String message) {
        super(message);
        url = null;
        this.appLcmError = appLcmError;
    }

    public String getUrl() {
        return url;
    }

    public AppLcmError getAppLcmError() {
        return appLcmError;
    }
}
