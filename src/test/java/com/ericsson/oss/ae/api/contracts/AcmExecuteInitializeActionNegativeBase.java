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
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEFAULT_APP_LCM_ERROR;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.InitializeActionRequest;

@SpringBootTest(classes = AppLcmApplication.class)
public class AcmExecuteInitializeActionNegativeBase extends AcmAppsBase {

    @Override
    public void validate() {
        // 404 Not Found
        doThrow(new AppLcmException(NOT_FOUND, APP_NOT_FOUND_ERROR))
            .when(service).executeInitializeAction("64e2e1bb-5e99-45fa-9d1a-67a7df7d0e43", new InitializeActionRequest().action("INITIALIZE"));

        // 404 Not Found
        doThrow(new AppLcmException(NOT_FOUND, APP_NOT_FOUND_ERROR))
                .when(service).executeInitializeAction("64e2e1bb-5e99-45fa-9d1a-67a7df7d0e43", new InitializeActionRequest().action("DEINITIALIZE"));

        // 500 Internal Server Error
        doThrow(new AppLcmException(INTERNAL_SERVER_ERROR, AppLcmError.INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR))
            .when(service).executeInitializeAction("64e2e1bb-5e99-45fa-9d1a-67a7df7d0e42", new InitializeActionRequest().action("INITIALIZE"));

        // 500 Internal Server Error
        doThrow(new AppLcmException(INTERNAL_SERVER_ERROR, AppLcmError.DEINITIALIZE_APP_DEPRIME_COMPOSITION_TYPE_ERROR))
                .when(service).executeInitializeAction("64e2e1bb-5e99-45fa-9d1a-67a7df7d0e42", new InitializeActionRequest().action("DEINITIALIZE"));

        // 400 Bad Request
        doThrow(new AppLcmException(BAD_REQUEST, AppLcmError.APP_ACTION_INVALID_TYPE_ERROR))
            .when(service).executeInitializeAction("64e2e1bb-5e99-45fa-9d1a-67a7df7d0e41", new InitializeActionRequest().action("INVALID-ACTION"));
    }
}
