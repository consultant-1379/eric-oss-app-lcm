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


import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;

import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Handler class that contains the logic to execute the 'Undeploy App Instance' use case.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UndeployActionHandler {

    @Autowired
    private final AppInstancesRepository appInstancesRepository;

    @Autowired
    private final AcmService acmService;

    public void undeployAppInstance(final AppInstances appInstance) {
        final App app = appInstance.getApp();
        final DeployState currentAcmInstanceDeployState = getAcmInstanceDeployState(app, appInstance);

        if (currentAcmInstanceDeployState.equals(DeployState.UNDEPLOYED) || currentAcmInstanceDeployState.equals(DeployState.UNDEPLOYING)) {
            log.debug("ACM instance deploy state for instance id {} is already in {}. Setting app instance status as UNDEPLOYING in DB", appInstance.getId(), currentAcmInstanceDeployState);
            updateAppInstanceStatus(appInstance, AppInstanceStatus.UNDEPLOYING);
        } else {
            try {
                log.info("Call ACM-R to undeploy app instance for the instance id: {}", appInstance.getId());
                acmService.undeployAutomationCompositionInstance(app.getCompositionId(), appInstance.getCompositionInstanceId());
                log.debug("Update app instance status to UNDEPLOYING for instance id: {}", appInstance.getId());
                updateAppInstanceStatus(appInstance, AppInstanceStatus.UNDEPLOYING);
            } catch (final RestRequestFailedException ex) {
                log.error("Request failure when calling ACM-R to undeploy app instance with instanceId: {}. Reason: {}", appInstance.getId(), ex.getErrorDetails(), ex);
                updateAppInstanceStatus(appInstance, AppInstanceStatus.UNDEPLOY_ERROR);
                throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.UNDEPLOY_APP_INSTANCE_ERROR);
            }
        }
    }

    public DeployState getAcmInstanceDeployState(final App app, final AppInstances appInstance) {
        try {
            final AutomationCompositionInstance automationCompositionInstance = acmService.getAutomationCompositionInstance(app.getCompositionId(), appInstance.getCompositionInstanceId());
            return automationCompositionInstance.getDeployState();
        } catch (final RestRequestFailedException ex) {
            log.error("Automation Composition instance for ID {} and compositionId {} was not fond in ACM-R", appInstance.getCompositionInstanceId(), app.getCompositionId(), ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.COMPOSITION_INSTANCE_NOT_FOUND);
        }
    }

    private void updateAppInstanceStatus(final AppInstances appInstance, final AppInstanceStatus instanceStatus) {
        appInstance.setStatus(instanceStatus);
        appInstancesRepository.save(appInstance);
    }

}
