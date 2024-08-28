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

import com.ericsson.oss.ae.api.model.MultiDeleteFailureDetails;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * The type App lcm multi failure exception.
 */
@Getter
public class AppLcmMultiFailureException extends RuntimeException {

    private final AppLcmError appLcmError;
    private final Long totalSuccessfulDeletion;
    private final transient List<MultiDeleteFailureDetails> errorData;

    /**
     * @param appLcmError
     * @param totalSuccessfulDeletion
     * @param errorData
     */
    public AppLcmMultiFailureException(AppLcmError appLcmError, Long totalSuccessfulDeletion,
                                       List<MultiDeleteFailureDetails> errorData) {
        this.appLcmError = appLcmError;
        this.totalSuccessfulDeletion = totalSuccessfulDeletion;
        this.errorData = Collections.unmodifiableList(errorData);
    }
}
