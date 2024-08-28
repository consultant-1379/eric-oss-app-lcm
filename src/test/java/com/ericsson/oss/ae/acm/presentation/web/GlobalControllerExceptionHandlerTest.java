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

package com.ericsson.oss.ae.acm.presentation.web;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.AC_INSTANCE_PROPERTIES_GENERATION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.BAD_REQUEST_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.DEFAULT_APP_LCM_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.WebRequest;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.ProblemDetails;

/**
 * Test case for the AppLcmExceptionHandler.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, GlobalControllerExceptionHandler.class})
public class GlobalControllerExceptionHandlerTest {
    private final GlobalControllerExceptionHandler globalControllerExceptionHandler = new GlobalControllerExceptionHandler();
    private static final String TEST_COLON = "test:";

    @Test
    public void testAppLcmExceptionForToscaServiceTemplateGenerationError() {
        // Given
        final WebRequest webRequest = mock(WebRequest.class);
        final AppLcmException appLcmException = Assertions.assertThrows(AppLcmException.class, () -> {
            throw new AppLcmException(INTERNAL_SERVER_ERROR, TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR
                    );
                }
        );

        // When
        final ResponseEntity<Object> responseEntity = this.globalControllerExceptionHandler.handleAppLcmException(appLcmException, webRequest);

        // Then
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        final ProblemDetails problemDetails = (ProblemDetails) responseEntity.getBody();

        assert problemDetails != null;
        assertEquals(TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorTitle(), problemDetails.getTitle());
        assertEquals(TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorMessage(), problemDetails.getDetail());
        assertEquals(INTERNAL_SERVER_ERROR.value(), problemDetails.getStatus());
    }

    @Test
    public void testAppLcmExceptionForAcInstancePropertiesGenerationError() {
        // Given
        final WebRequest webRequest = mock(WebRequest.class);
        final AppLcmException appLcmException = Assertions.assertThrows(
                AppLcmException.class, () -> {
                    throw new AppLcmException(
                            INTERNAL_SERVER_ERROR,
                            AC_INSTANCE_PROPERTIES_GENERATION_ERROR
                    );
                }
        );

        // When
        final ResponseEntity<Object> responseEntity = this.globalControllerExceptionHandler.handleAppLcmException(appLcmException, webRequest);

        // Then
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        final ProblemDetails problemDetails = (ProblemDetails) responseEntity.getBody();

        assert problemDetails != null;
        assertEquals(AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorTitle(), problemDetails.getTitle());
        assertEquals(AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorMessage(), problemDetails.getDetail());
        assertEquals(INTERNAL_SERVER_ERROR.value(), problemDetails.getStatus());
    }

    @Test
    public void testAppLcmExceptionForAppNotFoundError() {
        // Given
        final WebRequest webRequest = mock(WebRequest.class);
        final AppLcmException appLcmException = Assertions.assertThrows(
            AppLcmException.class, () -> {
                throw new AppLcmException(
                    NOT_FOUND,
                    APP_NOT_FOUND_ERROR
                );
            }
        );

        // When
        final ResponseEntity<Object> responseEntity = this.globalControllerExceptionHandler.handleAppLcmException(appLcmException, webRequest);

        // Then
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());

        final ProblemDetails problemDetails = (ProblemDetails) responseEntity.getBody();

        assert problemDetails != null;
        assertEquals(APP_NOT_FOUND_ERROR.getErrorTitle(), problemDetails.getTitle());
        assertEquals(APP_NOT_FOUND_ERROR.getErrorMessage(), problemDetails.getDetail());
        assertEquals(NOT_FOUND.value(), problemDetails.getStatus());
        System.out.print(TEST_COLON + responseEntity);
        System.out.print(TEST_COLON + problemDetails);
    }

    @Test
    public void testAppLcmExceptionForBadRequestError() {
        // Given
        final WebRequest webRequest = mock(WebRequest.class);
        final AppLcmException appLcmException = Assertions.assertThrows(
            AppLcmException.class, () -> {
                throw new AppLcmException(
                    BAD_REQUEST,
                    BAD_REQUEST_ERROR
                );
            }
        );

        // When
        final ResponseEntity<Object> responseEntity = this.globalControllerExceptionHandler.handleAppLcmException(appLcmException, webRequest);

        // Then
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());

        final ProblemDetails problemDetails = (ProblemDetails) responseEntity.getBody();

        assert problemDetails != null;
        assertEquals(BAD_REQUEST_ERROR.getErrorTitle(), problemDetails.getTitle());
        assertEquals(BAD_REQUEST_ERROR.getErrorMessage(), problemDetails.getDetail());
        assertEquals(BAD_REQUEST.value(), problemDetails.getStatus());
        System.out.print(TEST_COLON + responseEntity);
    }

    @Test
    public void testAppLcmExceptionForInternalServerError() {
        // Given
        final WebRequest webRequest = mock(WebRequest.class);
        final AppLcmException appLcmException = Assertions.assertThrows(
            AppLcmException.class, () -> {
                throw new AppLcmException(
                    INTERNAL_SERVER_ERROR,
                    DEFAULT_APP_LCM_ERROR
                );
            }
        );

        // When
        final ResponseEntity<Object> responseEntity = this.globalControllerExceptionHandler.handleAppLcmException(appLcmException, webRequest);

        // Then
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        final ProblemDetails problemDetails = (ProblemDetails) responseEntity.getBody();

        assert problemDetails != null;
        assertEquals(DEFAULT_APP_LCM_ERROR.getErrorTitle(), problemDetails.getTitle());
        assertEquals(DEFAULT_APP_LCM_ERROR.getErrorMessage(), problemDetails.getDetail());
        assertEquals(INTERNAL_SERVER_ERROR.value(), problemDetails.getStatus());
        System.out.print(TEST_COLON +responseEntity);
    }
}
