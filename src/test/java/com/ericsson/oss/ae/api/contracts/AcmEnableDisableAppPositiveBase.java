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

import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22230;
import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getAppOperationResponse;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.EnableDisableAppRequest;

@ExtendWith(MockitoExtension.class)
public class AcmEnableDisableAppPositiveBase extends AcmAppsBase {
    @Override
    public void validate() {
        final UUID uuid = UUID.fromString(EEFD_22230);
        given(service.enableDisableApp(EEFD_22230, new EnableDisableAppRequest().mode(AppMode.DISABLED)))
            .willReturn(getAppOperationResponse(uuid, AppMode.DISABLED));
    }
}
