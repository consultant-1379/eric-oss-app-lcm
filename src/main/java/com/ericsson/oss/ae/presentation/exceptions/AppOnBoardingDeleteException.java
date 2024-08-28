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
 * The type App onBoarding delete exception.
 */
public class AppOnBoardingDeleteException extends AppLcmException{

    /**
     * Instantiates a new App onBoarding delete exception.
     *
     * @param appLcmError the app lcm error
     * @param message     the message
     */
    public AppOnBoardingDeleteException(AppLcmError appLcmError, String message) {
        super(appLcmError, message);
    }

    public AppOnBoardingDeleteException(AppLcmError appLcmError, String message, String url) {
        super(appLcmError, message, url);
    }
}
