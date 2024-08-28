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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.presentation.filter.GetAppsFilter;

@SpringBootTest(classes = AppLcmApplication.class)
public class AcmGetAppsNegativeBase extends AcmAppsBase {
    @Override
    public void validate() {

        GetAppsFilter notFoundVersionFilter = new GetAppsFilter.GetAppsFilterBuilder().version("9999").build();
        GetAppsFilter internalErrorVersionFilter = new GetAppsFilter.GetAppsFilterBuilder().version("1.0.1").build();
        GetAppsFilter badRequestVersionFilter = new GetAppsFilter.GetAppsFilterBuilder().version("8888").build();

        // 404 Not Found
        given(service.getApps(eq(notFoundVersionFilter))).willThrow(
        new AppLcmException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_ERROR));

        // 500 Internal Server Error
        given(service.getApps(eq(internalErrorVersionFilter))).willThrow(
            new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_APP_LCM_ERROR));

        // 400 Bad Request
        given(service.getApps(eq(badRequestVersionFilter))).willThrow(
            new AppLcmException(HttpStatus.BAD_REQUEST, BAD_REQUEST_ERROR));
    }
}
