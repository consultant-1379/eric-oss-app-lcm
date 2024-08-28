/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.ae.presentation.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.api.model.MultiDeleteErrorMessage;
import com.ericsson.oss.ae.api.model.ProblemDetails;

/**
 * Exception handler used for handling exceptions that may occur in App Lcm endpoints.
 */
@Slf4j
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_MESSAGE = "{} Occurred, {}";
    private static final String INTERNAL_SERVER_ERROR_TITLE = "Internal Server Error";
    private static final String NOT_FOUND_TITLE = "Not Found";
    private static final String BAD_REQUEST_TITLE = "Bad Request";
    private static final String CONFLICT = "Conflict";
    private static final String SERVICE_UNAVAILABLE = "Service Unavailable";

    /**
     * Exception handler used to handle ResourceNotFoundException.
     *
     * @param exception
     *            ResourceNotFoundException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<ProblemDetails> handleResourceNotFoundException(final ResourceNotFoundException exception) {
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(NOT_FOUND_TITLE).type(NOT_FOUND.getReasonPhrase()).status(NOT_FOUND.value())
                .detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, NOT_FOUND);
    }

    /**
     * Exception handler used to handle AppOnboardingAppNotExistException.
     *
     * @param exception
     *            AppOnboardingAppNotExistException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(AppOnboardingAppNotExistException.class)
    public final ResponseEntity<ProblemDetails> handleAppOnboardingAppNotExistException(final AppOnboardingAppNotExistException exception) {
        log.info("App Onboarding Exception: {}", exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(BAD_REQUEST_TITLE).type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value()).detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    /**
     * Response handler used to handle handleAppOnBoardingAppDisabled.
     *
     * @param exception
     *            AppOnBoardingDisabled.
     * @return ResponseEntity with response for the problem details
     */
    @ExceptionHandler(AppOnBoardingModeException.class)
    public final ResponseEntity<ProblemDetails> handleAppOnBoardingAppDisabled(final AppOnBoardingModeException exception) {
        log.info("App OnBoarding Exception: {}", exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(CONFLICT).type(HttpStatus.CONFLICT.getReasonPhrase()).status(HttpStatus.CONFLICT.value())
                .detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, HttpStatus.CONFLICT);
    }

    /**
     * Exception handler used to handle AppOnboardingArtifactRetrievalException.
     *
     * @param exception
     *            AppOnboardingArtifactRetrievalException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(AppOnboardingArtifactRetrievalException.class)
    public final ResponseEntity<ProblemDetails> handleAppOnboardingArtifactRetrievalException(final AppOnboardingArtifactRetrievalException exception) {
        log.info("App onboarding exception: {}", exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(NOT_FOUND_TITLE).type(NOT_FOUND.getReasonPhrase())
            .status(INTERNAL_SERVER_ERROR.value())
            .detail(exception.getMessage())
            .appLcmErrorCode(exception.getAppLcmError().getErrorCode())
            .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage())
            .url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, NOT_FOUND);
    }

    /**
     * Exception handler used to handle FailureToInstantiateException.
     *
     * @param exception
     *            FailureToInstantiateException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(FailureToInstantiateException.class)
    public final ResponseEntity<ProblemDetails> handleFailureToInstantiateException(final FailureToInstantiateException exception) {
        log.info("Helm orchestrator exception: {}", exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(INTERNAL_SERVER_ERROR_TITLE).type(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(INTERNAL_SERVER_ERROR.value()).detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler used to handle UnableToRetrieveArtifactException.
     *
     * @param exception
     *            UnableToRetrieveArtifactException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(UnableToRetrieveArtifactException.class)
    public final ResponseEntity<ProblemDetails> handleUnableToRetrieveArtifactException(final UnableToRetrieveArtifactException exception) {
        log.info("App Lcm exception: {}", exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(NOT_FOUND_TITLE).type(NOT_FOUND.getReasonPhrase()).status(NOT_FOUND.value())
                .detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, NOT_FOUND);
    }

    /**
     * Exception handler used to handle FailureToTerminateException.
     *
     * @param exception
     *            FailureToTerminateException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(FailureToTerminateException.class)
    public final ResponseEntity<ProblemDetails> handleFailureToTerminateException(final FailureToTerminateException exception) {
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(BAD_REQUEST_TITLE).type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value()).detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(FailureToUpdateException.class)
    public final ResponseEntity<ProblemDetails> handleFailureToUpdateException(final FailureToUpdateException exception) {
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(BAD_REQUEST_TITLE).type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value()).detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(HelmOrchestratorException.class)
    public final ResponseEntity<ProblemDetails> handleHelmOrchestratorException(final HelmOrchestratorException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title("Helm Orchestrator Client Exception").type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value()).appLcmErrorCode(ex.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(ex.getAppLcmError().getErrorMessage()).detail(ex.getMessage()).url(ex.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    @ExceptionHandler(KeycloakException.class)
    public final ResponseEntity<ProblemDetails> handleKeycloakException(final KeycloakException ex) {
        log.error(ERROR_MESSAGE, ex, ex.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title("Keycloak  Client Exception").type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value()).appLcmErrorCode(ex.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(ex.getAppLcmError().getErrorMessage()).detail(ex.getMessage()).url(ex.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    /**
     * Exception handler used to handle DataAccessException.
     *
     * @param exception
     *            AppLcmDataAccessException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(AppLcmDataAccessException.class)
    public final ResponseEntity<ProblemDetails> handleDataAccessException(final AppLcmDataAccessException exception) {
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(NOT_FOUND_TITLE).type(NOT_FOUND.getReasonPhrase())
                .status(BAD_REQUEST.value()).detail(exception.getMessage()).appLcmErrorCode(exception.getAppLcmError().getErrorCode())
                .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage()).url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, NOT_FOUND);
    }

    /**
     * Exception handler used to handle InvalidInputException.
     *
     * @param exception
     *            InvalidInputException.
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(InvalidInputException.class)
    public final ResponseEntity<ProblemDetails> handleInvalidInputException(final InvalidInputException exception) {
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails().title(BAD_REQUEST_TITLE)
            .type(BAD_REQUEST.getReasonPhrase())
            .status(BAD_REQUEST.value())
            .detail(exception.getMessage())
            .appLcmErrorCode(exception.getAppLcmError().getErrorCode())
            .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage())
            .url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, BAD_REQUEST);
    }

    /**
     * Exception handler used to handle FailureToDeleteException.
     *
     * @param exception
     *          FailureToDeleteException
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(FailureToDeleteException.class)
    public final ResponseEntity<MultiDeleteErrorMessage>handleDeleteException(final FailureToDeleteException exception){
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final MultiDeleteErrorMessage errorMessage = new MultiDeleteErrorMessage()
            .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage())
            .appLcmErrorCode(exception.getAppLcmError().getErrorCode())
            .totalSuccessful(exception.getTotalSuccessfulDeletion())
            .errorData(exception.getErrorData());
        return new ResponseEntity<>(errorMessage, OK);
    }

    /**
     * Exception handler used to handle AppLcmServerException.
     *
     * @param exception
     *          AppLcmServerException
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(AppLcmServiceException.class)
    public final ResponseEntity<ProblemDetails>handleServerException(final AppLcmServiceException exception){
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails()
            .title(SERVICE_UNAVAILABLE)
            .type(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .detail(exception.getMessage())
            .appLcmErrorCode(exception.getAppLcmError().getErrorCode())
            .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage())
            .url(exception.getUrl());
        return new ResponseEntity<>(problemDetails,
                                    HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Exception handler used to handle AppLcmServerException.
     *
     * @param exception
     *          AppLcmServerException
     * @return ResponseEntity with response for the error
     */
    @ExceptionHandler(AppOnBoardingDeleteException.class)
    public final ResponseEntity<ProblemDetails>handleAppOnBoardingDeleteException(final AppOnBoardingDeleteException exception){
        log.error(ERROR_MESSAGE, exception, exception.getMessage());
        final ProblemDetails problemDetails = new ProblemDetails()
            .title(INTERNAL_SERVER_ERROR_TITLE)
            .type(INTERNAL_SERVER_ERROR.getReasonPhrase())
            .status(INTERNAL_SERVER_ERROR.value())
            .detail(exception.getMessage())
            .appLcmErrorCode(exception.getAppLcmError().getErrorCode())
            .appLcmErrorMessage(exception.getAppLcmError().getErrorMessage())
            .url(exception.getUrl());
        return new ResponseEntity<>(problemDetails, INTERNAL_SERVER_ERROR);
    }
}
