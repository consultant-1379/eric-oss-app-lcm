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

package com.ericsson.oss.ae.participant.command;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Interface for executing an action.
 */
@Component
public interface ParticipantCommand<T> {
    ResponseEntity<T> execute();
}
