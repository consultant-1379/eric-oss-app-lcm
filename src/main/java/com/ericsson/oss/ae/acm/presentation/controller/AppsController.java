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

import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_DEINITIALIZE_APP_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_DELETE_APP_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_DISABLE_APP_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_ENABLE_APP_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLogMessages.V3_INITIALIZE_APP_KEYWORD;
import static com.ericsson.oss.ae.acm.common.constant.AuditLoggingConstants.ADDITIONAL_MESSAGE_KEY;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.core.services.AppService;
import com.ericsson.oss.ae.acm.core.services.handlers.InitializeAction;
import com.ericsson.oss.ae.acm.presentation.filter.GetAppsFilter;
import com.ericsson.oss.ae.v3.api.AppsApi;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppInitializeOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppItems;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponse;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.EnableDisableAppRequest;
import com.ericsson.oss.ae.v3.api.model.InitializeActionRequest;

@RestController
@RequestMapping("${eric-oss-app-lcm_v3.base-path:/v3}")
@Slf4j
@RequiredArgsConstructor
public class AppsController implements AppsApi {

    @Autowired
    private final AppService appsService;

    @Autowired
    private NativeWebRequest nativeWebRequest;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(nativeWebRequest);
    }

    @Override
    public ResponseEntity<AppDetails> createApp(final CreateAppRequest createAppRequest) {
        log.info("V3 create app for {} version {} provider {}",
                createAppRequest.getName(), createAppRequest.getVersion(), createAppRequest.getProvider());
        return new ResponseEntity<>(appsService.createApp(createAppRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<AppItems> getApps(String name, String version, String mode, String status, String type,
                                            String offset, String limit, String accept) {
        GetAppsFilter getAppsFilter = new GetAppsFilter.GetAppsFilterBuilder().name(name).version(version).mode(mode).status(status).type(type)
            .offset(offset).limit(limit).build();
        log.info("V3 get apps for query: {}", getAppsFilter);

        return new ResponseEntity<>(appsService.getApps(getAppsFilter), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AppDetails> getAppById(final String appId, String accept) {
        log.info("V3 get app details for path by id - {}", appId);
        return new ResponseEntity<>(appsService.getAppById(appId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AppOperationResponse> enableDisableApp(
        final String appId,
        final EnableDisableAppRequest enableDisableAppRequest,
        @RequestHeader(value = "Accept", required = false) String accept,
        @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
        if (enableDisableAppRequest.getMode().equals(AppMode.DISABLED)) {
            setAdditionalAuditLogMessage(String.format(V3_DISABLE_APP_KEYWORD, appId));
        } else {
            setAdditionalAuditLogMessage(String.format(V3_ENABLE_APP_KEYWORD, appId));
        }
        log.info("V3 enable/disable app, app id - {}", appId);
        return new ResponseEntity<>(appsService.enableDisableApp(appId, enableDisableAppRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteAppById(final String appId, String accept) {
        setAdditionalAuditLogMessage(String.format(V3_DELETE_APP_KEYWORD, appId));
        log.info("Deleting app for appId - {}", appId);
        appsService.deleteAppById(appId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<AppInitializeOperationResponse> executeInitializeAction(
        final String appId,
        final InitializeActionRequest initializeActionRequest,
        @RequestHeader(value = "Accept", required = false) String accept,
        @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
        if (InitializeAction.getSpecificActionType(initializeActionRequest.getAction()) == InitializeAction.INITIALIZE) {
            setAdditionalAuditLogMessage(String.format(V3_INITIALIZE_APP_KEYWORD, appId));
        } else {
           setAdditionalAuditLogMessage(String.format(V3_DEINITIALIZE_APP_KEYWORD, appId));
        }
        log.info("Initialize App action requested with action type {} for appId {}", initializeActionRequest, appId);
        final AppInitializeOperationResponse response = appsService.executeInitializeAction(appId, initializeActionRequest);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    private void setAdditionalAuditLogMessage(final String additionalMessage) {
        getRequest().ifPresent(request -> request.setAttribute(ADDITIONAL_MESSAGE_KEY, additionalMessage, NativeWebRequest.SCOPE_REQUEST));
    }
}