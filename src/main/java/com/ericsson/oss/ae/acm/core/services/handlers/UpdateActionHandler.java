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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;

/**
 * Class for handling update app instance action
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateActionHandler {

    @Autowired
    private final AcmService acmService;

    @Autowired
    private final AcmFileGenerator acmFileGenerator;

    /**
     * Update app instance
     *
     * @param appInstance                  - app instance details
     * @param componentInstances - Instance property list
     */
    public void updateAppInstance(final AppInstances appInstance, final List<ComponentInstances> componentInstances) {
        log.info("Update app instance for the given component instance properties for instance id: {}", appInstance.getId());
        final App app = appInstance.getApp();
        log.info("Create composition instance data for instance id: {}",appInstance.getId());
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstance,componentInstances);
        log.debug("Generate service property yaml file for instance id: {}", appInstance.getId());
        final String acmServiceProperty = acmFileGenerator.generateAcmInstancePropertiesFile(compositionInstanceData);
        log.debug("Update Automation Composition instance for automation composition instance id: {} and composition id: {}", appInstance.getCompositionInstanceId(), app.getCompositionId());
        try {
            log.debug("Call ACM-R to update app instance for the instance id: {}", appInstance.getId());
            acmService.updateAutomationCompositionInstance(acmServiceProperty, app.getCompositionId());
        } catch (final RestRequestFailedException ex) {
            log.error("Request failure when calling ACM-R to deploy app instance with instanceId: {}. Reason: {}", appInstance.getId(), ex.getErrorDetails(), ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.UPDATE_APP_INSTANCE_ERROR, new String[]{ex.getErrorDetails()});
        }
    }
}
