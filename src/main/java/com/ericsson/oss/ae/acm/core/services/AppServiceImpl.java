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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.LCM_APP_MODE_VALIDATION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.LCM_STATUS_VALIDATION_ERROR;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.minio.ObjectStoreService;
import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.core.services.handlers.DeInitializeAppHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.InitializeAction;
import com.ericsson.oss.ae.acm.core.services.handlers.InitializeAppHandler;
import com.ericsson.oss.ae.acm.core.validation.CustomValidator;
import com.ericsson.oss.ae.acm.core.validation.creation.CreateAppRequestValidator;
import com.ericsson.oss.ae.acm.core.validation.statemachine.AppUseCase;
import com.ericsson.oss.ae.acm.core.validation.statemachine.StateTransitionValidator;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.Artifact;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.persistence.specification.AppDetailsSearchSpecification;
import com.ericsson.oss.ae.acm.presentation.filter.GetAppsFilter;
import com.ericsson.oss.ae.acm.presentation.mapper.AppDetailsMapper;
import com.ericsson.oss.ae.acm.presentation.mapper.LcmUrlGenerator;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppInitializeOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInitializeOperationResponseApp;
import com.ericsson.oss.ae.v3.api.model.AppItems;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponseApp;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.Component;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.EnableDisableAppRequest;
import com.ericsson.oss.ae.v3.api.model.InitializeActionRequest;

/**
 * Class for App service implementation in App LCM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppServiceImpl implements AppService {

    @Autowired
    private final AcmFileGenerator acmFileGenerator;

    @Autowired
    private final AppRepository appRepository;

    @Autowired
    private final AppEventRepository appEventRepository;

    @Autowired
    private final AppDetailsMapper appDetailsMapper;

    @Autowired
    private final AcmService acmService;

    @Autowired
    private final ObjectStoreService objectStoreService;

    @Autowired
    private final StateTransitionValidator stateTransition;

    @Autowired InitializeAppHandler initializeAppHandler;

    @Autowired DeInitializeAppHandler deInitializeAppHandler;

    @Autowired
    LcmUrlGenerator lcmUrlGenerator;

    @Override
    public AppDetails createApp(final CreateAppRequest createAppRequest) throws RestRequestFailedException, AppLcmException {
        //validate create app request and upper case the app component type
        CreateAppRequest validatedCreateAppRequest = validateCreateAppRequest(createAppRequest);
        log.info("processing create app request for {} version {} provider {}",
                createAppRequest.getName(),
                createAppRequest.getVersion(),
                createAppRequest.getProvider());
        App app = appDetailsMapper.toApp(validatedCreateAppRequest);

        final String acmTypeTemplate = acmFileGenerator.generateToscaServiceTemplate(app);
        log.info("Generated AcmServiceTemplate: acmTypeTemplate = " + acmTypeTemplate);

        try {
            final AcCommissionResponse acmComposition = acmService.commissionAutomationCompositionType(acmTypeTemplate);
            app = saveAppDetails(acmComposition, app);
            return appDetailsMapper.createAppResponseFromApp(app);
        } catch (RestRequestFailedException ex) {
            log.error("Exception while calling ACM-R", ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.ACM_CREATE_COMPOSITION_ERROR);
        }
    }

    private App saveAppDetails(final AcCommissionResponse acmComposition, final App app) {
        log.info("Generate app entity with composition details");
        app.setCompositionId(acmComposition.getCompositionId());
        log.info("Save app entity details to LCM Db");
        return appRepository.save(app);
    }

    @Override
    public AppItems getApps(final GetAppsFilter getAppsFilter) {
        log.info("getApps() returning AppItems with filter: {}", getAppsFilter);

        AppItems appItems = new AppItems();
        appItems.setItems(appDetailsMapper.toAppDetailsList(this.queryApps(getAppsFilter)));
        appItems.getItems().sort(Comparator.comparing(AppDetails::getCreatedAt).reversed());
        return appItems;
    }

    public AppDetails getAppById(final String appID) {
        UUID appId = CustomValidator.validateUUID(appID);
        log.info("getAppById() returning app details for appId: {}", appId);
        final Optional<App> appEntity = appRepository.findById(appId);

        if (appEntity.isPresent()) {
            return appDetailsMapper.fromApp(appEntity.get());
        } else {
            log.error("App for given ID: {}, was not found in the DB", appId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_NOT_FOUND_ERROR);
        }
    }

    @Override
    public AppInitializeOperationResponse executeInitializeAction(final String appID, final InitializeActionRequest initializeActionRequest) {
        UUID appId = CustomValidator.validateUUID(appID);
        log.info("Executing 'Initialize App' request with action type {} for appId {}", initializeActionRequest.getAction(), appId.toString());
        final InitializeAction requestedAction = InitializeAction.getSpecificActionType(initializeActionRequest.getAction());
        final App app = getAppFromDB(appId);
        if (requestedAction == InitializeAction.INITIALIZE) {
            log.debug("Validate app mode and status for initialize operation");
            stateTransition.validateAppState(appId, AppUseCase.INITIALIZE);
            initializeAppHandler.initializeApp(app);
            log.info("Successfully handled Initialize app action for appId {} and name {}", appId.toString(), app.getName());
            AppInitializeOperationResponseApp appInitializeOperationResponseApp = new AppInitializeOperationResponseApp();
            appInitializeOperationResponseApp
                    .status(app.getStatus())
                    .id(app.getId().toString())
                    .href(lcmUrlGenerator.getAppsUrlById(appId));
            return new AppInitializeOperationResponse().app(appInitializeOperationResponseApp);

        } else {
            log.debug("Validate app mode and status for de-initialize operation");
            stateTransition.validateAppState(appId, AppUseCase.DEINITIALIZE);
            deInitializeAppHandler.deInitializeApp(app);
            log.info("Successfully handled De-Initialize app action for appId: {} and name: {}", appId.toString(), app.getName());
            AppInitializeOperationResponseApp appInitializeOperationResponseApp = new AppInitializeOperationResponseApp();
            appInitializeOperationResponseApp
                    .status(app.getStatus())
                    .id(app.getId().toString())
                    .href(lcmUrlGenerator.getAppsUrlById(appId));
            return new AppInitializeOperationResponse().app(appInitializeOperationResponseApp);
        }
    }

    @Override
    public AppOperationResponse enableDisableApp(final String appID, final EnableDisableAppRequest enableDisableAppRequest) {
        UUID appId = CustomValidator.validateUUID(appID);
        final App app = getAppFromDB(appId);
        if (enableDisableAppRequest.getMode().equals(AppMode.ENABLED)) {
            log.info("Executing 'Enable App' request with app mode: {}", enableDisableAppRequest.getMode());

            stateTransition.validateAppState(appId, AppUseCase.ENABLE);
            app.setMode(AppMode.ENABLED);
            appRepository.save(app);
            log.debug("The app entity with Id: {} is enabled", app.getId());
        } else {
            log.info("Executing 'Disable App' request with app mode: {}", enableDisableAppRequest.getMode());

            stateTransition.validateAppState(appId, AppUseCase.DISABLE);
            app.setMode(AppMode.DISABLED);
            appRepository.save(app);
            log.debug("The app entity with Id: {} is disabled", app.getId());
        }

        AppOperationResponseApp appOperationResponseApp = new AppOperationResponseApp();
        appOperationResponseApp
                .id(app.getId().toString())
                .href(lcmUrlGenerator.getAppsUrlById(appId));

        return new AppOperationResponse()
                .mode(app.getMode())
                .app(appOperationResponseApp);
    }

    @Override
    public void deleteAppById(final String appID) {
        UUID appId = CustomValidator.validateUUID(appID);
        log.info("Deleting an App by appId - {} ", appId);

        // check App mode is DISABLED and status is either CREATED, DEINITIALIZED or DELETE_ERROR
        stateTransition.validateAppState(appId, AppUseCase.DELETE);

        //get App details
        final App app = getAppFromDB(appId);

        // check no app instances exists for this appId
        checkNoAppInstancesExist(app);

        // get acm-composition by compositionId in App
        final UUID acmCompositionId = app.getCompositionId();

        try {
            deleteAutomationCompositionType(acmCompositionId, appId);

            // delete all artifacts and bucket in object-store
            if (!app.getAppComponents().isEmpty()) {
                this.removeArtifactsFromObjectStore(app.getAppComponents());
            }

            // delete App from lcm db
            appRepository.delete(app);
        } catch (AppLcmException lcmException) {
            log.info("deleteAppById() Error deleting App {}. Setting it as DELETE_ERROR.", appId);
            updateAppStatus(app, AppStatus.DELETE_ERROR, lcmException);
            throw lcmException;
        }
    }

    private void deleteAutomationCompositionType(final UUID acmCompositionId, final UUID appId) {
        try {
            // delete acm-composition by compositionId (decommission)
            acmService.deleteAutomationCompositionType(acmCompositionId);
        } catch (final RestRequestFailedException ex) {
            if (ex.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                log.info("deleteAppById() AC Definition not found. Proceeding to delete App...");
            } else {
                log.error("Request failure when calling ACM-R to Delete the AC Definition with appId {}. Reason: {}", appId,
                        ex.getErrorDetails(), ex);
                throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.SERVER_ERROR_ACM_COMPOSITION_DEF_DELETE);
            }
        }
    }

    private List<App> queryApps(final GetAppsFilter getAppsFilter) {
        log.debug("queryApps() Creating searchSpecification for filter: {}", getAppsFilter);
        final Specification<App> searchSpecification = this.createSearchSpec(getAppsFilter);

        return appRepository.findAll(searchSpecification);
    }

    private CreateAppRequest validateCreateAppRequest(final CreateAppRequest createAppRequest) throws AppLcmException {
        log.debug("validating create app request");
        //validate create app request
        new CreateAppRequestValidator().validate(createAppRequest);
        //upper case app component type
        return upperCaseAppComponentType(createAppRequest);
    }

    private Specification<App> createSearchSpec(final GetAppsFilter getAppsFilter) {
        Specification<App> spec = Specification.where(null);

        if (getAppsFilter.name() != null) {
            spec = spec.and(parse("name", getAppsFilter.name()));
        }

        if (getAppsFilter.version() != null) {
            spec = spec.and(parse("version", getAppsFilter.version()));
        }

        if (getAppsFilter.mode() != null) {
            isValidMode(getAppsFilter.mode());
            spec = spec.and(parse("mode", getAppsFilter.mode()));
        }

        if (getAppsFilter.status() != null) {
            isValidStatus(getAppsFilter.status());
            spec = spec.and(parse("status", getAppsFilter.status()));
        }

        if (getAppsFilter.type() != null) {
            spec = spec.and(parse("type", getAppsFilter.type()));
        }

        return spec;
    }

    private void isValidStatus(final String value) {
        log.debug("checking if value is a valid Status option - {}", value);
        List<String> statuses = Arrays.stream(AppStatus.class.getEnumConstants()).map(Enum::name).toList();
        boolean valid = Arrays.stream(value.split(String.valueOf(AppLcmConstants.COMMA))).map(String::toUpperCase).allMatch(statuses::contains);

        if (!valid) {
            throwPathParamValidationError(value, statuses, LCM_STATUS_VALIDATION_ERROR);
        }
    }

    private void isValidMode(final String value) {
        log.debug("checking if value is a valid Mode option - {}", value);
        List<String> modes = Arrays.stream(AppMode.class.getEnumConstants()).map(Enum::name).toList();
        boolean valid = Arrays.stream(value.split(String.valueOf(AppLcmConstants.COMMA))).map(String::toUpperCase).allMatch(modes::contains);

        if (!valid) {
            throwPathParamValidationError(value, modes, LCM_APP_MODE_VALIDATION_ERROR);
        }
    }

    private void throwPathParamValidationError(String value, List<String> validValues, AppLcmError error) {
        String invalidMode = String.valueOf(
            Arrays.stream(value.split(String.valueOf(AppLcmConstants.COMMA))).filter(s -> !validValues.contains(s)).findFirst().orElse(null));
        throw new AppLcmException(HttpStatus.BAD_REQUEST, error,
            new String[]{invalidMode, "GET Apps", String.join(" or ", validValues) });
    }

    private Specification<App> parse(String pathParamKey, String pathParamValue) {
        Specification<App> searchSpec = null;

        for (String value : pathParamValue.split(String.valueOf(AppLcmConstants.COMMA))) {
            if (searchSpec == null) {
                searchSpec = new AppDetailsSearchSpecification(pathParamKey, value);
            } else {
                searchSpec = searchSpec.or(new AppDetailsSearchSpecification(pathParamKey, value));
            }
        }

        return searchSpec;
    }

    private App getAppFromDB(final UUID appId) {
        final Optional<App> app = appRepository.findById(appId);
        if (app.isPresent()) {
            return app.get();
        } else {
            log.error("App Details for given ID: {}, was not found in the DB", appId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_NOT_FOUND_ERROR);
        }
    }

    private void checkNoAppInstancesExist(final App app) {
        // get app instance details
        final List<AppInstances> appInstances = app.getAppInstances();

        if (appInstances != null && !appInstances.isEmpty()) {
            log.info("deleteAppById() Unable to delete App. App Instances exist.");
            throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.BAD_REQUEST_ERROR_DELETE_APP_INSTANCES_EXIST);
        }
    }

    private boolean isAppInErrorState(final AppStatus status) {
        return status == AppStatus.DELETE_ERROR;
    }

    private void removeArtifactsFromObjectStore(@NotNull final List<AppComponent> appComponents) {
        final AppComponent appComponent = appComponents.iterator().next();
        final List<Artifact> artifacts = appComponent.getArtifacts();

        if (!artifacts.isEmpty()) {
            final Artifact artifact = artifacts.iterator().next();
            final String artifactLocation = artifact.getLocation();

            if (StringUtils.isNoneBlank(artifactLocation)) {
                final String bucketName = artifactLocation.split("/")[0];
                final String jobId = artifactLocation.split("/")[1];

                objectStoreService.deleteAllObjectsInBucket(bucketName, UUID.fromString(jobId));

            }
        }
    }

    private void updateAppStatus(final App app, final AppStatus status, final AppLcmException lcmException) {
        if (isAppInErrorState(status)) {
            addErrorAppEvent(app, lcmException);
        }

        app.setStatus(status);
        appRepository.save(app);
    }

    private void addErrorAppEvent(final App app, final AppLcmException lcmException) {
        final AppLcmError lcmError = lcmException.getAppLcmError();
        log.info("addAppErrorEvent() {}. Reason: {}", lcmError.getErrorTitle(), lcmError.getErrorMessage());
        final AppEvent appEvent = AppEvent.builder().app(app).type(EventType.ERROR).title(lcmError.getErrorTitle()).detail(lcmError.getErrorMessage())
                .build();
        appEventRepository.save(appEvent);
    }

    private CreateAppRequest upperCaseAppComponentType(CreateAppRequest createAppRequest){
        for (Component component : createAppRequest.getComponents()) {
            component.setType(component.getType().toUpperCase(Locale.ROOT));
        }
        return createAppRequest;
    }
}
