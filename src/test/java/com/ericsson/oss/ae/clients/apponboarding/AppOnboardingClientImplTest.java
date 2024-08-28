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

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.constants.AppLcmConstants;
import com.ericsson.oss.ae.presentation.exceptions.*;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.rest.RequestHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_UPDATE_STATUS_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { AppLcmApplication.class, AppOnboardingClientImpl.class })
class AppOnboardingClientImplTest {

    @Autowired
    private AppOnboardingClientImpl objectUnderTest;
    @Autowired
    private UrlGenerator urlGenerator;
    @MockBean
    private RequestHandler requestHandler;

    @Test
    public void givenGetRequestForAppById_whenRequestProcessedSuccessfully_thenAppReturned() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateAppByIdUrl(1L), AppDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET))
                .thenReturn(new ResponseEntity(HttpStatus.OK));

        final ResponseEntity actualAppResponse = objectUnderTest.getAppById(1L);

        assertThat(actualAppResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenGetRequestForAppById_whenErrorProcessingRequest_thenExceptionThrown() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateAppByIdUrl(1L), AppDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET))
                .thenThrow(new RestClientException("test"));

        final AppOnboardingAppNotExistException actualException = assertThrows(AppOnboardingAppNotExistException.class, () -> {
            objectUnderTest.getAppById(1L);
        });
        assertThat(actualException.getAppLcmError()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving application by ID 1");
    }

    @Test
    public void givenGetRequestForAppById_whenOnBoardingServiceErrorProcessingRequest_thenResourceAccessExceptionThrown() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateAppByIdUrl(1L), AppDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET))
            .thenThrow(new ResourceAccessException("test"));

        final AppLcmServiceException actualException = assertThrows(AppLcmServiceException.class, () -> {
            objectUnderTest.getAppById(1L);
        });
        assertThat(actualException.getAppLcmError()).isEqualTo(APP_ON_BOARDING_SERVICE_UNAVAILABLE);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving application by ID 1");
    }

    @Test
    public void givenGetRequestForAppArtifactsByAppId_whenRequestProcessedSuccessfully_thenAppArtifactsReturned() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateArtifactsByAppIdUrl(1L), List.class, MediaType.APPLICATION_JSON,
                HttpMethod.GET)).thenReturn(new ResponseEntity(HttpStatus.ACCEPTED));

        final ResponseEntity actualAppResponse = objectUnderTest.getAppArtifacts(1L);

        assertThat(actualAppResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    public void givenGetRequestForAppArtifactsByAppId_whenErrorProcessingRequest_thenExceptionThrown() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateArtifactsByAppIdUrl(1L), List.class, MediaType.APPLICATION_JSON,
                HttpMethod.GET)).thenThrow(new RestClientException("test"));

        final AppOnboardingArtifactRetrievalException actualException = assertThrows(AppOnboardingArtifactRetrievalException.class, () -> {
            objectUnderTest.getAppArtifacts(1L);
        });
        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.APP_ONBOARDING_APP_ARTIFACT_NOT_FOUND);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving artifacts for App with ID 1");
    }

    @Test
    public void givenGetRequestForAppArtifactFile__whenRequestProcessedSuccessfully_thenAppArtifactFileReturned() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateAppArtifactFileUrl(1L, 1L), ByteArrayResource.class, MediaType.ALL,
                HttpMethod.GET)).thenReturn(new ResponseEntity(HttpStatus.ACCEPTED));

        final ResponseEntity actualResponse = objectUnderTest.getAppArtifactFile(1L, 1L);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void givenGetRequestForAppArtifactFile_whenErrorProcessingRequest_thenExceptionThrown() {
        when(requestHandler.createAndSendRestRequest(urlGenerator.generateAppArtifactFileUrl(1L, 1L), ByteArrayResource.class, MediaType.ALL,
                HttpMethod.GET)).thenThrow(new RestClientException("test"));

        final AppOnboardingArtifactRetrievalException actualException = assertThrows(AppOnboardingArtifactRetrievalException.class, () -> {
            objectUnderTest.getAppArtifactFile(1L, 1L);
        });
        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.ARTIFACT_INSTANCE_FILE_ERROR);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving artifact file by App ID 1 and Artifact ID 1");
    }
    //update status for deletion
    @Test
    public void givenPutRequestForValidAppId_whenUpdateStatusForDeletion_thenReturnResponseEntityOk(){
        final Long appId = 1L;
        final Map<String, Object> deletingDto = new HashMap<>();
        deletingDto.put(AppLcmConstants.STATUS, AppLcmConstants.DELETING.toUpperCase(Locale.ROOT));

        when(requestHandler.sendSimpleBodyRestRequest(deletingDto, urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, HttpMethod.PUT))
            .thenReturn(new ResponseEntity(HttpStatus.OK));

        final ResponseEntity actualResponse = objectUnderTest.updateStatusForDeletion(appId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenPutRequestForValidAppIdWithOnBoardingServiceUnavailable_whenUpdateStatusForDeletion_thenReturnAppLcmServerException(){
        final Long appId = 1L;
        final Map<String, Object> deletingDto = new HashMap<>();
        deletingDto.put(AppLcmConstants.STATUS, AppLcmConstants.DELETING.toUpperCase(Locale.ROOT));

        when(requestHandler.sendSimpleBodyRestRequest(deletingDto, urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, HttpMethod.PUT))
            .thenThrow(new ResourceAccessException("test"));

        final AppLcmServiceException actualException = assertThrows(AppLcmServiceException.class, () -> {
            objectUnderTest.updateStatusForDeletion(1L);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(APP_ON_BOARDING_SERVICE_UNAVAILABLE);
        assertThat(actualException.getMessage()).isEqualTo(APP_ONBOARDING_UPDATE_STATUS_ERROR+ appId);
    }

    @Test
    public void givenPutRequestForInvalidApp_whenUpdateStatusForDeletion_thenReturnResponseEntityBadRequest(){
        final Long appId = 1L;
        final Map<String, Object> deletingDto = new HashMap<>();
        deletingDto.put(AppLcmConstants.STATUS, AppLcmConstants.DELETING.toUpperCase(Locale.ROOT));

        when(requestHandler.sendSimpleBodyRestRequest(deletingDto, urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, HttpMethod.PUT))
            .thenReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));

        final ResponseEntity actualResponse = objectUnderTest.updateStatusForDeletion(appId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void givenPutRequestForInvalidApp_whenUpdateStatusForDeletion_thenExceptionThrowForUpdatingStatus(){
        final Long appId = 1L;
        final Map<String, Object> deletingDto = new HashMap<>();
        deletingDto.put(AppLcmConstants.STATUS, AppLcmConstants.DELETING.toUpperCase(Locale.ROOT));

        when(requestHandler.sendSimpleBodyRestRequest(deletingDto, urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, HttpMethod.PUT))
            .thenThrow(RestClientException.class);
        try{
            objectUnderTest.updateStatusForDeletion(appId);
        }catch (AppOnboardingAppNotExistException e){
            assertThat(e.getAppLcmError())
                .isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
            assertThat(e.getMessage())
                .isEqualTo(APP_ONBOARDING_UPDATE_STATUS_ERROR + appId);
        }
    }
    //delete package
    @Test
    public void givenDeleteRequestForValidAppId_whenDeletePackage_thenReturnResponseEntityNoContent(){
        final Long appId = 1L;

        when(requestHandler.createAndSendRestRequest(
            urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE))
            .thenReturn(new ResponseEntity(HttpStatus.NO_CONTENT));

        final ResponseEntity actualResponse = objectUnderTest.deletePackage(appId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void givenDeleteRequestForValidAppIdWithOnBoardingServiceUnavailable_whenDeletePackage_thenReturnAppLcmServerException(){
        final Long appId = 1L;

        when(requestHandler.createAndSendRestRequest(
            urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE))
            .thenThrow(new ResourceAccessException("test"));

        final ResourceAccessException actualException = assertThrows(ResourceAccessException.class, () -> {
            objectUnderTest.deletePackage(1L);
        });

        assertThat(actualException.getMessage()).isEqualTo("test");
    }

    @Test
    public void givenDeleteRequestForValidAppId_whenDeletePackage_thenReturnResponseEntityBadRequest(){
        final Long appId = 1L;

        when(requestHandler.createAndSendRestRequest(
            urlGenerator.generateAppOnBoardingWithIdUrl(appId), Void.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE))
            .thenReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));

        final ResponseEntity actualResponse = objectUnderTest.deletePackage(appId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}