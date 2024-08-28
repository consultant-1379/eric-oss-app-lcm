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

package com.ericsson.oss.ae.participant.concretecommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.model.participant.RestRequest;
import com.ericsson.oss.ae.participant.RestService;
import com.ericsson.oss.ae.participant.command.ParticipantCommand;

/**
 * Concrete Command that implements ParticipantCommand for Helm Orchestrator.
 */
@Component
public class HelmParticipantCommand<T> implements ParticipantCommand {

    @Autowired
    RestService restService;

    @Autowired
    RestRequest restRequest;

    public HelmParticipantCommand(final RestService restService, final RestRequest restRequest) {
        this.restService = restService;
        this.restRequest = restRequest;
    }

    @Override
    public ResponseEntity<T> execute() {
        return restService.callRestEndpoint(restRequest);
    }
}
