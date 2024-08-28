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

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.ProblemDetails;

/**
 * Exception handler class for App LCM exceptions.
 */
@ControllerAdvice
@Slf4j
@Order(HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler {
    /**
     * Handles an AppLcmException.
     *
     * @param ex      The exception.
     * @param request The web request.
     * @return A response entity with ProblemDetails.
     */
    @ExceptionHandler(AppLcmException.class)
    public ResponseEntity<Object> handleAppLcmException(final AppLcmException ex, WebRequest request) {
        final AppLcmError appLcmError = ex.getAppLcmError();
        final ProblemDetails problemDetails = generateErrorResponse(ex.generateErrorMessage(),
                appLcmError.getErrorTitle(), ex.getHttpStatus().value());
        log.info("Sending response {} : {}", ex.getHttpStatus(), problemDetails.getTitle());
        return new ResponseEntity<>(problemDetails, ex.getHttpStatus());
    }

    /**
     * Generates a ProblemDetails response.
     *
     * @param errorMessage The error message.
     * @param title        The error title.
     * @param status       The HTTP status.
     * @return A ProblemDetails response.
     */
    private ProblemDetails generateErrorResponse(final String errorMessage, final String title,
                                                 final int status) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setTitle(title);
        problemDetails.setStatus(status);
        problemDetails.setDetail(errorMessage);
        return problemDetails;
    }
}