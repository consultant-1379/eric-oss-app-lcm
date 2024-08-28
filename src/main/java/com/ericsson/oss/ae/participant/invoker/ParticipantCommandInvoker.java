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

package com.ericsson.oss.ae.participant.invoker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.participant.command.ParticipantCommand;

/**
 * Invoker class asks Participant Command to carry out action.
 */
@Component
public class ParticipantCommandInvoker<T> {

    @Autowired
    ParticipantCommand participantCommand;

    public ParticipantCommandInvoker(final ParticipantCommand participantCommand) {
        this.participantCommand = participantCommand;
    }

    public ResponseEntity<T> executeHelmCommand() {
        return participantCommand.execute();
    }

}
