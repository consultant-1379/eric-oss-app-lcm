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

import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppStatus;



@SpringBootTest(classes = AppLcmApplication.class)
public class AcmExecuteInitializeActionPositiveBase extends AcmAppsBase {

    @Override
    public void validate() {
        final AppDetails initializingApp = DummyDataGenerator.createDummyApp();
        initializingApp.setStatus(AppStatus.INITIALIZING);
    }
}
