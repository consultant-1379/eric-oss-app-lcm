/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

package com.ericsson.oss.ae.acm.core.services.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;


/**
 * Class for handling deploy app instance action
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeployActionHandler {

    @Autowired
    private final AcmService acmService;

    @Autowired
    private final AppInstancesRepository appInstancesRepository;

    /**
     * Deploy app instance
     *
     * @param appInstance           - app instance details
     * @param appInstanceManagementRequest - deploy api request details
     */
    public void deployAppInstance(final AppInstances appInstance, final AppInstanceManagementRequest appInstanceManagementRequest) {
        final App app = appInstance.getApp();
        try {
            log.debug("Call ACM-R to DEPLOY app instance for the instance id: {}", appInstance.getId());
            acmService.deployAutomationCompositionInstance(app.getCompositionId(), appInstance.getCompositionInstanceId());
            log.debug("Update app instance status to DEPLOYING for instance id: {}", appInstance.getId());
            updateAppInstanceStatus(appInstance, AppInstanceStatus.DEPLOYING);
        } catch (final RestRequestFailedException ex) {
            log.error("Request failure when calling ACM-R to DEPLOY app instance with instanceId: {}. Reason: {}", appInstance.getId(), ex.getErrorDetails(), ex);
            updateAppInstanceStatus(appInstance, AppInstanceStatus.DEPLOY_ERROR);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DEPLOY_APP_INSTANCE_ERROR, new String[]{ex.getErrorDetails()});
        }
    }

    private void updateAppInstanceStatus(final AppInstances appInstance, final AppInstanceStatus instanceStatus) {
        appInstance.setStatus(instanceStatus);
        appInstancesRepository.save(appInstance);
    }
}
