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

package com.ericsson.oss.ae.acm.presentation.mapper;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponseAppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponseAppInstance;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.Credentials;
import com.ericsson.oss.ae.v3.api.model.Href;

/**
 * AppInstanceDetailsMapper for mapping data objects with App Instance entity.
 */
@Component
@AllArgsConstructor
@Slf4j
public class AppInstancesMapper {

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final LcmUrlGenerator lcmUrlGenerator;

    /**
     * Maps app instance entity with ACM instance data object.
     *
     * @param automationCompositionInstanceId
     *     - instance id from ACM
     * @param appInstanceStatus
     *     - instance status
     * @return AppInstances
     */
    public AppInstances toAppInstanceEntity(final UUID automationCompositionInstanceId, final App app, final AppInstanceStatus appInstanceStatus, final UUID appInstanceId) {
        log.info("Generate appInstance entity for the created ACM instance");
        final AppInstances appInstances = new AppInstances();
        appInstances.setId(appInstanceId);
        appInstances.setCompositionInstanceId(automationCompositionInstanceId);
        appInstances.setStatus(appInstanceStatus);
        appInstances.setApp(app);
        return appInstances;
    }

    /**
     * Maps an AppInstances Entity object instance and its values to an AppInstance object instance, and also adds the given
     * client credential information and ComponentInstance data to the returned AppInstance.
     * @param appInstancesEntity
     * @param componentInstances
     * @param clientCredential
     * @return the AppInstance with all AppInstance and ComponentInstance data.
     */
    public AppInstance toAppInstance(final AppInstances appInstancesEntity, final List<ComponentInstances> componentInstances,
                                     final ClientCredential clientCredential) {
        log.info("Generate AppInstance details");
        final AppInstance appInstance = modelMapper.map(appInstancesEntity, AppInstance.class);
        // This statement can be deleted once rAppId configured properly
        appInstance.setAppId(appInstancesEntity.getApp().getId().toString());
        appInstance.setComponentInstances(componentInstances);
        if (clientCredential != null) {
            final Credentials credentials = new Credentials().clientId(clientCredential.getClientId());
            appInstance.setCredentials(credentials);
        } else {
            log.warn("No client credential information was found for App Instance with ID {}", appInstance.getId());
        }
        appInstance.self(new Href().href(lcmUrlGenerator.getAppsInstanceUrlById(String.valueOf(appInstancesEntity.getId()))));
        appInstance.app(new Href().href(lcmUrlGenerator.getAppsUrlById(appInstancesEntity.getApp().getId())));

        return appInstance;
    }

    /**
     * Generates and returns an AppInstance entity for the created ACM instance.
     *
     * @param appInstanceId              The unique ID of the generated AppInstance.
     * @param app                        The application details.
     * @return                           The generated AppInstance entity.
     */
    public AppInstances generateAppInstanceEntity(UUID appInstanceId, App app) {
        log.info("Generate appInstance entity for the created ACM instance");
        final AppInstances appInstances = new AppInstances();
        appInstances.setId(appInstanceId);
        appInstances.setApp(app);
        return appInstances;
    }

    /**
     * Maps app instance entity with AppInstance data object for getting App Instance details.
     *
     * @param appInstances
     *     - app instance entity details
     * @return AppDetails
     */
    public AppInstanceOperationResponse toAppInstanceOperationResponse(final AppInstances appInstances) {
        log.info("Generate AppInstance details for the app instance");
        final AppInstanceOperationResponse appInstanceOperationResponse = new AppInstanceOperationResponse();
        final AppInstanceOperationResponseAppInstance appInstance = new AppInstanceOperationResponseAppInstance();
        appInstance.setStatus(appInstances.getStatus());
        appInstance.setHref(lcmUrlGenerator.getAppsInstanceUrlById(String.valueOf(appInstances.getId())));
        appInstanceOperationResponse.setAppInstance(appInstance);
        return appInstanceOperationResponse;
    }

    /**
     * Maps app instance with AppInstance data object for getting App Instance details.
     *
     * @param appInstance
     *     - app instance details
     * @return AppDetails
     */
    public AppInstanceUpdateResponse toAppInstanceUpdateResponse(final AppInstance appInstance) {
        log.info("Generate AppInstance details for the app instance");
        final AppInstanceUpdateResponse appInstanceUpdateResponse = new AppInstanceUpdateResponse();
        final AppInstanceUpdateResponseAppInstance appInstanceUpdateResponseAppInstance = new AppInstanceUpdateResponseAppInstance();
        appInstanceUpdateResponseAppInstance.setId(appInstance.getId());
        appInstanceUpdateResponseAppInstance.setHref(lcmUrlGenerator.getAppsInstanceUrlById(String.valueOf(appInstance.getId())));
        appInstanceUpdateResponse.setAppInstance(appInstanceUpdateResponseAppInstance);

        final List<ComponentInstances> componentInstancesList = appInstance.getComponentInstances();
        appInstanceUpdateResponse.setComponentInstances(componentInstancesList);

        return appInstanceUpdateResponse;
    }

    /**
     * Maps App instance entity with AppInstance data object for getting App Instance Management Response.
     *
     * @param appInstanceManagementRequest      - App instance management request
     * @param appInstances                      - App instance entity details
     * @return AppInstanceDetails
     */
    public AppInstanceManagementResponse toAppInstanceManagementResponse(final AppInstanceManagementRequest appInstanceManagementRequest, final AppInstances appInstances) {
        log.info("Generate AppInstance details for the App instance");
        final AppInstanceManagementResponse appInstanceManagementResponse = new AppInstanceManagementResponse();
        AppInstanceOperationResponseAppInstance appInstance = new AppInstanceOperationResponseAppInstance();
        appInstance.setHref(lcmUrlGenerator.getAppsInstanceUrlById(String.valueOf(appInstances.getId())));
        appInstance.setStatus(appInstances.getStatus());
        appInstanceManagementResponse.setAppInstance(appInstance);
        if(appInstanceManagementRequest.getAdditionalData() != null) {
            AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
            additionalData.setComponentInstances(appInstanceManagementRequest.getAdditionalData().getComponentInstances());
            appInstanceManagementResponse.setAdditionalData(additionalData);
        }
        if(appInstanceManagementRequest.getTargetAppId() != null){
            appInstanceManagementResponse.setTargetAppId(appInstanceManagementRequest.getTargetAppId());
        }
        appInstanceManagementResponse.setType(AppInstanceManagementResponse.TypeEnum.fromValue(appInstanceManagementRequest.getType().getValue()));
        return appInstanceManagementResponse;
    }
}
