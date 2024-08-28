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
import com.ericsson.oss.ae.acm.presentation.filter.GetAppsFilter;
import com.ericsson.oss.ae.v3.api.model.AppItems;

@ExtendWith(MockitoExtension.class)
public class AcmGetAppsPositiveBase extends AcmAppsBase {
    @Override
    public void validate() {
        GetAppsFilter versionFilter = new GetAppsFilter.GetAppsFilterBuilder().version("1.0.1").build();
        AppItems appItems = new AppItems().items(TestUtils.getAppDetails());

        given(service.getApps(versionFilter)).willReturn(appItems);
    }
}
