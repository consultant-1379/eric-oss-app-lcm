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

import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22233;
import static com.ericsson.oss.ae.acm.TestUtils.getAppInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

@ExtendWith(MockitoExtension.class)
public class AcmUpdateAppInstancePositiveBase extends AcmAppInstancesBase {
    @Override
    public void validate() {
        AppInstance appInstance = getAppInstance();
        doReturn(appInstance).when(service).getAppInstanceById(any(String.class));
        given(service.updateAppInstance(eq(EEFD_22233), any(UpdateAppInstanceRequest.class)))
            .willReturn(DummyDataGenerator.getDummyAppInstanceUpdateOperationResponse());
    }
}
