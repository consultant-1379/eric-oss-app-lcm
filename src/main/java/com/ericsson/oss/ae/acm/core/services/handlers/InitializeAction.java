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

package com.ericsson.oss.ae.acm.core.services.handlers;

import java.util.Locale;

import org.springframework.http.HttpStatus;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

/**
 * Supported action types that may be executed on an App resource
 */
public enum InitializeAction {
    INITIALIZE("initialize"), DEINITIALIZE("deinitialize");

    private final String action;

    InitializeAction(final String action){
        this.action = action;
    }

    public static InitializeAction getSpecificActionType(final String requestedAction) {
        final String requestSpecificAction = requestedAction.toLowerCase(Locale.ROOT);
        if (requestSpecificAction.equals(InitializeAction.INITIALIZE.action)){
            return InitializeAction.INITIALIZE;
        } else if (requestSpecificAction.equals(InitializeAction.DEINITIALIZE.action)){
            return InitializeAction.DEINITIALIZE;
        } else {
            throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.APP_ACTION_INVALID_TYPE_ERROR);
        }
    }
}
