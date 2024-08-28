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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceItems;

@ExtendWith(MockitoExtension.class)
public class AcmGetAppInstancesPositiveBase extends AcmAppInstancesBase {
    @Override
    public void validate() {
        List<AppInstance> appInstances = new ArrayList<>();
        appInstances.add(
            TestUtils.getDummyAppInstance()
        );
        AppInstanceItems appInstanceItems = new AppInstanceItems();
        appInstanceItems.setItems(appInstances);
        given(service.getAppInstances(any(String.class))).willReturn(appInstanceItems);

    }
}