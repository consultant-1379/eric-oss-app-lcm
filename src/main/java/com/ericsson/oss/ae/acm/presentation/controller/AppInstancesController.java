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

package com.ericsson.oss.ae.acm.presentation.controller;

import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_CREATE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_DELETE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_DEPLOY_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_UNDEPLOY_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_UPDATE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_UPGRADE_APP_INSTANCE_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLoggingConstants.ADDITIONAL_MESSAGE_KEY;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.core.services.AppInstancesService;
import com.ericsson.oss.ae.acm.enums.AppInstanceAction;
import com.ericsson.oss.ae.v3.api.AppInstancesApi;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceItems;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

@RestController
@RequestMapping("${eric-oss-app-lcm_v3.base-path:/v3}")
@Slf4j
@RequiredArgsConstructor
public class AppInstancesController implements AppInstancesApi {

    @Autowired
    private final AppInstancesService appInstancesService;

    @Autowired
    private NativeWebRequest nativeWebRequest;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(nativeWebRequest);
    }

    @Override
    public ResponseEntity<AppInstanceItems> getAppInstances(
        @RequestParam(required = false) final String appId,
        @RequestParam(required = false, defaultValue = "0") String offset,
        @RequestParam(required = false, defaultValue = "10") String limit,
        @RequestHeader(value = "Accept", required = false) String accept
    ) {
        log.info("Getting app instances. Optional query parameter appId is {}", appId);
        return new ResponseEntity<>(appInstancesService.getAppInstances(appId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AppInstance> getAppInstanceById(
        final String appInstanceId,
        @RequestHeader(value = "Accept", required = false) String accept
    ) {
        log.info("Getting app instance for appInstanceId - {}", appInstanceId);
        final AppInstance appInstance = appInstancesService.getAppInstanceById(appInstanceId);
        return new ResponseEntity<>(appInstance, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AppInstance> createAppInstance(final CreateAppInstanceRequest createAppInstanceRequest) {
        setAdditionalAuditLogMessage(String.format(V3_CREATE_APP_INSTANCE_KEYWORD, "n/a"));
        log.info("Create app instance for appId - {}", createAppInstanceRequest.getAppId());
        AppInstance appInstance = appInstancesService.createAppInstance(createAppInstanceRequest);
        setAdditionalAuditLogMessage(String.format(V3_CREATE_APP_INSTANCE_KEYWORD, appInstance.getId()));
        return new ResponseEntity<>(appInstance, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<AppInstanceManagementResponse> manageAppInstance(
        final String instanceId,
        final AppInstanceManagementRequest appInstanceManagementRequest,
        @RequestHeader(value = "Accept", required = false) String accept,
        @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
        if (Optional.ofNullable(appInstanceManagementRequest.getType()).map(Enum::name).orElse("").equals(AppInstanceAction.DEPLOY.name())) {
            setAdditionalAuditLogMessage(String.format(V3_DEPLOY_APP_INSTANCE_KEYWORD, instanceId));
        } else if (Optional.ofNullable(appInstanceManagementRequest.getType()).map(Enum::name).orElse("").equals(AppInstanceAction.UNDEPLOY.name())) {
            setAdditionalAuditLogMessage(String.format(V3_UNDEPLOY_APP_INSTANCE_KEYWORD, instanceId));
        } else if (Optional.ofNullable(appInstanceManagementRequest.getType()).map(Enum::name).orElse("").equals(AppInstanceAction.UPGRADE.name())) {
            setAdditionalAuditLogMessage(String.format(V3_UPGRADE_APP_INSTANCE_KEYWORD, instanceId));
        }

        log.info("{} type for appInstanceId {}",
                appInstanceManagementRequest.getType(),
            instanceId);

        return new ResponseEntity<>(appInstancesService.manageAppInstance(instanceId, appInstanceManagementRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<AppInstanceOperationResponse> deleteAppInstance(
        final String instanceId,
        @RequestHeader(value = "Accept", required = false) String accept
    ) {
        setAdditionalAuditLogMessage(String.format(V3_DELETE_APP_INSTANCE_KEYWORD, instanceId));
        log.info("Deleting an app instance for instanceId: {}", instanceId);
        return new ResponseEntity<>(appInstancesService.deleteAppInstance(instanceId), HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<AppInstanceUpdateResponse> updateAppInstance(
        final String appInstanceId,
        final UpdateAppInstanceRequest updateAppInstanceRequest,
        @RequestHeader(value = "Accept", required = false) String accept,
        @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
        setAdditionalAuditLogMessage(String.format(V3_UPDATE_APP_INSTANCE_KEYWORD, appInstanceId));
        log.info("Updating an app instance for appInstanceId: {}", appInstanceId);
        AppInstanceUpdateResponse updateResponse = appInstancesService.updateAppInstance(appInstanceId, updateAppInstanceRequest);
        if(appInstancesService.getAppInstanceById(appInstanceId).getStatus().equals(AppInstanceStatus.UNDEPLOYED)) {
            return new ResponseEntity<>(updateResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(updateResponse, HttpStatus.ACCEPTED);
        }
    }

    private void setAdditionalAuditLogMessage(final String additionalMessage) {
        getRequest().ifPresent(request -> request.setAttribute(ADDITIONAL_MESSAGE_KEY, additionalMessage, NativeWebRequest.SCOPE_REQUEST));
    }
}
