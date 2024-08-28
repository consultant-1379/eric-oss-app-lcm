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

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.TestUtils;


@ExtendWith(MockitoExtension.class)
public class AcmGetAppInstancePositiveBase extends AcmAppInstancesBase {
    @Override
    public void validate() {
        given(service.getAppInstanceById("26471a81-1de4-4ad9-9724-326eefd22230")).willReturn(
            TestUtils.getDummyAppInstance());
    }
}
