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

import java.util.List;

/**
 * The type Failure to delete exception.
 */
@Getter
public class FailureToDeleteException extends AppLcmMultiFailureException{

    /**
     * Instantiates a new Failure to delete exception.
     *
     * @param appLcmError             the app lcm error
     * @param totalSuccessfulDeletion the total successful deletion
     * @param errorData               the error data
     */
    public FailureToDeleteException(AppLcmError appLcmError, Long totalSuccessfulDeletion,
                                    final List<MultiDeleteFailureDetails> errorData) {
        super(appLcmError, totalSuccessfulDeletion, errorData);
    }
}
