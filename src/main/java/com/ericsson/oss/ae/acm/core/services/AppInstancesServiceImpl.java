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

package com.ericsson.oss.ae.acm.core.services;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_INSTANCE_COMPONENT_INSTANCE_REQUEST_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_INSTANCE_COMPONENT_MISMATCH_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.APP_NOT_FOUND_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.BAD_REQUEST_TARGET_APP_INVALID_STATE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcInstanceResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.core.services.builders.AppComponentInstancePropertyBuilder;
import com.ericsson.oss.ae.acm.core.services.handlers.DeployActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UndeployActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UpdateActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UpgradeActionHandler;
import com.ericsson.oss.ae.acm.core.validation.CustomValidator;
import com.ericsson.oss.ae.acm.core.validation.statemachine.AppInstanceUseCase;
import com.ericsson.oss.ae.acm.core.validation.statemachine.StateTransitionValidator;
import com.ericsson.oss.ae.acm.enums.AppInstanceAction;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentInstanceRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.persistence.repository.ClientCredentialRepository;
import com.ericsson.oss.ae.acm.presentation.mapper.AppInstancesMapper;
import com.ericsson.oss.ae.utils.validator.HelmAppValidator;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceItems;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

/**
 * Class for Apps service implementation in App LCM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppInstancesServiceImpl implements AppInstancesService {
    @Autowired
    private final AcmFileGenerator acmFileGenerator;
    @Autowired
    private final AcmService acmService;
    @Autowired
    private final AppInstancesMapper appInstancesMapper;
    @Autowired
    private final AppRepository appRepository;
    @Autowired
    private final AppInstancesRepository appInstancesRepository;
    @Autowired
    private final ClientCredentialRepository clientCredentialRepository;
    @Autowired
    private final KeycloakHandler keycloakHandler;
    @Autowired
    private final AppInstanceEventRepository appInstanceEventRepository;
    @Autowired
    private final StateTransitionValidator stateTransition;
    @Autowired
    private final AppComponentInstanceRepository appComponentInstanceRepository;
    @Autowired
    private final AppComponentRepository appComponentRepository;
    @Autowired
    private final AppComponentInstancePropertyBuilder appComponentInstancePropertyBuilder;
    private final DeployActionHandler deployActionHandler;
    private final UpdateActionHandler updateActionHandler;

    @Autowired
    private final UndeployActionHandler undeployActionHandler;

    @Autowired
    private final KeycloakUrlGenerator keycloakUrlGenerator;

    private final UpgradeActionHandler upgradeActionHandler;

    @Value("${acmTimeout:1200000}")
    public Long defaultAcmTimeout;

    @Override
    public AppInstance createAppInstance(final CreateAppInstanceRequest createAppInstanceRequest) throws AppLcmException {
        log.info("Create app instance for app id: {}", createAppInstanceRequest.getAppId());

        // Validate the current state of the app instance
        stateTransition.validateAppInstanceState(CustomValidator.validateUUID(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);

        // Retrieve app details based on the provided app ID
        final App app = getAppDetailsByAppId(CustomValidator.validateUUID(createAppInstanceRequest.getAppId()));

        // Generate Keycloack credentials
        final ClientCredential clientCredential = keycloakHandler.generateKeycloakCredentials(app);

        // Set the Client Url
        clientCredential.setClientUrl(keycloakUrlGenerator.generateBasePath());

        // Initialize variables for AC instance response
        AcInstanceResponse acInstanceResponse = null;

        try {
            // Create a UUID for the AppInstances entity
            final UUID appInstanceId = UUID.randomUUID();

            // Set the App Instances
            final AppInstances appInstancesEntity = appInstancesMapper.generateAppInstanceEntity(appInstanceId, app);

            // Set the Client Credentials
            clientCredential.setAppInstance(appInstancesEntity);
            List<ClientCredential> clientCredentialList = new ArrayList<>();
            clientCredentialList.add(clientCredential);
            appInstancesEntity.setClientCredentials(clientCredentialList);

            // Set App Component Instances
            final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();

            for (AppComponent appComponent : appInstancesEntity.getApp().getAppComponents()) {
                AppComponentInstance appComponentInstance = createAppComponentInstance(appComponent, appInstancesEntity);
                appComponentInstance.setAppInstance(appInstancesEntity);
                appComponentInstancesList.add(appComponentInstance);
            }
            appInstancesEntity.setAppComponentInstances(appComponentInstancesList);

            // Set the componentInstance data properties
            CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstancesEntity);

            // Create property yaml file
            log.info("Generate service property yaml file for app id is: {}", app.getId());
            final String acmServiceProperty = acmFileGenerator.generateAcmInstancePropertiesFile(compositionInstanceData);

            //  Create the AC instance for the app
            log.info("Create AC instance for app id: {} and composition id: {}", app.getId(), app.getCompositionId());
            acInstanceResponse = acmService.commissionAutomationCompositionInstance(acmServiceProperty, app.getCompositionId());

            // Set the app instance properties
            appInstancesEntity.setCompositionInstanceId(acInstanceResponse.getInstanceId());
            appInstancesEntity.setStatus(AppInstanceStatus.UNDEPLOYED);

            // Save the App Instance, Client credentials, Component Instance.
            saveAppInstanceAndRelatedEntitiesData(appComponentInstancesList, appInstancesEntity);
            // Map the saved app instance data
            return buildAppInstance(appInstancesEntity);
        } catch (AppLcmException exception) {
            log.error("Exception while creating app instance", exception);
            keycloakHandler.rollBackCreatedKeycloakCredentials(clientCredential);
            throw exception;
        } catch (RestRequestFailedException exception) {
            log.error("Exception while calling ACM-R", exception);
            keycloakHandler.rollBackCreatedKeycloakCredentials(clientCredential);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.ACM_CREATE_COMPOSITION_INSTANCE_ERROR);
        }
    }

    @Override
    public AppInstanceManagementResponse manageAppInstance(final String instanceID, final AppInstanceManagementRequest appInstanceManagementRequest) {
        UUID instanceId = CustomValidator.validateUUID(instanceID);
        log.info("{} request for an App instance with appInstanceId: {}", appInstanceManagementRequest.getType(), instanceId);
        final AppInstances appInstance = getAppInstanceByInstanceId(instanceId);
        if (appInstanceManagementRequest.getType().name().equals(AppInstanceAction.DEPLOY.name())) {
            log.debug("Validate App instance for DEPLOY operation for instance id: {}", instanceId);
            stateTransition.validateAppInstanceState(instanceId, AppInstanceUseCase.DEPLOY);
            if (appInstanceManagementRequest.getAdditionalData() !=null && appInstanceManagementRequest.getAdditionalData().getComponentInstances() !=null) {
                validateAppInstanceRequestBody(appInstance, appInstanceManagementRequest.getAdditionalData().getComponentInstances());
                validateNamespaceIfExist(appInstanceManagementRequest);
                validateTimeout(appInstanceManagementRequest.getAdditionalData().getComponentInstances());
                log.debug("Update app instance for the given component instance properties for instance id: {}", appInstance.getId());
                updateActionHandler.updateAppInstance(appInstance, appInstanceManagementRequest.getAdditionalData().getComponentInstances());
            }
            deployActionHandler.deployAppInstance(appInstance, appInstanceManagementRequest);
            log.debug("Generate App instance response for DEPLOY operation for instance id: {}", instanceId);
        } else if (appInstanceManagementRequest.getType().name().equals(AppInstanceAction.UNDEPLOY.name())) {
            log.debug("Validate App instance for UNDEPLOY operation for instance id: {}", instanceId);
            stateTransition.validateAppInstanceState(instanceId, AppInstanceUseCase.UNDEPLOY);
            undeployActionHandler.undeployAppInstance(appInstance);
            log.debug("Generate App instance response for UNDEPLOY operation for instance id: {}", instanceId);
        } else if (appInstanceManagementRequest.getType().name().equals(AppInstanceAction.UPGRADE.name())) {
            log.debug("Validate App instance for UPGRADE operation for instance id: {}", instanceId);
            stateTransition.validateAppInstanceState(instanceId, AppInstanceUseCase.UPGRADE);
            if (appInstanceManagementRequest.getAdditionalData() !=null && appInstanceManagementRequest.getAdditionalData().getComponentInstances() !=null) {
                validateAppInstanceRequestBody(appInstance, appInstanceManagementRequest.getAdditionalData().getComponentInstances());
                validateTimeout(appInstanceManagementRequest.getAdditionalData().getComponentInstances());
            }
            final UUID targetAppId = CustomValidator.validateUUID(appInstanceManagementRequest.getTargetAppId());
            log.info("Target App Id is: {}", targetAppId);
            final App targetAppDetails = getAppDetailsByAppId(targetAppId);
            validateTargetApp(targetAppDetails);
            upgradeActionHandler.executeAppInstanceUpgrade(appInstance, targetAppDetails, appInstanceManagementRequest);
            log.debug("Generate App instance response for UPGRADE operation for instance id: {}", instanceId);
        }
        return appInstancesMapper.toAppInstanceManagementResponse(appInstanceManagementRequest, appInstance);
    }

    private void validateTargetApp(App appDetails){
        if(appDetails.getMode().equals(AppMode.DISABLED)){
            throw new AppLcmException(HttpStatus.BAD_REQUEST, BAD_REQUEST_TARGET_APP_INVALID_STATE);
        }
    }

    @Override
    public AppInstanceOperationResponse deleteAppInstance(final String instanceID) {
        UUID instanceId = CustomValidator.validateUUID(instanceID);
        log.debug("Validate instance status for Delete operation. Instance id - {}", instanceId);
        stateTransition.validateAppInstanceState(instanceId, AppInstanceUseCase.DELETE);
        final AppInstances appInstance = getAppInstanceByInstanceId(instanceId);
        final App app = appInstance.getApp();
        final UUID acmCompositionId = app.getCompositionId();
        final UUID acmInstanceId = appInstance.getCompositionInstanceId();
        try {
            log.debug("Delete automation composition instance");
            deleteAutomationCompositionInstance(acmCompositionId, acmInstanceId);
            appInstance.setStatus(AppInstanceStatus.DELETING);
            appInstancesRepository.save(appInstance);
            return appInstancesMapper.toAppInstanceOperationResponse(appInstance);
        } catch (final AppLcmException appLcmException) {
            log.error("deleteAppInstance() Exception while deleting app instance {}. Setting App Instance Status as DELETE_ERROR.", instanceId, appLcmException);
            updateAppInstanceStatus(appInstance, AppInstanceStatus.DELETE_ERROR, appLcmException);
            throw appLcmException;
        }
    }

    @Override
    public AppInstanceUpdateResponse updateAppInstance(final String instanceId, final UpdateAppInstanceRequest updateAppInstanceRequest) throws AppLcmException{
        UUID appInstanceId = CustomValidator.validateUUID(instanceId);
        log.info("Updating an app instance for appInstanceId: {}", appInstanceId);
        stateTransition.validateAppInstanceState(appInstanceId, AppInstanceUseCase.UPDATE);
        AppInstances appInstance = getAppInstanceByInstanceId(appInstanceId);
        validateAppInstanceRequestBody(appInstance, updateAppInstanceRequest.getComponentInstances());
        validateTimeout(updateAppInstanceRequest.getComponentInstances());
        try{
            updateActionHandler.updateAppInstance(appInstance, updateAppInstanceRequest.getComponentInstances());
            if (acmService.getAutomationCompositionInstance(appInstance.getApp().getCompositionId(),appInstance.getCompositionInstanceId()).getDeployState() != DeployState.UNDEPLOYED) {
                appInstance.setStatus(AppInstanceStatus.UPDATING);
                appInstancesRepository.save(appInstance);
            }
        } catch (AppLcmException appLcmException) {
            log.error("updateAppInstance() Exception while updating app instance {}. Setting App Instance Status as UPDATE_ERROR", appInstanceId);
            if (acmService.getAutomationCompositionInstance(appInstance.getApp().getCompositionId(),appInstance.getCompositionInstanceId()).getDeployState() != DeployState.UNDEPLOYED) {
                updateAppInstanceStatus(appInstance, AppInstanceStatus.UPDATE_ERROR, appLcmException);
            }
            throw appLcmException;
        }
        AppInstance appInstanceForResponse = buildAppInstance(appInstance);
        return appInstancesMapper.toAppInstanceUpdateResponse(appInstanceForResponse);
    }

    @Override
    public AppInstanceItems getAppInstances(String appID) {
        final List<AppInstance> appInstances = new ArrayList<>();
        if (isQueryByAppIdRequested(appID)) {
            log.info("Getting app instances with query by appId: {}", appID);
            UUID appId = CustomValidator.validateUUID(appID);
            appInstances.addAll(getAppInstancesByAppId(appId));
        } else {
            log.info("Getting all app instances");
            appInstances.addAll(getAllAppInstances());
        }
        // Default sorted by time, the newest App instance first
        appInstances.sort(Comparator.comparing(AppInstance::getCreatedAt).reversed());
        AppInstanceItems appInstanceItems = new AppInstanceItems();
        appInstanceItems.setItems(appInstances);
        return appInstanceItems;
    }

    @Override
    public AppInstance getAppInstanceById(final String appInstanceID) {
        UUID appInstanceId = CustomValidator.validateUUID(appInstanceID);
        log.info("Getting app instance for appInstanceId: {}", appInstanceId);
        final Optional<AppInstances> appInstanceEntity = appInstancesRepository.findById(appInstanceId);
        if (appInstanceEntity.isPresent()) {
            return buildAppInstance(appInstanceEntity.get());
        } else {
            log.error("AppInstance for given ID: {}, was not found in the DB", appInstanceId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_INSTANCE_ENTITY_NOT_FOUND);
        }
    }

    private void validateAppInstanceRequestBody(final AppInstances appInstance, final List<ComponentInstances> componentInstances){
        if (appInstance.getApp()!=null && appInstance.getApp().getAppComponents()!=null){
            if (componentInstances!=null){
                componentInstances.stream().forEach(componentInstance ->
                    appInstance.getApp().getAppComponents().stream().filter(component -> componentInstance.getName().equalsIgnoreCase(component.getName()))
                            .findFirst().orElseThrow(() -> new AppLcmException(HttpStatus.BAD_REQUEST, APP_INSTANCE_COMPONENT_MISMATCH_ERROR))
                );
            } else {
                throw new AppLcmException(HttpStatus.BAD_REQUEST, APP_INSTANCE_COMPONENT_INSTANCE_REQUEST_ERROR);
            }
        }
    }

    /**
     * Builds the ComponentInstance model object, populating with the properties stored in the LCM DATABASE
     * and also adding those properties sourced from the ACM-R service.
     *
     * @param appComponentInstance entity with all properties stored in the lcm db
     * @param compositionElement contains the ACM-R properties
     * @return ComponentInstance with all the LCM and ACM-R properties added
     */
    private ComponentInstances buildComponentInstance(final AppComponentInstance appComponentInstance, final AutomationCompositionElement compositionElement) {

        final ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setDeployState(compositionElement.getDeployState().name());
        componentInstance.setMessage(compositionElement.getMessage());

        // Get the app component type for the component instance
        final UUID appComponentId = appComponentInstance.getAppComponent().getId();
        final AppComponent appComponent = getSavedAppComponent(appComponentId);
        componentInstance.setName(appComponent.getName());
        componentInstance.setVersion(appComponent.getVersion());
        componentInstance.setType(appComponent.getType());

        // Get just the properties that we want to show to the App Mgr user
        final Map<String, Object> properties = appComponentInstancePropertyBuilder
                .buildInstanceProperties(appComponent.getType(), compositionElement.getProperties());
        if (properties != null && !properties.isEmpty()) {
            componentInstance.setProperties(properties);
        }
        return componentInstance;
    }

    /**
     * Builds the ComponentInstance model object, just populating with the properties stored in the LCM DATABASE
     * and ignoring those properties sourced from the ACM-R service.
     * For use when, for example, the AppInstance is in state DELETING and therefore the ACM element properties
     * may already have been deleted on that side.
     *
     * @param appComponentInstance entity with properties stored in the lcm db
     * @return ComponentInstance with just the LCM properties added
     */
    private ComponentInstances buildComponentInstanceWithoutAcmProperties(final AppComponentInstance appComponentInstance) {
        final ComponentInstances componentInstance = new ComponentInstances();

        // Get the app component type for the component instance
        final UUID appComponentId = appComponentInstance.getAppComponent().getId();
        final AppComponent appComponent = getSavedAppComponent(appComponentId);
        componentInstance.setName(appComponent.getName());
        componentInstance.setVersion(appComponent.getVersion());
        componentInstance.setType(appComponent.getType());
        return componentInstance;
    }

    private AppComponent getSavedAppComponent(final UUID appComponentId) {
        final Optional<AppComponent> appComponent = appComponentRepository.findById(appComponentId);
        if (appComponent.isEmpty()) {
            log.error("AppComponent for given appInstanceId:, was not found in the DB");
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_COMPONENT_ENTITY_NOT_FOUND);
        }
        return appComponent.get();
    }

    private List<AppInstance> getAllAppInstances() {
        final List<AppInstance> appInstances = new ArrayList<>();
        final List<AppInstances> appInstanceEntities = appInstancesRepository.findAll();
        for (final AppInstances appInstanceEntity : appInstanceEntities) {
            final AppInstance appInstance = buildAppInstance(appInstanceEntity);
            appInstances.add(appInstance);
        }
        return appInstances;
    }

    private AppComponentInstance createAppComponentInstance(AppComponent appComponent, AppInstances appInstances) {
        AppComponentInstance appComponentInstance = new AppComponentInstance();
        appComponentInstance.setAppId(appInstances.getApp().getId());
        appComponentInstance.setAppComponent(appComponent);
        appComponentInstance.setCompositionElementInstanceId(UUID.randomUUID());
        appComponentInstance.setAppInstance(appInstances);
        return appComponentInstance;
    }

    private void saveAppInstanceAndRelatedEntitiesData(List<AppComponentInstance> appComponentInstanceList, AppInstances appInstances) {
        appInstancesRepository.save(appInstances);
        for (AppComponentInstance appComponentInstance : appComponentInstanceList) {
            appComponentInstanceRepository.save(appComponentInstance);
        }
    }

    private AppInstance buildAppInstance(final AppInstances appInstanceEntity) {
        final UUID appInstanceId = appInstanceEntity.getId();
        final ClientCredential clientCredential = getClientCredential(appInstanceEntity);
        // Get the existing app component instances from the DB
        final List<AppComponentInstance> appComponentInstances = appComponentInstanceRepository.findByAppInstanceId(appInstanceId).stream()
                .filter(componentInstance -> componentInstance.getAppId().equals(appInstanceEntity.getApp().getId()))
                .collect(Collectors.toList());
        // Now go to ACM and get the properties for the associated composition instances
        final Map<UUID, AutomationCompositionElement> automationCompositionElements = getAutomationCompositionElementsFromAcmr(appInstanceEntity);
        // Build the App Component Instances using data from each corresponding composition element instance
        final List<ComponentInstances> componentInstances = buildAllAppComponentInstances(appComponentInstances, automationCompositionElements);

        log.info("Returning app instance for appInstanceId: {}", appInstanceId);
        return appInstancesMapper.toAppInstance(appInstanceEntity, componentInstances, clientCredential);
    }

    private ClientCredential getClientCredential(final AppInstances appInstanceEntity) {
        final List<ClientCredential> clientCredentials = appInstanceEntity.getClientCredentials();
        if (clientCredentials == null || clientCredentials.isEmpty()) {
            log.warn("Client credential for AppInstance ID: {}, was not found in the Database", appInstanceEntity.getId());
            return null;
        }
        return clientCredentials.get(0);
    }

    private boolean isQueryByAppIdRequested(final String appId) {
        // The appId is an optional query parameter, so check if it was provided.
        return appId != null;
    }

    /**
     * Given a list of AppComponent DB instances and the Map of Composition Element instances read from ACM,
     * for each component, get the composition element associated with the component instance, read the
     * relevant data from the element and then build it with the lcm component data into an App Mgr model
     * response object.
     *
     * @param appComponentInstances
     * @param automationCompositionElements
     * @return a list of component instance model objects that can be returned to the App Mgr user.
     */
    private List<ComponentInstances> buildAllAppComponentInstances(final List<AppComponentInstance> appComponentInstances,
                                                                  final Map<UUID, AutomationCompositionElement> automationCompositionElements) {
        final List<ComponentInstances> componentInstances = new ArrayList<>();
        for (final AppComponentInstance appComponentInstance : appComponentInstances) {
            final UUID compositionElementId = appComponentInstance.getCompositionElementInstanceId();
            final AutomationCompositionElement compositionElement = automationCompositionElements.get(compositionElementId);
            // build app mgr component instance model data from automation composition element data
            if (compositionElement != null) {
                final ComponentInstances componentInstance = buildComponentInstance(appComponentInstance, compositionElement);
                componentInstances.add(componentInstance);
            } else {
                if(appComponentInstance.getAppInstance().getStatus() == AppInstanceStatus.DELETING) {
                    log.warn("AppInstance status is DELETING and Composition Element Instance with ID {}, was not found in Composition instance data returned from ACM-R.",
                        compositionElementId);
                    final ComponentInstances componentInstance = buildComponentInstanceWithoutAcmProperties(appComponentInstance);
                    componentInstances.add(componentInstance);
                } else if (appComponentInstance.getAppInstance().getStatus() == AppInstanceStatus.UPGRADING) {
                    log.debug("AppInstance status is Upgrading and Composition Element Instance with ID {} is left out from the Composition instance data until it is successfully upgraded.", compositionElementId);
                } else {
                    log.error("Composition Element Instance with ID {} for AppComponent Instance ID: {}, was not found in Composition instance data returned from ACM-R",
                        compositionElementId, appComponentInstance.getId());
                    throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.COMPOSITION_ELEMENT_INSTANCE_NOT_EXISTING);
                }

            }
        }
        return componentInstances;
    }

    private Map<UUID, AutomationCompositionElement> getAutomationCompositionElementsFromAcmr(final AppInstances appInstanceEntity) {
        final UUID compositionId = appInstanceEntity.getApp().getCompositionId();
        final UUID compositionInstanceId = appInstanceEntity.getCompositionInstanceId();
        try {
            final AutomationCompositionInstance compositionInstance = acmService.getAutomationCompositionInstance(compositionId, compositionInstanceId);
            return compositionInstance.getElements();
        } catch (RestRequestFailedException ex) {
            log.error("Automation Composition instance for ID {} and compositionId {} was not found in ACM-R", compositionInstanceId, compositionId, ex);
            return new HashMap<>();
        }
    }

    private AppInstances getAppInstanceByInstanceId(final UUID instanceId) {
        final Optional<AppInstances> appInstance = appInstancesRepository.findById(instanceId);
        if (appInstance.isEmpty()) {
            log.error("App Instance Details for given Id: {}, was not found in the DB", instanceId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_INSTANCE_ENTITY_NOT_FOUND);
        }
        return appInstance.get();
    }

    private void deleteAutomationCompositionInstance(final UUID acmCompositionId, final UUID acmInstanceId) {
        try {
            // delete acm-composition instance
            acmService.deleteAutomationCompositionInstance(acmCompositionId, acmInstanceId);
        } catch (final RestRequestFailedException ex) {
            if (ex.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                log.info("deleteAutomationCompositionInstance() AC Instance not found. Setting App Instance Status as DELETING...");
            } else {
                log.error(
                        "deleteAutomationCompositionInstance() Request failure when calling ACM-R to Delete the AC Instance with acmInstanceId {}. Reason: {}",
                        acmInstanceId,
                        ex.getErrorDetails(), ex);
                throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DELETE_AC_INSTANCE_ERROR);
            }
        }
    }

    private void updateAppInstanceStatus(final AppInstances appInstances, final AppInstanceStatus appInstanceStatus,
                                         final AppLcmException lcmException) {
        addErrorAppInstanceEvent(appInstances, lcmException);
        appInstances.setStatus(appInstanceStatus);
        appInstancesRepository.save(appInstances);
    }

    private void addErrorAppInstanceEvent(final AppInstances appInstances, final AppLcmException lcmException) {
        final AppLcmError lcmError = lcmException.getAppLcmError();
        log.info("addErrorAppInstanceEvent() {}. Reason: {}", lcmError.getErrorTitle(), lcmError.getErrorMessage());
        final AppInstanceEvent appInstanceEvent = AppInstanceEvent.builder().appInstance(appInstances).type(EventType.ERROR)
                .title(lcmError.getErrorTitle()).detail(lcmError.getErrorMessage())
                .build();
        appInstanceEventRepository.save(appInstanceEvent);
    }

    private List<AppInstance> getAppInstancesByAppId(final UUID appId) {
        final List<AppInstance> appInstances = new ArrayList<>();
        final Optional<App> app = appRepository.findById(appId);
        if (app.isPresent()) {
            log.debug("App detail for given ID: {}", app.get());
            final List<AppInstances> appInstanceEntities = app.get().getAppInstances();
            for (final AppInstances appInstanceEntity : appInstanceEntities) {
                final AppInstance appInstance = getAppInstanceById(appInstanceEntity.getId().toString());
                // This statement can delete once rAppId configured properly
                appInstance.setAppId(app.get().getId().toString());
                appInstances.add(appInstance);
            }
        } else {
            log.error("App for given ID: {}, was not found in the DB", appId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_ERROR);
        }
        return appInstances;
    }

    private App getAppDetailsByAppId(final UUID appId) {
        final Optional<App> app = appRepository.findById(appId);
        if (app.isEmpty()) {
            log.error("App Details for given Id: {}, was not found", appId);
            throw new AppLcmException(HttpStatus.BAD_REQUEST, APP_NOT_FOUND_ERROR);
        }
        return app.get();
    }

    private void validateNamespaceIfExist(AppInstanceManagementRequest request) {
        request.getAdditionalData().getComponentInstances()
                .stream()
                .filter(Objects::nonNull)
                .map(ComponentInstances::getProperties)
                .filter(Objects::nonNull)
                .filter(Map.class::isInstance)
                .map(entry -> (Map<String, Object>) entry)
                .filter(map -> map.containsKey(AppLcmConstants.NAMESPACE))
                .map(map -> map.get(AppLcmConstants.NAMESPACE))
                .forEach(AppInstancesServiceImpl::verifyNamespace);
    }

    private static void verifyNamespace(Object item) {
        if (item instanceof String) {
            HelmAppValidator.isNamespaceValid(String.valueOf(item));
        }
    }

    public void validateTimeout(List<ComponentInstances> componentInstances) {
        componentInstances.stream()
            .filter(Objects::nonNull)
            .map(ComponentInstances::getProperties)
            .filter(Objects::nonNull)
            .filter(Map.class::isInstance)
            .map(entry -> (Map<String, Object>) entry)
            .filter(map -> map.containsKey(AppLcmConstants.TIMEOUT))
            .map(map -> map.get(AppLcmConstants.TIMEOUT))
            .forEach(timeout -> {
                if (timeout instanceof Integer) {
                    int timeoutInMinutes = (int) timeout;
                    int timeoutInMilliseconds = timeoutInMinutes * 60 * 1000;
                    log.debug("User-defined timeout in milliseconds: {}", timeoutInMilliseconds);
                    log.debug("Default ACM timeout in milliseconds: {}", defaultAcmTimeout);
                    if (timeoutInMilliseconds > defaultAcmTimeout) {
                        long defaultTimeoutMinutes = defaultAcmTimeout / 60000;
                        throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.APP_COMPONENT_INSTANCE_TIMEOUT_REQUEST_ERROR, new String[]{String.valueOf(defaultTimeoutMinutes)});
                    }
                } else {
                    throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.APP_COMPONENT_INSTANCE_UNSUPPORTED_TIMEOUT_TYPE_ERROR_MESSAGE);
                }
            });
    }
}