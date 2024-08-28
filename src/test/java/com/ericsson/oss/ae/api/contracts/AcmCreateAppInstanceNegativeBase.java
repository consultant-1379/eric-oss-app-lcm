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
import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22231;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEFAULT_APP_LCM_ERROR;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;

@SpringBootTest(classes = AppLcmApplication.class)
public class AcmCreateAppInstanceNegativeBase extends AcmAppInstancesBase {

    @Override
    public void validate() {
        CreateAppInstanceRequest createAppInstanceRequestNotFound =
                new CreateAppInstanceRequest().appId(EEFD_22230);

        CreateAppInstanceRequest createAppInstanceRequestInternalError =
                new CreateAppInstanceRequest().appId(EEFD_22231);

        // 404 Not Found
        given(service.createAppInstance(eq(createAppInstanceRequestNotFound))).willThrow(
                new AppLcmException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_ERROR));

        // 500 Internal Server Error
        given(service.createAppInstance(eq(createAppInstanceRequestInternalError))).willThrow(
                new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_APP_LCM_ERROR));
    }
}
