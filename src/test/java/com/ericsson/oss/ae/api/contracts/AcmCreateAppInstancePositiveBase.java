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
package com.ericsson.oss.ae.api.contracts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator;

@ExtendWith(MockitoExtension.class)
public class AcmCreateAppInstancePositiveBase extends AcmAppInstancesBase {
    @Override
    public void validate() {
        given(service.createAppInstance(any())).willReturn(
                DummyDataGenerator.getDummyCreateAppInstanceResponse(UUID.fromString("7e151de6-18a9-4770-be4f-354b620f0035")));
    }
}