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
import com.ericsson.oss.ae.model.participant.RestRequest;
import com.ericsson.oss.ae.participant.RestService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, HelmParticipantCommand.class })
public class HelmParticipantCommandTest {

    @Autowired
    HelmParticipantCommand helmParticipantCommand;

    @Mock
    RestService restService;

    @Mock
    RestRequest restRequest;

    @Test
    public void givenValidResponseFromRestEndpoint_WhenRestRequestIsSet_ThenReturnHttpStatus200() {
        // given:
        Mockito.when(restService.callRestEndpoint(restRequest)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when:
        helmParticipantCommand = new HelmParticipantCommand(restService, restRequest);
        final ResponseEntity<Object> result = helmParticipantCommand.execute();

        // then:
        BDDAssertions.then(result.getStatusCodeValue()).isEqualTo(200);
    }
}
