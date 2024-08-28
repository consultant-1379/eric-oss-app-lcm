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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.participant.command.ParticipantCommand;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, ParticipantCommandInvoker.class })
public class ParticipantCommandInvokerTest {

    @Autowired
    ParticipantCommandInvoker participantCommandInvoker;

    @Mock
    ParticipantCommand participantCommand;

    @Test
    public void givenValidResponseFromRestEndpoint_WhenParticipantCommandIsSet_ThenReturnHttpStatus200() {
        // given:
        Mockito.when(participantCommand.execute()).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when:
        participantCommandInvoker = new ParticipantCommandInvoker(participantCommand);
        final ResponseEntity<Object> result = participantCommandInvoker.executeHelmCommand();

        // then:
        BDDAssertions.then(result.getStatusCodeValue()).isEqualTo(200);
    }
}
