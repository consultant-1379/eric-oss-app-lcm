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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.utils.MapperUtil;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Class for handling upgrade action
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpgradeActionHandler {

    @Autowired
    private final AcmService acmService;

    @Autowired
    private final AppInstancesRepository appInstancesRepository;

    @Autowired
    private final MapperUtil mapperUtil;

    @Autowired
    private final AcmFileGenerator acmFileGenerator;

    @Autowired
    private final KeycloakHandler keycloakHandler;

    /**
     * Upgrade App instance
     *
     * @param appInstance                   - App instance details
     * @param targetAppDetails              - Target App details
     * @param appInstanceManagementRequest  - Upgrade request
     */
    public void executeAppInstanceUpgrade(
            final AppInstances appInstance,
            final App targetAppDetails,
            final AppInstanceManagementRequest appInstanceManagementRequest
    ) throws AppLcmException {
        setTargetAppDetails(appInstance, targetAppDetails);
        final List<AppComponentInstance> appComponentInstances = new ArrayList<>();
        setAppComponentInstances(appInstance, appComponentInstances);
        log.info("Update keycloak credentials");
        keycloakHandler.updateExistingClientRoles(appInstance, targetAppDetails);
        CompositionInstanceData compositionInstanceData = createCompositionInstanceData(appInstance, appInstanceManagementRequest);
        log.info("Generate service property yaml file for App instance : {}", appInstance.getId());
        final String acmServicePropertyForUpgrade = generateServicePropertyFile(compositionInstanceData);
        try {
            log.info("Send API Call to ACM for Upgrade AC instance with instance id: {} ", appInstance.getId());
            upgradeAcInstance(acmServicePropertyForUpgrade, appInstance);
        } catch (RestRequestFailedException ex) {
            log.error("Exception while calling ACM-R", ex);
            updateAppInstanceErrorDetails(appInstance.getId(), ex.getErrorDetails());
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.UPGRADE_APP_INSTANCE_ERROR, new String[]{ex.getErrorDetails()});
        }
        saveAppInstanceDetails(appInstance, appComponentInstances);
    }

    private void setTargetAppDetails(final AppInstances appInstance, final App targetAppDetails) {
        appInstance.setTargetApp(targetAppDetails);
    }

    private void setAppComponentInstances(final AppInstances appInstance, final List<AppComponentInstance> appComponentInstances) {
        appComponentInstances.addAll(appInstance.getAppComponentInstances());
        appInstance.getAppComponentInstances().removeAll(appComponentInstances);
        appInstance.getTargetApp().getAppComponents().stream().forEach(appComponent -> {
            final AppComponentInstance appComponentInstance = new AppComponentInstance();
            appComponentInstance.setAppId(appInstance.getTargetApp().getId());
            appComponentInstance.setAppComponent(appComponent);
            final Optional<UUID> compositionElementInstanceId = appComponentInstances.stream().filter(existingComponentInstance -> appComponent.getName().equalsIgnoreCase(existingComponentInstance.getAppComponent().getName()))
                    .map(existingComponentInstance -> existingComponentInstance.getCompositionElementInstanceId()).findFirst();
            if (!compositionElementInstanceId.isPresent()) {
                throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.UPGRADE_APP_INSTANCE_COMPONENT_MISMATCH_ERROR, new String[]{appComponent.getName()});
            }
            appComponentInstance.setCompositionElementInstanceId(compositionElementInstanceId.get());
            appComponentInstance.setAppInstance(appInstance);
            appInstance.getAppComponentInstances().add(appComponentInstance);
        });
    }

    private CompositionInstanceData createCompositionInstanceData(final AppInstances appInstance, final AppInstanceManagementRequest appInstanceManagementRequest) {
        CompositionInstanceData compositionInstanceData;
        if(appInstanceManagementRequest.getAdditionalData() !=null && appInstanceManagementRequest.getAdditionalData().getComponentInstances() != null) {
            compositionInstanceData = new CompositionInstanceData(appInstance, appInstanceManagementRequest.getAdditionalData().getComponentInstances());
        } else {
            compositionInstanceData = new CompositionInstanceData(appInstance);
        }
        return compositionInstanceData;
    }

    private String generateServicePropertyFile(final CompositionInstanceData compositionInstanceData) {
        return acmFileGenerator.generateAcmInstancePropertiesFile(compositionInstanceData);
    }

    private void upgradeAcInstance(final String acmServicePropertyForUpgrade, final AppInstances appInstance) {
        acmService.updateAutomationCompositionInstance(acmServicePropertyForUpgrade, appInstance.getApp().getCompositionId());
    }

    private void saveAppInstanceDetails(final AppInstances appInstance, final List<AppComponentInstance> appComponentInstance) {
        appInstance.getAppComponentInstances().addAll(appComponentInstance);
        appInstance.setStatus(AppInstanceStatus.UPGRADING);
        appInstancesRepository.save(appInstance);
    }

    private void updateAppInstanceErrorDetails(final UUID instanceId, final String errorDetails) {
        log.info("The AppInstance: {} is upgrading ", instanceId);
        final Optional<AppInstances> appInstances = appInstancesRepository.findById(instanceId);
        if (appInstances.isPresent()) {
            final AppInstanceEvent appInstanceEvent = AppInstanceEvent.builder().appInstance(appInstances.get()).type(EventType.ERROR)
                    .title(AppLcmError.UPGRADE_APP_INSTANCE_ERROR.getErrorTitle()).detail(errorDetails)
                    .build();
            appInstances.get().getAppInstanceEvents().add(appInstanceEvent);
            appInstances.get().setStatus(AppInstanceStatus.DEPLOY_ERROR);
            appInstancesRepository.save(appInstances.get());
        }
    }

}
