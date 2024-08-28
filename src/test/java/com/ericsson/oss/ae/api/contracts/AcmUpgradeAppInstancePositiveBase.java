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

import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getDummyAppInstanceUpgrade;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AcmUpgradeAppInstancePositiveBase extends AcmAppInstancesBase{
    @Override
    public void validate() {
        given(service.manageAppInstance(any(String.class), any()))
                .willReturn(getDummyAppInstanceUpgrade());
    }
}
