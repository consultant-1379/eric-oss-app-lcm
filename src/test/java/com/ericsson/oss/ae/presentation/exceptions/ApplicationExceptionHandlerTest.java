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

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.api.model.MultiDeleteErrorMessage;
import com.ericsson.oss.ae.api.model.MultiDeleteFailureDetails;
import com.ericsson.oss.ae.api.model.ProblemDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_URL;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_METHOD_INSTANTIATION;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, ApplicationExceptionHandler.class })
public class ApplicationExceptionHandlerTest {
    public static final String ERROR = "This is a test";
    public static final String TEST_URL = "url/test-endpoint";

    private final ApplicationExceptionHandler applicationExceptionHandler = new ApplicationExceptionHandler();

    @Test
    public void givenAppOnBoardingDisabled_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        final ResponseEntity<ProblemDetails> result;
        try {
            throw new AppOnBoardingModeException(APP_ON_BOARDING_DISABLED, 1L, APP_ONBOARDING_METHOD_INSTANTIATION, APP_INSTANCES_URL);
        } catch (final AppOnBoardingModeException e) {
            result = applicationExceptionHandler.handleAppOnBoardingAppDisabled(e);
        }
        assertThat(result.getStatusCode()).isEqualTo(CONFLICT);
        assertThat(result.getBody().getType()).isEqualTo(CONFLICT.getReasonPhrase());
        assertThat(result.getBody().getStatus()).isEqualTo(CONFLICT.value());
        assertThat(result.getBody().getDetail()).isEqualTo("Requested app 1 is not enabled. Please contact app admin to enable the app for app " +
                                                               APP_ONBOARDING_METHOD_INSTANTIATION);
        assertThat(result.getBody().getAppLcmErrorCode()).isEqualTo(APP_ON_BOARDING_DISABLED.getErrorCode());
        assertThat(result.getBody().getAppLcmErrorMessage()).isEqualTo(String.valueOf(APP_ON_BOARDING_DISABLED.getErrorMessage()));
        assertThat(result.getBody().getUrl()).isEqualTo(APP_INSTANCES_URL);
    }

    @Test
    public void givenAResourceNotFoundException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new ResourceNotFoundException(SPECIFIED_APP_INSTANCE_NOT_FOUND, ERROR, TEST_URL, new Throwable());
        } catch (final ResourceNotFoundException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleResourceNotFoundException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1001,
                            SPECIFIED_APP_INSTANCE_NOT_FOUND, TEST_URL, NOT_FOUND);
        }
    }

    @Test
    public void givenAnAppOnBoardingAppNotExistException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, ERROR, TEST_URL);
        } catch (final AppOnboardingAppNotExistException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleAppOnboardingAppNotExistException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1002, APP_ONBOARDING_APP_NOT_FOUND, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    public void givenAnAppOnboardingArtifactRetrievalException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppOnboardingArtifactRetrievalException(APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND, ERROR, TEST_URL);
        } catch (final AppOnboardingArtifactRetrievalException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleAppOnboardingArtifactRetrievalException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1003, APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND, TEST_URL,
                    NOT_FOUND);
        }
    }

    @Test
    public void givenAFailureToInstantiateException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new FailureToInstantiateException(HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, ERROR, TEST_URL);
        } catch (final FailureToInstantiateException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleFailureToInstantiateException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1004, HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, TEST_URL,
                    INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void givenAnUnableToRetrieveArtifactException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new UnableToRetrieveArtifactException(ARTIFACT_INSTANCE_NOT_FOUND, ERROR, TEST_URL);
        } catch (final UnableToRetrieveArtifactException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleUnableToRetrieveArtifactException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1005, ARTIFACT_INSTANCE_NOT_FOUND, TEST_URL, NOT_FOUND);
        }
    }

    @Test
    public void givenAnAppOnboardingAppNotExistException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppOnboardingAppNotExistException(APP_ONBOARDING_APP_NOT_FOUND, ERROR, TEST_URL, new Throwable());
        } catch (final AppOnboardingAppNotExistException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleAppOnboardingAppNotExistException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1002, APP_ONBOARDING_APP_NOT_FOUND, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    public void givenAnAppOnboardingArtifactRetrievalExceptionWithThrowableTest_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppOnboardingArtifactRetrievalException(APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND, ERROR, TEST_URL, new Throwable());
        } catch (final AppOnboardingArtifactRetrievalException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleAppOnboardingArtifactRetrievalException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1003, APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND, TEST_URL,
                    NOT_FOUND);
        }
    }

    @Test
    public void givenFailureToInstantiateException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new FailureToInstantiateException(HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, ERROR, TEST_URL, new Throwable());
        } catch (final FailureToInstantiateException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleFailureToInstantiateException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1004, HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, TEST_URL,
                    INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void givenFailureToTerminateException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new FailureToTerminateException(AppLcmError.FAILURE_TO_TERMINATE_APP, ERROR, TEST_URL);
        } catch (final FailureToTerminateException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleFailureToTerminateException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1014, FAILURE_TO_TERMINATE_APP, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    public void givenUnableToRetrieveArtifactException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new UnableToRetrieveArtifactException(ARTIFACT_INSTANCE_NOT_FOUND, ERROR, TEST_URL, new Throwable());
        } catch (final UnableToRetrieveArtifactException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleUnableToRetrieveArtifactException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1005, ARTIFACT_INSTANCE_NOT_FOUND, TEST_URL, NOT_FOUND);
        }
    }

    @Test
    public void givenHelmOrchestratorException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_OPERATION_ERROR, ERROR, TEST_URL, new Throwable());
        } catch (final HelmOrchestratorException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleHelmOrchestratorException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1010, HELM_ORCHESTRATOR_OPERATION_ERROR, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    public void givenAnAppLCMDataAccessException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppLcmDataAccessException(APP_LCM_DATA_ACCESS_ERROR, ERROR, TEST_URL);
        } catch (final AppLcmDataAccessException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleDataAccessException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1015, APP_LCM_DATA_ACCESS_ERROR, TEST_URL, NOT_FOUND);
        }
    }

    @Test
    public void givenAnAppLCMDataAccessExceptionWithThrowableTest_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new AppLcmDataAccessException(APP_LCM_DATA_ACCESS_ERROR, ERROR, TEST_URL, new Throwable());
        } catch (final AppLcmDataAccessException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleDataAccessException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1015, APP_LCM_DATA_ACCESS_ERROR, TEST_URL, NOT_FOUND);
        }
    }

    @Test
    public void givenInvalidInputException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new InvalidInputException(INVALID_INPUT_EXCEPTION, ERROR);
        } catch (final InvalidInputException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleInvalidInputException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1017, INVALID_INPUT_EXCEPTION, null, BAD_REQUEST);
        }
    }

    @Test
    public void givenAFailureToUpdateException_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new FailureToUpdateException(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND, ERROR, APP_INSTANCES_URL);
        } catch (final FailureToUpdateException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleFailureToUpdateException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1005, ARTIFACT_INSTANCE_NOT_FOUND, APP_INSTANCES_URL,
                BAD_REQUEST);
        }
    }

    @Test
    public void givenAFailureToUpdateExceptionWithThrowableTest_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new FailureToUpdateException(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND, ERROR, APP_INSTANCES_URL, new Throwable());
        } catch (final FailureToUpdateException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleFailureToUpdateException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1005, ARTIFACT_INSTANCE_NOT_FOUND, APP_INSTANCES_URL,
                    BAD_REQUEST);
        }
    }

    @Test
    public void givenInvalidInputExceptionWithThrowable_whenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new InvalidInputException(INVALID_INPUT_EXCEPTION, ERROR, new Throwable());
        } catch (final InvalidInputException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleInvalidInputException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1017, INVALID_INPUT_EXCEPTION, null, BAD_REQUEST);
        }
    }

    @Test
    public void givenAKeycloakExceptionException_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new KeycloakException(FAILURE_TO_CREATE_SECRET, ERROR, TEST_URL);
        } catch (final KeycloakException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleKeycloakException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1023, FAILURE_TO_CREATE_SECRET, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    public void givenAKeycloakExceptionWithThrowableTest_WhenExceptionIsValid_ThenProblemDetailsResponseContainsCorrectExceptionData() {
        try {
            throw new KeycloakException(FAILURE_TO_CREATE_SECRET, ERROR, TEST_URL, new Throwable());
        } catch (final KeycloakException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleKeycloakException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 1023, FAILURE_TO_CREATE_SECRET, TEST_URL, BAD_REQUEST);
        }
    }

    @Test
    void givenFailureToDeleteExceptionForNotTerminatedOrNotFailed_whenExceptionIsValid_ThenMultiDeleteErrorMessageResponseContainsCorrectExceptionData() {
        final Long totalSuccessfulDeletion = 1L;
        List<MultiDeleteFailureDetails> errorData = getMultiDeleteFailureDetails(totalSuccessfulDeletion, APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED);

        try {
            throw new FailureToDeleteException(APP_LCM_PARTIAL_DELETE_FAILURE, totalSuccessfulDeletion, errorData);
        } catch (final FailureToDeleteException e) {
            final ResponseEntity<MultiDeleteErrorMessage> responseEntity = applicationExceptionHandler.handleDeleteException(e);
            assertEquals(OK, responseEntity.getStatusCode());
            assertEquals(totalSuccessfulDeletion, responseEntity.getBody().getTotalSuccessful());
            assertEquals(APP_LCM_PARTIAL_DELETE_FAILURE.getErrorCode(), responseEntity.getBody().getAppLcmErrorCode());
            assertEquals(APP_LCM_PARTIAL_DELETE_FAILURE.getErrorMessage(), responseEntity.getBody().getAppLcmErrorMessage());
            assertEquals(errorData, responseEntity.getBody().getErrorData());
        }
    }

    @Test
    void givenAppLcmServiceExceptionForServiceUnavailable_whenExceptionIsValid_ThenErrorMessageResponseContainsCorrectExceptionData() {
        try {
            throw new AppLcmServiceException(APP_ON_BOARDING_SERVICE_UNAVAILABLE, ERROR, TEST_URL, new Throwable());
        } catch (final AppLcmServiceException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleServerException(e);
            verifyException(responseEntity.getBody(), responseEntity.getStatusCode(), 2002, APP_ON_BOARDING_SERVICE_UNAVAILABLE, TEST_URL, SERVICE_UNAVAILABLE);
        }
    }

    @Test
    void givenAppOnBoardingDeleteExceptionForInternalError_whenExceptionIsValid_ThenErrorMessageResponseContainsCorrectExceptionData() {
        try {
            throw new AppOnBoardingDeleteException(APP_ON_BOARDING_DELETE_ERROR, ERROR, TEST_URL);
        } catch (final AppOnBoardingDeleteException e) {
            final ResponseEntity<ProblemDetails> responseEntity = applicationExceptionHandler.handleAppOnBoardingDeleteException(e);
            verifyException(responseEntity.getBody(),
                            responseEntity.getStatusCode(),
                            1034,
                            APP_ON_BOARDING_DELETE_ERROR,
                            TEST_URL,
                            INTERNAL_SERVER_ERROR);
        }
    }

    private List<MultiDeleteFailureDetails> getMultiDeleteFailureDetails(final Long totalSuccessfulDeletion, final AppLcmError error) {
        List<MultiDeleteFailureDetails> errorData = new ArrayList<>();
        MultiDeleteFailureDetails details = new MultiDeleteFailureDetails();
        details.setAppInstanceId(totalSuccessfulDeletion);
        details.setAppLcmErrorCode(error.getErrorCode());
        details.setFailureMessage(error.getErrorMessage());
        errorData.add(details);
        return errorData;
    }

    private void verifyException(final ProblemDetails result, final HttpStatusCode resultCode, final int errorCode, final AppLcmError appLcmError,
                                 final String url, final HttpStatus status) {
        assert result != null;
        assertThat(appLcmError.getErrorCodeAsString()).isEqualTo(String.valueOf(appLcmError.getErrorCode()));
        assertThat(result.getAppLcmErrorCode()).isEqualTo(errorCode);
        assertThat(result.getAppLcmErrorMessage()).isEqualTo(appLcmError.getErrorMessage());
        assertThat(result.getDetail()).isEqualTo(ERROR);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(resultCode).isEqualTo(status);
    }
}
