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
package com.ericsson.oss.ae.presentation.controllers;

import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V1_DELETE_ALL_APP_INSTANCES_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V1_DELETE_APP_INSTANCES_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V1_INSTANTIATE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V1_TERMINATE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V1_UPDATE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLoggingConstants.ADDITIONAL_MESSAGE_KEY;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.api.AppInstancesApi;
import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstanceListRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.api.model.MultiDeleteErrorMessage;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;


/**
 * Controller for App Instance.
 * <p>
 * Contains methods to interact with the {@link AppInstanceService} class
 */
@Slf4j
@RestController
@RequestMapping("/app-lcm/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontEnd.corsValue:corsValue}")
public class AppInstanceController implements AppInstancesApi {
    @Autowired
    private final AppInstanceService appInstanceService;

    @Autowired
    private NativeWebRequest nativeWebRequest;

    private static final Logger logger = LoggerFactory.getLogger(AppInstanceController.class);

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(nativeWebRequest);
    }

    /**
     * Invokes a POST request to {@link AppInstanceService}.
     *
     * @param appInstancePostRequest
     *            Post Request Containing AppInstance ID
     * @return Returns a ResponseEntity containing the HTTP Status & created App Object
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstanceDto> createAppInstance(final AppInstancePostRequestDto appInstancePostRequest) {
        setAdditionalAuditLogMessage(String.format(V1_INSTANTIATE_APP_INSTANCE_KEYWORD, "n/a"));
        logger.debug("Create Application Instance called");
        final AppInstanceDto appInstance = appInstanceService.create(appInstancePostRequest);
        logger.info("Successfully created Application Instance Id: {}", appInstance.getId());
        setAdditionalAuditLogMessage(String.format(V1_INSTANTIATE_APP_INSTANCE_KEYWORD, appInstance.getId()));
        return new ResponseEntity<>(appInstance, HttpStatus.CREATED);
    }
    /**
     * Invokes a GET request to {@link AppInstanceService}.
     *
     * @param appInstanceId
     *            Long Object containing the ID of the specified AppInstance
     * @return Returns a ResponseEntity containing the Http Status & appInstance Retrieved from {@link AppInstanceRepository}
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstanceDto> getAppInstanceById(final @PathVariable Long appInstanceId) {
        logger.info("Get App Instance Id: {}", appInstanceId);
        final AppInstanceDto appInstance = appInstanceService.getAppInstance(appInstanceId);
        logger.info("Successfully retrieved Instance Id: {}. Health Status: {}", appInstanceId, appInstance.getHealthStatus());
        return new ResponseEntity<>(appInstance, HttpStatus.OK);
    }
    /**
     * Invokes a PUT request to {@link AppInstanceService} to terminate an {@link AppInstance}.
     * <p>
     * The PUT request updates the HealthStatus & TargetStatus of the AppInstance to 'TERMINATED'
     * <p>
     * and marks the object for deletion.
     *
     * @param appInstanceId
     *            Long object containing the ID of the specified AppInstance
     * @return Returns a ResponseEntity containing the Http status of the terminate request
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<Void> terminateAppInstanceById(final Long appInstanceId) {
        setAdditionalAuditLogMessage(String.format(V1_TERMINATE_APP_INSTANCE_KEYWORD, appInstanceId));
        logger.debug("Terminate Application Instance by id {} called", appInstanceId);
        appInstanceService.terminate(appInstanceId);
        logger.info("Successfully requested Termination for instance id {}", appInstanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    /**
     * Invokes a GET request to {@link AppInstanceService}.
     *
     * @return Returns all AppInstances stored in Repository
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstancesDto> getAllAppInstances(Long id) {
        var logId = id == null ? "" : (" for App Id: " + id);
        logger.info("Get all Application Instances called {}", logId);
        final AppInstancesDto allAppInstances = appInstanceService.getAllAppInstances(id);
        logger.info("All Application Instances retrieved");
        return new ResponseEntity<>(allAppInstances, HttpStatus.OK);
    }

    /**
     * Invokes a PUT request to {@link AppInstanceService} to update an app.
     *
     * @param appInstancePutRequestDto
     *            AppInstancePutRequestDto is generated by openApi. Contains body of put request.
     * @return Returns a ResponseEntity containing the HTTP Status & created App Object
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstanceDto> updateAppInstanceById(final AppInstancePutRequestDto appInstancePutRequestDto) {
        setAdditionalAuditLogMessage(String.format(V1_UPDATE_APP_INSTANCE_KEYWORD, appInstancePutRequestDto.getAppInstanceId()));
        logger.debug("Update Application Instance called");
        final AppInstanceDto appInstance = appInstanceService.updateAppInstance(appInstancePutRequestDto);
        logger.info("Successfully requested update of Application Instance Id: {}", appInstancePutRequestDto.getAppInstanceId());
        return new ResponseEntity<>(appInstance, HttpStatus.OK);
    }

    /**
     * Invokes a DELETE request to {@link AppInstanceService} to delete an {@link AppInstance}.
     * <p>
     * The DELETE request remove all the resources from Helm Executor and App-Lcm
     * <p>
     *
     * @param appId
     *            Long object containing the ID of the specified Application
     * @param appInstanceListRequestDto
     *            AppInstanceListRequestDto is generated by openApi. Contains body of delete request.
     * @return Returns a ResponseEntity containing the Http status of the terminate request
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<MultiDeleteErrorMessage> deleteMultipleAppInstances(final Long appId, final AppInstanceListRequestDto appInstanceListRequestDto) {
        setAdditionalAuditLogMessage(String.format(V1_DELETE_APP_INSTANCES_KEYWORD, appId));
        logger.info("Delete multiple app instances called for app Id {}", appId);
        appInstanceService.deleteAppInstances(appId, appInstanceListRequestDto);
        logger.info("Requested App Instances deleted");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Invokes a DELETE request to {@link AppInstanceService} to delete an Application by Long appId.
     * <p>
     * The DELETE request remove all the resources from Helm Executor and App-Lcm for requested App.
     * <p>
     *
     * @param appId The Id of the app (required)
     * @return
     */
    
    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<MultiDeleteErrorMessage> deleteApplication(Long appId) {
        setAdditionalAuditLogMessage(String.format(V1_DELETE_ALL_APP_INSTANCES_KEYWORD, appId));
        logger.info("Delete app by app Id {}", appId);
        appInstanceService.deleteApp(appId);
        logger.info("Requested App is accepted for deletion");
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private void setAdditionalAuditLogMessage(final String additionalMessage) {
        getRequest().ifPresent(request -> request.setAttribute(ADDITIONAL_MESSAGE_KEY, additionalMessage, NativeWebRequest.SCOPE_REQUEST));
    }
}