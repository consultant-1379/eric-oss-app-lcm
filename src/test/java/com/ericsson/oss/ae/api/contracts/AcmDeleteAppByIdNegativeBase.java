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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.BAD_REQUEST_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEFAULT_APP_LCM_ERROR;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

@SpringBootTest(classes = AppLcmApplication.class)
public class AcmDeleteAppByIdNegativeBase extends AcmAppsBase {
    @Override
    public void validate() {
        // 404 Not Found
        doThrow(new AppLcmException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_ERROR)).when(service)
            .deleteAppById("99999a99-9de9-9ad9-9999-999eefd99999");

        // 500 Internal Server Error
        doThrow(new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_APP_LCM_ERROR)).when(service)
            .deleteAppById("26471a81-1de4-4ad9-9724-326eefd22230");

        // 400 Bad Request
        doThrow(new AppLcmException(HttpStatus.BAD_REQUEST, BAD_REQUEST_ERROR)).when(service)
            .deleteAppById("26471181-1d14-4119-9724-326111122230");
    }
}