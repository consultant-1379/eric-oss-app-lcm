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

package com.ericsson.oss.ae.clients.apponboarding;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.ae.constants.AppLcmConstants;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmServiceException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnboardingAppNotExistException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnboardingArtifactRetrievalException;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.rest.RequestHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_UPDATE_STATUS_ERROR;

/**
 * Implementation of App Onboarding REST client class {@link AppOnboardingClient}.
 * <p>
 * Contains methods used to make REST requests to App Onboarding service.
 */
@Service
@Slf4j
public class AppOnboardingClientImpl implements AppOnboardingClient {

    @Autowired
    private RequestHandler requestHandler;

    @Autowired
    private UrlGenerator urlGenerator;

    /**
     * Sends a GET request using {@link RequestHandler} to App Onboarding's Service '/v1/apps/{appId}' endpoint for a given appId.
     *
     * @param appId
     *            Used to identify and retrieve the App Onboarding App.
     *
     * @return Returns a {@link ResponseEntity} containing a {@link AppDto} response.
     */
    @Override
    public ResponseEntity getAppById(final Long appId) {
        final String appUrl = urlGenerator.generateAppByIdUrl(appId);
        log.debug("Get App Id, OnBoarding Url: {}", appUrl);
        try {
            return requestHandler.createAndSendRestRequest(appUrl, AppDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final ResourceAccessException exception) {
            final String message = "Error retrieving application by ID " + appId;
            log.error(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorMessage(), exception.getMessage());
            throw new AppLcmServiceException(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE, message, appUrl, exception);
        } catch (final RestClientException exception) {
            final String message = "Error retrieving application by ID " + appId;
            log.error(message, exception.getMessage());
            throw new AppOnboardingAppNotExistException(AppLcmError.APP_ONBOARDING_APP_NOT_FOUND, message, appUrl, exception);
        }
    }

    /**
     * Sends a GET request to App Onboarding Service Artifacts endpoint using {@link RequestHandler}.
     * <p>
     * An app can contain one or more artifacts. All Artifacts for the given 'appId' are contained in the returned response.
     *
     * @param appId
     *            Used to identify App Onboarding App and retrieve its Artifacts.
     *
     * @return Returns a {@link ResponseEntity} containing a List of {@link ArtifactDto} response.
     */
    @Override
    public ResponseEntity getAppArtifacts(final Long appId) {
        final String appArtifactUrl = urlGenerator.generateArtifactsByAppIdUrl(appId);
        log.info("Get App Artifacts OnBoarding Url: {}", appArtifactUrl);
        try {
            return requestHandler.createAndSendRestRequest(appArtifactUrl, List.class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestClientException exception) {
            final String message = "Error retrieving artifacts for App with ID " + appId;
            log.error(message, exception.getMessage());
            throw new AppOnboardingArtifactRetrievalException(AppLcmError.APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND, message, appArtifactUrl, exception);
        }
    }

    /**
     * Sends a GET request to App Onboarding service '/v1/apps/{appId}/artifacts/{artifactId}/file' endpoint using {@link RequestHandler}.
     * <p>
     * Each artifact has corresponding file. This files can be helm charts, docker images etc in the form of .tgz files.
     * <p>
     * The file is used to instantiate the application.
     *
     * @param appId
     *            Used to identify the App in the App Onboarding service.
     * @param artifactId
     *            Used to identify the Artifact in the App Onboarding service and retrieve its associated file.
     *
     * @return Returns a {@link ResponseEntity} containing a {@link ByteArrayResource} response.
     */
    @Override
    public ResponseEntity getAppArtifactFile(final Long appId, final Long artifactId) {
        final String appArtifactFileUrl = urlGenerator.generateAppArtifactFileUrl(appId, artifactId);
        log.info("Get App Artifacts File, OnBoarding Url: {}", appArtifactFileUrl);
        try {
            return requestHandler.createAndSendRestRequest(appArtifactFileUrl, ByteArrayResource.class, MediaType.ALL, HttpMethod.GET);
        } catch (final RestClientException exception) {
            final String message = "Error retrieving artifact file by App ID " + appId + " and Artifact ID " + artifactId;
            log.error(message, exception.getMessage());
            throw new AppOnboardingArtifactRetrievalException(AppLcmError.ARTIFACT_INSTANCE_FILE_ERROR, message, appArtifactFileUrl, exception);
        }
    }

    /**
     * Sends a PUT request to App OnBoarding Service endpoint using {@link RequestHandler}.
     * <p>
     * Update Status of Application intended to be deleted
     *
     * @param appId
     *            Used to identify App OnBoarding.
     *
     * @return Returns a {@link ResponseEntity} Http response.
     */
    @Override
    public ResponseEntity updateStatusForDeletion(Long appId) {
        final String updateStatusForDeletionUrl = urlGenerator.generateAppOnBoardingWithIdUrl(appId);
        log.info("Update Status For Deletion, OnBoarding URL: {}", updateStatusForDeletionUrl);
        final Map<String, Object> deletingDto = new HashMap<>();
        deletingDto.put(AppLcmConstants.STATUS, AppLcmConstants.DELETING.toUpperCase(Locale.ROOT));
        try {
            return requestHandler.sendSimpleBodyRestRequest(deletingDto, updateStatusForDeletionUrl,  Void.class, HttpMethod.PUT);
        } catch (final ResourceAccessException exception) {
            final String message = APP_ONBOARDING_UPDATE_STATUS_ERROR + appId;
            log.error(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorMessage(), exception.getMessage());
            throw new AppLcmServiceException(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE, message, updateStatusForDeletionUrl, exception);
        }catch (final RestClientException exception) {
            final String message = APP_ONBOARDING_UPDATE_STATUS_ERROR + appId;
            log.error(message, exception.getMessage());
            throw new AppOnboardingAppNotExistException(
                AppLcmError.APP_ONBOARDING_APP_NOT_FOUND, APP_ONBOARDING_UPDATE_STATUS_ERROR + appId, updateStatusForDeletionUrl, exception);
        }
    }

    /**
     * Sends a DELETE request to App OnBoarding Service endpoint using {@link RequestHandler}.
     * <p>
     * An app can be deleted for the given 'appId' on OnBoarding and returned NO_CONTENT response is expected.
     *
     * @param appId
     *            Used to identify App OnBoarding.
     *
     * @return Returns a {@link ResponseEntity} containing a List of {@link ArtifactDto} response.
     */
    @Override
    public ResponseEntity deletePackage(Long appId) {
        final String deleteAppOnBoardingUrl = urlGenerator.generateAppOnBoardingWithIdUrl(appId);
        log.info("Delete App OnBoarding Package, OnBoarding URL: {}", deleteAppOnBoardingUrl);
        return requestHandler.createAndSendRestRequest(
            deleteAppOnBoardingUrl,  Void.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE);
    }
}
