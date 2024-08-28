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

public class AppLcmServiceException extends AppLcmException {

    public AppLcmServiceException(final AppLcmError appLcmError, final String message, final String url, final Throwable e) {
        super(appLcmError,
              message, url);
    }
}
