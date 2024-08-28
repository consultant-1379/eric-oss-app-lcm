/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.presentation.services.appinstance;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.BDR;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_URL;
import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_LCM_DELETE_PATH;
import static com.ericsson.oss.ae.constants.AppLcmConstants.FAILED_TO_DELETE_MESSAGE;
import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_DELETING_ERROR;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_METHOD_DELETION;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_METHOD_INSTANTIATION;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_MODE_DISABLED;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KAFKA;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_WORKLOAD_INSTANCE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_INSTANCE_NOT_FOUND;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_LCM_ROLE_NOT_FOUND_IN_KEYCLOAK;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ONBOARDING_APP_NOT_FOUND;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_DELETE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_ENABLED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_UPDATE_DELETING_FAILED;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_ORCHESTRATOR_DELETE_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.HELM_ORCHESTRATOR_OPERATION_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND;
import static com.ericsson.oss.ae.utils.json.JsonUtils.parseMapToJsonString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.utils.JsonParser;
import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstanceListRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.api.model.MultiDeleteFailureDetails;
import com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClient;
import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.mapper.AppMapper;
import com.ericsson.oss.ae.clients.apponboarding.model.App;
import com.ericsson.oss.ae.clients.apponboarding.model.Artifact;
import com.ericsson.oss.ae.clients.apponboarding.model.ArtifactType;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.InstantiateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.WorkloadInstanceDtoMapper;
import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.clients.keycloak.dto.CredentialDto;
import com.ericsson.oss.ae.clients.keycloak.dto.RoleDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.clients.keycloak.model.Client;
import com.ericsson.oss.ae.clients.keycloak.model.ProtocolMapperEntry;
import com.ericsson.oss.ae.clients.keycloak.utility.RoleUtility;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.CredentialEvent;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmServiceException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnBoardingDeleteException;
import com.ericsson.oss.ae.presentation.exceptions.AppOnBoardingModeException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToDeleteException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToTerminateException;
import com.ericsson.oss.ae.presentation.exceptions.FailureToUpdateException;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.presentation.exceptions.KeycloakException;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.presentation.exceptions.UnableToRetrieveArtifactException;
import com.ericsson.oss.ae.presentation.mappers.AppInstanceMapper;
import com.ericsson.oss.ae.presentation.services.AppInstanceJpaSpecification;
import com.ericsson.oss.ae.presentation.services.artifactinstance.ArtifactInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.CredentialEventRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.json.JsonUtils;
import com.ericsson.oss.ae.utils.validator.HelmAppValidator;
import com.ericsson.oss.ae.utils.validator.InstanceValidator;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;

/** Implementation for App Instance Service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppInstanceServiceImpl implements AppInstanceService {

    private final AppInstanceRepository appInstanceRepository;
    private final AppInstanceMapper appInstanceMapper;
    private final AppMapper appMapper;
    private final WorkloadInstanceDtoMapper workloadInstanceDtoMapper;

    @Autowired
    private AppOnboardingClient appOnboardingClient;
    @Autowired
    private HelmOrchestratorClient helmOrchestratorClient;
    @Autowired
    private ArtifactInstanceService artifactInstanceService;
    @Autowired
    private UrlGenerator urlGenerator;
    @Autowired
    private KeycloakClient keycloakClient;
    @Autowired
    private CredentialEventRepository credentialEventRepository;
    @Autowired
    private InstanceValidator instanceValidator;
    @Autowired
    private KeycloakHandler keycloakHandler;

    /**
     * Creates a new app instance of type {@link AppInstanceDto}.
     * <p>
     * Instantiates an app on helm orchestrator saves app instance to database.
     *
     * @param appInstancePostRequestDto
     *            Data Transfer Object for app instance post request.
     * @return {@link AppInstanceDto}
     */
    @Override
    public AppInstanceDto create(final AppInstancePostRequestDto appInstancePostRequestDto) {
        final Long appId = appInstancePostRequestDto.getAppId();
        log.info("Instantiate app ID {}", appId);
        final ResponseEntity<AppDto> appResponse = appOnboardingClient.getAppById(appId);
        if (APP_ONBOARDING_MODE_DISABLED.equalsIgnoreCase(getAppMode(appResponse))) {
            log.error("Requested app {} is not enabled", appId);
            throw new AppOnBoardingModeException(AppLcmError.APP_ON_BOARDING_DISABLED, appId, APP_ONBOARDING_METHOD_INSTANTIATION, APP_INSTANCES_URL);
        }
        checkNoAppInstancesExist(appId);
        final ResponseEntity<ByteArrayResource> artifactHelmFileResponse = retrieveArtifactFileFromApp(appResponse, ArtifactType.HELM);
        final ResponseEntity<WorkloadInstanceDto> instantiateResponse = createAndPostInstantiateAppRequest(
            appInstancePostRequestDto,
            Objects.requireNonNull(artifactHelmFileResponse.getBody()),
            Objects.requireNonNull(appResponse.getBody()));
        final AppInstance appInstance = generateAndSaveAppInstance(appInstancePostRequestDto, instantiateResponse);
        // Update credential event table with App Instance ID
        updateInstanceIdCredentialEvent(appInstance, appId);
        return appInstanceMapper.map(appInstance, AppInstanceDto.class);
    }

    private void checkNoAppInstancesExist(final Long appId) {
        // check if app instance exist
        List<String> healthStatuses = new ArrayList<>();
        Collections.addAll(healthStatuses, HealthStatus.INSTANTIATED.toString(),
                HealthStatus.FAILED.toString(), HealthStatus.PENDING.toString(), HealthStatus.DELETING.toString(),
                HealthStatus.CREATED.toString());

        List<AppInstance> appInstanceList = appInstanceRepository.findByAppOnBoardingAppId(appId);

        appInstanceList.stream().forEach(appInstance -> {
            for (String status : healthStatuses) {
                if (appInstance.getHealthStatus().name().equals(status)) {
                    log.info("create() Unable to instantiate App. App Instances already exist for provided app Id: {}", appId);
                    throw new AppLcmException(HttpStatus.BAD_REQUEST,
                            com.ericsson.oss.ae.acm.common.constant.AppLcmError.BAD_REQUEST_ERROR_INSTANTIATE_APP_INSTANCES_EXIST);
                }
            }
        });
    }

    @Override
    public AppInstanceDto getAppInstance(final Long appInstanceId) {
        return getAppInstance(appInstanceId, Version.V1);
    }

    /**
     * Returns app instances for a given ID.
     *
     * @param appInstanceId
     *            ID used to identify an app.
     * @param version
     *            V1 or V2.
     * @return {@link AppInstanceDto}
     */
    @Override
    public AppInstanceDto getAppInstance(final Long appInstanceId, Version version) {
        log.debug("Get App Instance for instance id: {}, version: {}",appInstanceId, version);
        final Optional<AppInstance> appInstance = appInstanceRepository.findById(appInstanceId);

        if (appInstance.isEmpty()){
            log.error("App Instance by appInstanceId = {} was not found", appInstanceId);
            if(Version.V2.equals(version)){
                return null;
            }
            throw new ResourceNotFoundException(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND,
                String.format("App Instance by appInstanceId = " + "%s was not found", appInstanceId), APP_INSTANCES_URL);
        }
        return appInstanceMapper.map(appInstance.get(), AppInstanceDto.class);
    }

    @Override
    public AppInstancesDto getAllAppInstances(Long appBoardingId) {
        return getAllAppInstances(appBoardingId, Version.V1);
    }

    /**
     * Returns all app instances.
     *
     * @return {@link AppInstancesDto} object containing all app instances.
     */
    @Override
    public AppInstancesDto getAllAppInstances(Long appBoardingId, Version version) {
        log.debug("getAllAppInstances for appBoardingId: {} and version {}", appBoardingId, version);
        List<AppInstance> apps = appInstanceRepository.findAll();
        if (apps.isEmpty()) {
            log.debug("No apps found in the DB");
            if (Version.V2.equals(version)) {
                List<AppInstanceDto> appInstances = new ArrayList<>();
                AppInstancesDto appInstancesDto = new AppInstancesDto();
                appInstancesDto.setAppInstances(appInstances);
                return appInstancesDto;
            } else {
                var appId = appBoardingId == null ? "" : (" for app Id " + appBoardingId);
                log.error(APP_INSTANCE_NOT_FOUND + appId);
                throw new ResourceNotFoundException(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND, APP_INSTANCE_NOT_FOUND.getErrorMessage(), APP_INSTANCES_URL);
            }
        }

        if(appBoardingId==null){
            log.info("Get All AppInstances for all App Id's");
            return mapAppInstanceToDto(apps);
        }
        else {
            log.info("Get All AppInstances for App Id {}", appBoardingId);
            return getAppInstancesById(appBoardingId);
        }


    }

    private AppInstancesDto mapAppInstanceToDto (List<AppInstance> appInstances){
        log.debug("Map App Instance To Dto");
        final List<AppInstanceDto> appInstanceDtoList = appInstanceMapper.mapAsList(appInstances, AppInstanceDto.class);
        return new AppInstancesDto().appInstances(appInstanceDtoList);
    }

    private AppInstancesDto getAppInstancesById(Long appBoardingId) {
        log.debug("Get App Instances By Id {}", appBoardingId);
        List<AppInstance> appInstanceList = appInstanceRepository.findByAppOnBoardingAppId(appBoardingId);
        if (appInstanceList.isEmpty()) {
            log.info("No App Instances found for appId: {}", appBoardingId);
            throw new ResourceNotFoundException(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND, "App Instances not found", APP_INSTANCES_URL);
        }
        return mapAppInstanceToDto(appInstanceList);
    }

    /**
     * Service method used to request a workloadInstance termination to Helm file executor for each 'INSTANTIATED' artifact instance of a given app
     * instance {@link AppInstance} ID.
     *
     * @param appInstanceId
     *            The id of the app instance {@link AppInstance}.
     */
    @Override
    public void terminate(final Long appInstanceId) {
        log.info("Sending request to terminate app instance id: {}", appInstanceId);
        final AppInstance appInstance = findAppInstance(appInstanceId);
        if (!List.of(HealthStatus.FAILED, HealthStatus.INSTANTIATED).contains(appInstance.getHealthStatus())) {
            final String message = "App Instance should be instantiated (or Failed) to terminate it.";
            log.error("App Instance ID: "+ appInstanceId + " should be instantiated (or Failed) to terminate it.");
            throw new FailureToTerminateException(AppLcmError.FAILURE_TO_TERMINATE_APP, message,
                urlGenerator.generateAppInstanceByIdUrl(appInstanceId));
        }
        log.debug("Terminate artifact instance for instance with health status {}", appInstance.getHealthStatus());
        appInstance.getArtifactInstances().forEach(artifactInstance -> {
            if (List.of(HealthStatus.FAILED, HealthStatus.INSTANTIATED).contains(artifactInstance.getHealthStatus())) {
                artifactInstanceService.terminateArtifactInstance(artifactInstance);
            }
        });
        //check if keycloak resources exists and delete it if present
        deleteKeycloakResources(appInstance.getAppOnBoardingAppId());
        //update app instance status
        updateAppInstanceStatus(appInstance, TargetStatus.TERMINATED, HealthStatus.PENDING);
    }

    /**
     * Build MultiDeleteFailureDetails object
     * @param instantId
     * @param errorCode
     * @param errorMessage
     * @return
     */
    private MultiDeleteFailureDetails getMultiDeleteFailureDetails(long instantId, int errorCode, String errorMessage) {
        log.debug("Get Multi Delete Failure Details: instantId: {}, errorCode: {}, errorMessage: {}", instantId, errorCode, errorMessage);
        MultiDeleteFailureDetails failureDetails = new MultiDeleteFailureDetails();
        failureDetails.appInstanceId(instantId);
        failureDetails.setAppLcmErrorCode(errorCode);
        failureDetails.setFailureMessage(errorMessage);
        return failureDetails;
    }

    /**
     * Process Multi Delete Failure Details Exception
     * If helm has return 404 NOT_FOUND, return null as the requested workload for the instance does not exist and can be deleted
     * Else, return error response from Helm Executor
     * @param exception
     * @param instantId
     * @param workloadInstanceId
     * @return
     */
    private MultiDeleteFailureDetails processExceptionError(Exception exception, long instantId, String workloadInstanceId) {
        log.debug("Process Exception Error");
        if(exception instanceof HelmOrchestratorException){
            final HelmOrchestratorException helmOrchestratorException = (HelmOrchestratorException) exception;
            return getMultiDeleteFailureDetails(instantId,
                helmOrchestratorException.getAppLcmError().getErrorCode(),//inherited error code from Helm
                exception.getMessage() + HELM_WORKLOAD_INSTANCE_ERROR + workloadInstanceId);
        }else if(exception instanceof HttpClientErrorException){
            //if is 404 it means that Helm doesn't have the requested record, so App-Lcm can delete the linked instance - return null
            if(!HttpStatus.NOT_FOUND.equals(((HttpClientErrorException) exception).getStatusCode())){
                return getMultiDeleteFailureDetails(instantId,
                    HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode(),
                    HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage() + " "+ HELM_WORKLOAD_INSTANCE_ERROR + workloadInstanceId);
            }
        }else{
            return getMultiDeleteFailureDetails(instantId,
                HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode(),
                HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage() + " " + HELM_WORKLOAD_INSTANCE_ERROR + workloadInstanceId);
        }
        return null;
    }

    /**
     * Service method used to request an update of a given app instance {@link AppInstance}.
     *
     * @param appInstancePutRequestDto
     *            The {@link AppInstancePutRequestDto} object provided.
     */
    @Override
    public AppInstanceDto updateAppInstance(final AppInstancePutRequestDto appInstancePutRequestDto) {
        log.info("Upgrade(Update) App Instance id: {}", appInstancePutRequestDto.getAppInstanceId());
        final AppInstance appInstance = findAppInstance(appInstancePutRequestDto.getAppInstanceId());
        if (!appInstance.getHealthStatus().equals(HealthStatus.INSTANTIATED)) {
            final String message = "App Instance Id " + appInstance.getId() + " should be instantiated to update it.";
            log.error(message);
            throw new FailureToUpdateException(AppLcmError.FAILURE_TO_UPDATE_APP, message,
                urlGenerator.generateAppInstanceByIdUrl(appInstancePutRequestDto.getAppInstanceId()));
        }
        final ResponseEntity<AppDto> appResponse = appOnboardingClient.getAppById(appInstancePutRequestDto.getAppOnBoardingAppId());
        final ResponseEntity<AppDto> appInstantiatedResponse = appOnboardingClient.getAppById(appInstance.getAppOnBoardingAppId());
        Map<String, List<RoleDto>> roleDiffMap = RoleUtility.roleDiff(Optional.ofNullable(appResponse.getBody()).orElse(new AppDto()), Optional.ofNullable(appInstantiatedResponse.getBody()).orElse(new AppDto()));

        CredentialEvent ce = Optional.ofNullable(credentialEventRepository.findByAppOnBoardingAppId(appInstance.getAppOnBoardingAppId())).orElse(new CredentialEvent());
        if (!(roleDiffMap.get(RoleUtility.add).isEmpty() && roleDiffMap.get(RoleUtility.del).isEmpty())) {
            log.debug("Role difference found, adding following roles: {}", roleDiffMap.get(RoleUtility.add));
            log.debug("Role difference found, removing following roles: {}", roleDiffMap.get(RoleUtility.del));
            String clientId = ce.getClientId();
            RoleUtility.updateClientRoles(roleDiffMap, keycloakClient, clientId);
        }

        Map<String,Object> addParamsMap = Optional.ofNullable(appInstancePutRequestDto.getAdditionalParameters()).orElse(new HashMap<>());
        addParamsMap.put(KeycloakConstants.CLIENT_ID_NAME,ce.getClientId());
        addParamsMap.put(KeycloakConstants.CLIENT_SECRET_NAME,ce.getClientSecret());
        addParamsMap.put(KeycloakConstants.KEYCLOAK_URL_NAME,urlGenerator.getKeycloakHostAndPort());
        appInstancePutRequestDto.setAdditionalParameters(addParamsMap);

        final ResponseEntity<ByteArrayResource> artifactHelmFileResponse = retrieveArtifactFileFromApp(appResponse, ArtifactType.HELM);

        appInstance.getArtifactInstances().forEach(artifactInstance -> {
            artifactInstanceService.updateArtifactInstance(artifactInstance, artifactHelmFileResponse.getBody(), appInstancePutRequestDto);
            if (artifactInstance.getHealthStatus().equals(HealthStatus.PENDING)
                && !appInstance.getAppOnBoardingAppId().equals(appInstancePutRequestDto.getAppOnBoardingAppId())) {
                if (Objects.nonNull(appInstancePutRequestDto.getAdditionalParameters())) {
                    final String additionalParameters = parseMapToJsonString(appInstancePutRequestDto.getAdditionalParameters());
                    appInstance.setAdditionalParameters(additionalParameters);
                } else {
                    appInstance.setAdditionalParameters(null);
                }
                updateAppOnBoardingId(appInstance, appInstancePutRequestDto.getAppOnBoardingAppId());
            }
        });

        return appInstanceMapper.map(appInstance, AppInstanceDto.class);
    }

    private void updateAppOnBoardingId(AppInstance appInstance, @NotNull Long newAppOnBoardingId) {
        log.debug("Upgrade App OnBoarding Id");
        long oldAppOnBoardingId = appInstance.getAppOnBoardingAppId();
        log.info("Old App OnBoarding Id:{}, New App OnBoarding Id:{}", oldAppOnBoardingId, newAppOnBoardingId);
        //update AppOnBoardingId for app instance and his artifact instance
        updateAppInstanceAndHisArtifacts(appInstance, newAppOnBoardingId);
        //update newAppOnBoardingId for Kafka
        updateAppIdCredentialEvent(oldAppOnBoardingId, newAppOnBoardingId);
    }

    private void updateAppInstanceAndHisArtifacts(AppInstance appInstance, @NotNull Long newAppOnBoardingId) {
        log.debug("update App Instance And His Artifacts with new App Id {}", newAppOnBoardingId);
        appInstance.setAppOnBoardingAppId(newAppOnBoardingId);
        for (ArtifactInstance artifact : appInstance.getArtifactInstances()) {
            artifact.setAppOnBoardingArtifactId(newAppOnBoardingId);
        }
        updateAppInstanceStatus(appInstance, TargetStatus.INSTANTIATED,
            HealthStatus.PENDING);
    }

    @Override
    public List<AppInstance> getAllAppInstancesForRequestedFilter(AppInstanceFilter instanceFilter) {
        log.info("Get All App Instances For Requested Filter");
        AppInstanceJpaSpecification specification = new AppInstanceJpaSpecification();
        return appInstanceRepository.findAll(specification.getAppInstanceRequest(instanceFilter));
    }

    private ResponseEntity<WorkloadInstanceDto> createAndPostInstantiateAppRequest(final AppInstancePostRequestDto appInstancePostRequestDto,
                                                                                   final ByteArrayResource artifactHelmFile, AppDto appDto) {
        log.info("Create And Post Instantiate App Request for app {}", appDto.getId());
        final WorkloadInstance workloadInstance = workloadInstanceDtoMapper.map(appInstancePostRequestDto, WorkloadInstance.class);
        HelmAppValidator.validate(workloadInstance);
        final WorkloadInstancePostRequestDto workloadInstancePostRequestDto = workloadInstanceDtoMapper.map(workloadInstance,
            WorkloadInstancePostRequestDto.class);
        final InstantiateWorkloadDto instantiateWorkloadDto = InstantiateWorkloadDto.builder()
            .workloadInstancePostRequestDto(workloadInstancePostRequestDto).helmSource(artifactHelmFile).defaultValues().build();
        String clientId;
        CredentialEvent credentialEvent =  executeNewIemFlow(appDto);
        instantiateWorkloadDto.getWorkloadInstancePostRequestDto().
            putAdditionalParametersItem(KeycloakConstants.CLIENT_ID_NAME,credentialEvent.getClientId());
        instantiateWorkloadDto.getWorkloadInstancePostRequestDto().
            putAdditionalParametersItem(KeycloakConstants.CLIENT_SECRET_NAME,credentialEvent.getClientSecret());
        instantiateWorkloadDto.getWorkloadInstancePostRequestDto().
            putAdditionalParametersItem(KeycloakConstants.KEYCLOAK_URL_NAME,urlGenerator.getKeycloakHostAndPort());
        log.debug("Kafka additional parameters: {}={}, {}={}, {}={}", KeycloakConstants.CLIENT_ID_NAME, credentialEvent.getClientId(),
            KeycloakConstants.CLIENT_SECRET_NAME, credentialEvent.getClientSecret(),
            KeycloakConstants.KEYCLOAK_URL_NAME, urlGenerator.getKeycloakHostAndPort());
        clientId = credentialEvent.getClientId();

        if (areRolesPresent(appDto)) {
            ResponseEntity<ClientRoleDTO[]> rolesInKeycloak = keycloakClient.extractRoles();
            if (RoleUtility.areAllRolesPresent(Optional.ofNullable(rolesInKeycloak.getBody()).orElse(new ClientRoleDTO[]{}), appDto)) {
                List<ClientRoleDTO> clientRoleDTOS = RoleUtility.validateAndExtractRoles(appDto.getRoles(),rolesInKeycloak);
                ResponseEntity<ClientDto[]> clientArray = keycloakClient.getClients();
                String id = Arrays.stream(Optional.ofNullable(clientArray.getBody())
                        .orElse(new ClientDto[]{})).filter(cl -> clientId.equals(cl.getClientId())).map(ClientDto::getId).findFirst().orElse("");
                ResponseEntity<ServiceAccountDto> serviceAccount = keycloakClient.getServiceAccount(id);
                keycloakClient.associateRoles(clientRoleDTOS, Optional.ofNullable(serviceAccount.getBody()).map(ServiceAccountDto::getId).orElse(""));
            } else {
                //clean up credential_event db
                keycloakClient.deleteClientByClientId(clientId);
                credentialEventRepository.delete(credentialEvent);
                log.error(APP_LCM_ROLE_NOT_FOUND_IN_KEYCLOAK.getErrorMessage());
                throw new KeycloakException(APP_LCM_ROLE_NOT_FOUND_IN_KEYCLOAK, "One or multiple roles has not been found in keycloak", "");
            }

        } else {
            log.info("No roles were extracted for app onboarded id: {}", appDto.getId());
        }
        try{
            final ResponseEntity<WorkloadInstanceDto> instantiateResponse = helmOrchestratorClient.instantiateApp(instantiateWorkloadDto);
            log.info("POST Request successfully sent to helm orchestrator to instantiate app with id {}", appDto.getId());
            return instantiateResponse;
        }catch (HelmOrchestratorException helmExecutorException){
            //clean up keycloak for failed instantiation
            deleteKeycloakResources(appInstancePostRequestDto.getAppId());
            throw helmExecutorException;
        }
    }

    private boolean areRolesPresent(AppDto appDto) {
        return Optional.ofNullable(appDto.getRoles()).isPresent() && !appDto.getRoles().isEmpty();
    }
    private ResponseEntity<ByteArrayResource> retrieveArtifactFileFromApp(final ResponseEntity<AppDto> appResponse, final ArtifactType artifactType) {
        log.debug("Retrieve Artifact File From App for artifact type {}", artifactType.name());
        final App app = appMapper.map(appResponse.getBody(), App.class);
        final Optional<Artifact> artifactFile = app.getArtifacts().stream().filter(artifact -> artifact.getType().equals(artifactType.name()))
            .findFirst();
        if (artifactFile.isEmpty()) {
            log.error("Artifact of type {} could not be found", artifactType.name());
            throw new UnableToRetrieveArtifactException(AppLcmError.FAILURE_TO_RETRIEVE_ARTIFACT, "Artifact of type "+ artifactType.name()+" could not be found",
                urlGenerator.generateArtifactInstancesByAppIdUrl(app.getId()));
        }
        return getByteArrayResourceResponseEntity(app, artifactFile.get());
    }

    private ResponseEntity<ByteArrayResource> getByteArrayResourceResponseEntity(final App app, final Artifact artifact) {
        log.info("Artifact exists for application. Downloading the artifact");
        final ResponseEntity<ByteArrayResource> byteArrayResourceResponse = appOnboardingClient.getAppArtifactFile(app.getId(),
            artifact.getId());
        if (byteArrayResourceResponse.getBody() == null ||
            Objects.requireNonNull(byteArrayResourceResponse.getBody()).getByteArray().length == 0) {
            final String message = "Artifact File is Empty";
            log.error(message);
            throw new UnableToRetrieveArtifactException(AppLcmError.FAILURE_TO_RETRIEVE_ARTIFACT, message,
                urlGenerator.generateArtifactInstancesByAppIdUrl(app.getId()));
        }
        return byteArrayResourceResponse;
    }

    private AppInstance generateAndSaveAppInstance(final AppInstancePostRequestDto appInstancePostRequestDto,
                                                   final ResponseEntity<WorkloadInstanceDto> instantiateResponse ) {
        log.debug("Generate And Save App Instance - App Lcm");
        final ArtifactInstance artifactInstance = generateArtifactInstance(appInstancePostRequestDto.getAppId(), instantiateResponse);
        final AppInstance appInstance = new AppInstance();
        appInstance.setAppOnBoardingAppId(appInstancePostRequestDto.getAppId());
        appInstance.setArtifactInstances(Collections.singletonList(artifactInstance));
        artifactInstance.setAppInstance(appInstance);
        if (Objects.nonNull(appInstancePostRequestDto.getAdditionalParameters())) {
            final String additionalParameters = parseMapToJsonString(appInstancePostRequestDto.getAdditionalParameters());
            appInstance.setAdditionalParameters(additionalParameters);
        }
        log.debug("Generated App Instance: {}", appInstance);
        return saveAppInstance(appInstance);
    }

    private void updateInstanceIdCredentialEvent(AppInstance appInstance, Long appOnBoardingAppId) {

        log.debug("Find if Credential Event DB table contains record for App ID {}", appOnBoardingAppId);
        CredentialEvent credentialEvent = credentialEventRepository.findByAppOnBoardingAppId(appOnBoardingAppId);
        if (credentialEvent != null && credentialEvent.getClientId() != null) {
            log.debug("Credential Event Client Id:{}", credentialEvent.getClientId());
            credentialEvent.setAppInstanceId(appInstance.getId());
            credentialEventRepository.saveAndFlush(credentialEvent);
        }
    }

    private AppInstance saveAppInstance(final AppInstance appInstance) {
        log.debug("Saving Application Instance - App Lcm DB");
        appInstanceRepository.save(appInstance);
        log.debug("Application Instance saved successfully.");
        return appInstance;
    }

    private ArtifactInstance generateArtifactInstance(final Long appId, final ResponseEntity<WorkloadInstanceDto> workloadInstanceDto) {
        log.debug("Generate Artifact Instance");
        final WorkloadInstance workloadInstance = workloadInstanceDtoMapper.map(workloadInstanceDto.getBody(), WorkloadInstance.class);
        final String operationId = extractOperationId(workloadInstanceDto);
        log.debug("operationId: {}", operationId);
        return ArtifactInstance.builder().appOnBoardingArtifactId(appId).workloadInstanceId(workloadInstance.getWorkloadInstanceId())
            .operationId(operationId).build();
    }

    private String extractOperationId(final ResponseEntity<WorkloadInstanceDto> response) {
        log.debug("Extract Operation Id");
        final HttpHeaders headers = response.getHeaders();
        final String path = Objects.requireNonNull(headers.getLocation()).getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Update app instance health status to 'pending' and target status to given {@link TargetStatus}.
     *  @param appInstance
     *            The app instance object
     * @param targetStatus
     * @param healthStatus
     **/
    private void updateAppInstanceStatus(final AppInstance appInstance, final TargetStatus targetStatus, HealthStatus healthStatus) {
        log.debug("Updating app instance health status to '{}' and target status  to '{}'", healthStatus, targetStatus);
        if(targetStatus != null){
            appInstance.setTargetStatus(targetStatus);
        }
        if(healthStatus != null){
            appInstance.setHealthStatus(healthStatus);
        }
        appInstanceRepository.save(appInstance);
        log.debug("App Instances updated!");
    }

    private AppInstance findAppInstance(final Long appInstanceId) {
        log.debug("Find App Instance by app instance Id: {}", appInstanceId);
        return appInstanceRepository.findById(appInstanceId)
            .orElseThrow(() -> new ResourceNotFoundException(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND,
                "Could not find App Instance with ID '" + appInstanceId + "'", urlGenerator.generateAppInstanceByIdUrl(appInstanceId)));
    }

    private String getAppMode(final ResponseEntity<AppDto> appResponse) {
        log.debug("Get App Mode");
        if(appResponse != null && appResponse.getBody() != null && Objects.requireNonNull(appResponse.getBody()).getMode() != null) {
            return Objects.requireNonNull(appResponse.getBody()).getMode();
        }
        return null;
    }

    public CredentialEvent executeNewIemFlow (AppDto appDto){
        log.debug("Credential Event record not found in DB.");
        CredentialEvent credentialEvent = new CredentialEvent();
        createClientId(appDto,credentialEvent);
        updateCredentialEvent(HealthStatus.PENDING,credentialEvent);

        try{
            ResponseEntity<CredentialDto> credentialDto = createSecret(credentialEvent.getClientId());
            credentialEvent.setClientSecret(Objects.requireNonNull(credentialDto.getBody()).getValue());
            updateCredentialEvent(HealthStatus.CREATED,credentialEvent);
        } catch (ResourceNotFoundException | KeycloakException e) {
            log.error("Keycloak exception message:{}  ",e.getMessage());
            updateCredentialEvent(HealthStatus.FAILED,credentialEvent);
            throw new KeycloakException(e.getAppLcmError(), e.getMessage(),credentialEvent.getClientId(), e);
        }
        return credentialEvent;
    }

    private ResponseEntity<CredentialDto> createSecret(String clientId){
        log.debug("Create Secret client Id: {}", clientId);
        String id = getIdFromClient(clientId,keycloakClient.getClients().getBody());
        return keycloakClient.createSecret(id);
    }

    public Client createDefaultPropertiesClient(String clientId) {
        log.debug("Create Client for client Id:{}", clientId);
        Client client = (Client) JsonUtils.getObjectFromJsonFile("ClientJsonV1.json",Client.class);
        client.setClientId(clientId);
        client.setClientAuthenticatorType(KeycloakConstants.KEYCLOAK_CLIENT_AUTHENTICATOR_TYPE);
        client.setProtocol(KeycloakConstants.KEYCLOAK_PROTOCOL);
        return client;
    }
    private void createClientId(AppDto appDto, CredentialEvent credentialEvent){
        log.debug("createClientId for app id {}", appDto.getId());
        String appObsId = appDto.getId().toString();
        String clientId = keycloakClient.createClientId(appObsId);
        Client client = createDefaultPropertiesClient(clientId);
        configureClientPermissions(client, appDto);
        Map<String,Object> map = new HashMap<>();
        map.put("ExternalClient","True");
        client.setAttributes(map);
        keycloakClient.createClient(client);
        credentialEvent.setClientId(clientId);
        credentialEvent.setAppOnBoardingAppId(appDto.getId());
    }


    private void configureClientPermissions(Client client, AppDto app){

        List<ProtocolMapperEntry> protocolMapperEntryList = new ArrayList<>();
        app.getPermissions().forEach(
            permission -> {
                log.info("Analyzing claim  '{}' with scope '{}'", permission.getResource(), permission.getScope());
                switch(permission.getResource().toLowerCase(Locale.ROOT)) {
                    case KAFKA:
                        protocolMapperEntryList.add(createGenericClaim());
                        protocolMapperEntryList.add(createCustomClaim("oauth\\.username\\.claim", client.getClientId(), KAFKA));
                        break;
                    case BDR:
                        protocolMapperEntryList.add(createCustomClaim("policy", permission.getScope(), BDR));
                        client.getDefaultClientScopes().add(BDR);
                        break;
                    default:
                        break;
                }
            }

        );
        client.setProtocolMappers(protocolMapperEntryList);
    }

    private ProtocolMapperEntry createCustomClaim(String claimConfigName, String claimValue, String constants){
        ProtocolMapperEntry claimEntry = (ProtocolMapperEntry) JsonParser.getObjectFromJsonFile("CustomOIDCClaim.json", ProtocolMapperEntry.class);
        claimEntry.setName(claimEntry.getName().concat(constants));
        claimEntry.getConfig().put("claim.value", claimValue);
        claimEntry.getConfig().put("claim.name", claimConfigName);

        return claimEntry;
    }

    private ProtocolMapperEntry createGenericClaim(){
        return (ProtocolMapperEntry) JsonParser.getObjectFromJsonFile("StandardOIDCClaim.json", ProtocolMapperEntry.class);
    }

    private void updateCredentialEvent(HealthStatus healthStatus, CredentialEvent credentialEvent) {
        log.debug("Update Credential Event table health status {}", healthStatus);
        credentialEvent.setHealthStatus(healthStatus);
        credentialEventRepository.saveAndFlush(credentialEvent);
    }

    private void updateAppIdCredentialEvent(long oldAppOnBoardingId, @NotNull Long newAppOnBoardingId){
        log.debug("Find Credential Event Client Id and update App OnBoarding Id");
        CredentialEvent event = credentialEventRepository.findByAppOnBoardingAppId(oldAppOnBoardingId);
        if (event != null && event.getClientId() != null) {
            log.debug("Credential Event Client Id:{}", event.getClientId());
            event.setAppOnBoardingAppId(newAppOnBoardingId);
            credentialEventRepository.saveAndFlush(event);
        }
    }

    private String getIdFromClient(String clientId, ClientDto[] body) {
        log.debug("Get Id From Client");
        return  Arrays.stream(body)
            .filter(x -> clientId.equals(x.getClientId())).map(ClientDto::getId)
            .findAny()
            .orElseThrow(() -> {
                log.error("Could not find Client Id:{} ",clientId);
                return new ResourceNotFoundException(AppLcmError.ID_FROM_CLIENT_NOT_FOUND,
                    "Could not find id for ClientId '" + clientId + "'", urlGenerator.generateClientKeycloakUrl(KeycloakConstants.KEYCLOAK_REALM_MASTER));
            });
    }


    /**
     * Delete instance service method for Delete Instances API
     * @param appId
     * @param instanceListRequestDto
     */
    @Override
    public void deleteAppInstances(Long appId, AppInstanceListRequestDto instanceListRequestDto) {
        log.info("Delete app instance with App ID '{}' and App Instance IDs '{}'", appId, instanceListRequestDto);
        //Step 1: Find all requested instances
        AppInstanceFilter filter = new AppInstanceFilter();
        filter.setAppOnBoardingAppId(Arrays.asList(appId));
        filter.setInstanceIds(instanceListRequestDto.getAppInstanceId());
        List<AppInstance> appInstanceList = getAllAppInstancesForRequestedFilter(filter);
        //If not instances found - return error
        if(appInstanceList.isEmpty()) {
            log.error(SPECIFIED_APP_INSTANCE_NOT_FOUND +". "+ FAILED_TO_DELETE_MESSAGE + appId);
            throw new ResourceNotFoundException(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND,
                FAILED_TO_DELETE_MESSAGE + appId,
                APP_LCM_DELETE_PATH + appId + SLASH + APP_INSTANCES);
        }
        //Check if all requested instances has been found - compare DB list with requested instance list
        List<MultiDeleteFailureDetails> listOfInstancesNotFound = instanceValidator.compareTwoList(instanceListRequestDto, appInstanceList);
        log.debug("Number of instances not found: {}. Now Now verify health status", listOfInstancesNotFound.size());
        //Step 2: Verify if the app instances and their artifacts are in unhealthy health status (TERMINATED, FAILED, DELETED).
        List<MultiDeleteFailureDetails> errorList = instanceValidator.validateAppInstancesForDeletion(appInstanceList);
        errorList.addAll(listOfInstancesNotFound);
        Comparator<MultiDeleteFailureDetails> comparatorById = Comparator.comparing(MultiDeleteFailureDetails::getAppInstanceId);
        Collections.sort(errorList, comparatorById);
        //find all instances valid for deletion
        List<AppInstance> appInstancesToRemove = findInstancesToRemove(errorList, appInstanceList);
        if(appInstancesToRemove.isEmpty()){
            checkErrorListAndReturnErrorMessageIfExist(errorList, 0L);
        }
        log.debug("Helm is going to remove {} artifact instances", appInstancesToRemove.size());
        //Step 3: Attempt to delete Helm resources (workloads)
        appInstancesToRemove.forEach(instance -> updateAppInstanceStatus(instance, TargetStatus.DELETED, HealthStatus.DELETING));
        Long numOfDeletedArtifactsForInstance = deleteHelmResources(appInstancesToRemove, errorList);
        //If for any reason Helm could not delete resources for requested instance - remove this instance from the list of instances to remove
        log.debug("NumOfDeletedArtifactsForInstance: {}, appInstancesToRemove: {}", numOfDeletedArtifactsForInstance, appInstancesToRemove.size());
        if(numOfDeletedArtifactsForInstance != appInstancesToRemove.size()){
            appInstancesToRemove = findInstancesToRemove(errorList, appInstanceList);
        }
        log.info("Delete {} instance(s) and their artifacts from app-lcm", appInstancesToRemove.size());
        //Step 4: Delete instances and their artifacts from app-lcm
        if(!appInstancesToRemove.isEmpty()){
            appInstanceRepository.deleteAll(appInstancesToRemove);
        }
        checkErrorListAndReturnErrorMessageIfExist(errorList, numOfDeletedArtifactsForInstance);
    }

    @Override
    public void deleteApp(Long appId) {
        log.info("Delete application service");
        //Verify if the app exist and is in disabled mode.
        final ResponseEntity<AppDto> appResponse = appOnboardingClient.getAppById(appId);
        //If it does not exist or is enabled - return error
        if (!APP_ONBOARDING_MODE_DISABLED.equalsIgnoreCase(getAppMode(appResponse))) {
            log.error("Requested app {} is not enabled", appId);
            throw new AppOnBoardingModeException(APP_ON_BOARDING_ENABLED, appId, APP_ONBOARDING_MODE_DISABLED,
                APP_ONBOARDING_METHOD_DELETION, APP_INSTANCES_URL);
        }
        //Find all instances for app ID
        AppInstanceFilter filter = new AppInstanceFilter();
        filter.setAppOnBoardingAppId(Arrays.asList(appId));//Add criteria for search to filter
        List<AppInstance> appInstanceList = getAllAppInstancesForRequestedFilter(filter);
        //If no instances are found - just request to delete onBoarding resources
        boolean isOnBoardingStatusDeleting = isOnBoardingStatusDeleting(appResponse);
        if(appInstanceList.isEmpty()) {
            //no instances found for App ID in App-Lcm, so delete App-OnBoarding resources only
            log.debug("No instance(s) found in App-Lcm. Delete Keycloak resources if still persist and OnBoarding Resources for App ID {}", appId);
            deleteKeycloakResources(appId);
            deleteOnBoardingResourcesOnly(appId, isOnBoardingStatusDeleting);
        }else{
            //Verify if the app instances and their artifacts for the app are in unhealthy(TERMINATED or FAILED) state.
            log.debug("Validate {} Instances found in App-Lcm and required to Delete", appInstanceList.size());
            List<MultiDeleteFailureDetails> errorList = instanceValidator.validateAppInstancesForDeletion(appInstanceList);
            // If Not - throw error with information which instances are in invalid health status
            checkErrorListAndReturnErrorMessageIfExist(errorList, 0L);
            //Update onBoarding and App-Lcm about deletion
            if(!isOnBoardingStatusDeleting){//for the case when the deletion was ready Accepted but not performed, and the onBoarding is ready set to DELETING
                updateOnBoardingAboutDeletion(appId);
            }
            updateAppInstanceStatusForDeletion(appInstanceList, HealthStatus.DELETING, TargetStatus.APP_DELETED);
        }
    }

    private void deleteOnBoardingResourcesOnly(Long appId, boolean isOnBoardingStatusDeleting) {
        log.debug("Delete OnBoarding Resources Only. Is OnBoarding Status DELETING: {}", isOnBoardingStatusDeleting);
        if(!isOnBoardingStatusDeleting){
            updateOnBoardingAboutDeletion(appId);
        }
        deleteOnBoardingPackage(appId);
    }

    private void deleteOnBoardingPackage(Long appId) {
        log.debug("Send Delete request to OnBoarding for App ID "+appId);
        try{
            ResponseEntity<Object> deleteResponse = appOnboardingClient.deletePackage(appId);
            if(HttpStatus.NO_CONTENT.equals(deleteResponse.getStatusCode())){
                log.debug("Delete confirmed by OnBoarding for App ID "+appId);
            }else{
                final String deleteAppOnBoardingUrl = urlGenerator.generateAppOnBoardingWithIdUrl(appId);
                log.error(APP_ON_BOARDING_DELETE_ERROR.getErrorMessage());
                throw new AppOnBoardingDeleteException(APP_ON_BOARDING_DELETE_ERROR,
                    APP_ONBOARDING_DELETING_ERROR +appId,
                    deleteAppOnBoardingUrl);
            }
        }catch (final ResourceAccessException exception) {//catch unavailable service
            final String message = APP_ONBOARDING_DELETING_ERROR + appId;
            final String deleteAppOnBoardingUrl = urlGenerator.generateAppOnBoardingWithIdUrl(appId);
            log.error(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorMessage(),
                exception);
            throw new AppLcmServiceException(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE,
                message,
                deleteAppOnBoardingUrl, exception);
        }catch (final RestClientException exception) {//general catch
            final String deleteAppOnBoardingUrl = urlGenerator.generateAppOnBoardingWithIdUrl(appId);
            log.error(APP_ON_BOARDING_DELETE_ERROR.getErrorMessage(), exception);
            throw new AppOnBoardingDeleteException(APP_ON_BOARDING_DELETE_ERROR,
                APP_ONBOARDING_DELETING_ERROR + appId,
                deleteAppOnBoardingUrl);
        }
    }

    private void updateOnBoardingAboutDeletion(Long appId) {
        log.debug("Update OnBoarding About Deletion for App ID "+appId);
        final ResponseEntity<Void>  updateStatusForDeletionResponse = appOnboardingClient.updateStatusForDeletion(appId);
        if(!HttpStatus.OK.equals(updateStatusForDeletionResponse.getStatusCode())){
            final AppLcmError error = failedToUpdateStatusForDeletionOnBoarding(updateStatusForDeletionResponse.getStatusCode());
            log.error(APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage()+ "Reason: "+ error.getErrorMessage());
            throw new AppOnBoardingDeleteException(error, APP_ON_BOARDING_UPDATE_DELETING_FAILED.getErrorMessage());
        }
    }

    private boolean isOnBoardingStatusDeleting(ResponseEntity<AppDto> appResponse) {
        log.debug("Check if OnBoarding status is DELETING");
        if (appResponse != null && appResponse.getBody() != null && Objects.requireNonNull(appResponse.getBody()).getStatus() != null) {
            return Objects.requireNonNull(appResponse.getBody()).getStatus().equals(HealthStatus.DELETING.toString());
        }
        return false;
    }

    private AppLcmError failedToUpdateStatusForDeletionOnBoarding(HttpStatusCode statusCode) {
        log.debug("Update of OnBoarding about Deletion has failed. HttpStatus: {}", statusCode);
        if(HttpStatus.NOT_FOUND.equals(statusCode)){
            return APP_ONBOARDING_APP_NOT_FOUND;
        }
        return APP_ON_BOARDING_UPDATE_DELETING_FAILED;
    }

    private void updateAppInstanceStatusForDeletion(List<AppInstance> appInstanceList, HealthStatus healthStatus, TargetStatus targetStatus) {
        appInstanceList.forEach(instance -> {
            instance.setHealthStatus(healthStatus);
            instance.setTargetStatus(targetStatus);
        });
        log.debug("Deletion Accepted -Update App Instance Status For App-Lcm. Instance list: {}", appInstanceList);
        appInstanceRepository.saveAll(appInstanceList);
        log.debug("App-Lcm app Instance list updated");
    }

    /**
     * Find Instances To Remove
     * @param errorList
     * @param appInstanceList
     * @return
     */
    private List<AppInstance> findInstancesToRemove(List<MultiDeleteFailureDetails> errorList, List<AppInstance> appInstanceList) {
        log.debug("Find Instances To Remove. Error list size: {}", errorList.size());
        if(!errorList.isEmpty()){
            List<AppInstance> instancesToRemove = new ArrayList<>(appInstanceList);
            for(AppInstance instance : appInstanceList){
                errorList.stream()
                    .filter(errorInstance -> errorInstance.getAppInstanceId().equals(instance.getId()))
                    .findAny().ifPresent(e -> instancesToRemove.remove(instance));
            }
            return instancesToRemove;
        }else{
            return appInstanceList;
        }
    }

    /**
     * Delete App Instance Resources
     * @param appInstanceList
     */
    public boolean deleteAppInstancesResources(Long appId, List<AppInstance> appInstanceList) {
        //Attempt to delete workloads from Helm for requested app.
        log.info("Delete App Instances from Helm and App-Lcm. App Instance List Size: {}", appInstanceList.size());
        List<MultiDeleteFailureDetails> errorList = new ArrayList<>();
        //delete helm resources
        deleteHelmResources(appInstanceList, errorList);
        // If unsuccessful return false. If successful move to the step
        if(!errorList.isEmpty()){
            log.debug("Helm deletion has failed");
            return false;
        }
        log.debug("Helm deletion succeed. Next delete {} instance(s) and its artifacts from App Lcm and Keycloak resources if still exist", appInstanceList.size());
        //check if keycloak resources exists and delete it if present
        deleteKeycloakResources(appId);
        //Delete instances and their artifacts from app-lcm
        if(!appInstanceList.isEmpty()){

            appInstanceRepository.deleteAll(appInstanceList);
            return true;
        }
        return false;
    }
    /**
     * Delete Keycloak Resources if Present
     * @param appId
     * @return
     */
    private void deleteKeycloakResources(Long appId) {
        CredentialEvent event = credentialEventRepository.findByAppOnBoardingAppId(appId);
        log.debug("Delete Keycloak Resources method called");
        if (event != null && event.getClientId() != null) {
            log.debug("Set Pending Deletion status for appId: {} in CredentialEventRepository", appId);
            event.setDeletionStatus("PENDING_DELETION");
            credentialEventRepository.save(event);
        }
    }

    /**
     * Delete Helm Resources
     * @param appInstanceList
     * @param errorList
     * @return
     */
    private Long deleteHelmResources(List<AppInstance> appInstanceList, List<MultiDeleteFailureDetails> errorList) {
        log.debug("Delete Helm Resources method");
        Long numOfDeletedArtifactsForInstance = 0L;
        for(AppInstance instance : appInstanceList){
            if(instance.getArtifactInstances() != null && deleteArtifactInstances(instance, errorList)){
                log.debug("Helm deleted resources for instance {}", instance.getId());
                numOfDeletedArtifactsForInstance ++;
                updateAppInstanceStatus(instance, null,
                    HealthStatus.DELETED);
            }else {
                log.debug("Helm failed to delete resources for instance {}", instance.getId());
                updateAppInstanceStatus(instance,
                    null,
                    HealthStatus.FAILED);
            }
        }
        return numOfDeletedArtifactsForInstance;
    }

    /**
     * Delete Artifact Instances
     * @param appInstance
     * @param errorList
     * @return
     */
    private boolean deleteArtifactInstances(AppInstance appInstance, List<MultiDeleteFailureDetails> errorList) {
        log.debug("Delete artifact instance method");
        int currentErrorListSize = errorList.size();

        appInstance.getArtifactInstances().forEach(artifactInstance -> {
            MultiDeleteFailureDetails failureDetails = null;
            try{
                log.debug("Delete artifact instance Id: {}", artifactInstance.getId());
                artifactInstanceService.deleteArtifactInstance(artifactInstance);
                if(HealthStatus.FAILED.equals(artifactInstance.getHealthStatus())){
                    failureDetails = getMultiDeleteFailureDetails(appInstance.getId(),
                        HELM_ORCHESTRATOR_DELETE_ERROR.getErrorCode(),
                        HELM_ORCHESTRATOR_DELETE_ERROR.getErrorMessage());
                }
            }catch (Exception exception) {
                log.error(exception.getMessage(), HELM_ORCHESTRATOR_OPERATION_ERROR, exception);
                failureDetails = processExceptionError(exception, appInstance.getId(), artifactInstance.getWorkloadInstanceId());
            }

            if(failureDetails != null) {
                errorList.add(failureDetails);
                artifactInstance.setHealthStatus(HealthStatus.FAILED);

            }else{
                artifactInstance.setHealthStatus(HealthStatus.DELETED);
            }
            log.debug("Artifact Id: {} Health Status: {}", artifactInstance.getId(), artifactInstance.getHealthStatus());
        });
        return currentErrorListSize == errorList.size();
    }

    /**
     * Check Error List And Return Error Message If Error List is not empty
     * @param errorList
     * @param numOfDeletedInstances
     */
    private void checkErrorListAndReturnErrorMessageIfExist(List<MultiDeleteFailureDetails> errorList, Long numOfDeletedInstances) {
        log.debug("Check Error List And Return Error Message If The List Is Not Empty.");
        if(!errorList.isEmpty()){
            log.debug("Throw message error for {} instances(s). Total number of deleted instances: {} ", errorList.size(), numOfDeletedInstances);
            throwFailureToDeleteException(errorList, numOfDeletedInstances);
        }
    }

    private void throwFailureToDeleteException(List<MultiDeleteFailureDetails> errorList, Long numOfDeletedInstances) {
        log.debug("Throw Failure To Delete Exception method");
        if(numOfDeletedInstances == 0){
            log.error(AppLcmError.FAILURE_TO_DELETE.getErrorMessage());
            throw new FailureToDeleteException(AppLcmError.FAILURE_TO_DELETE,
                numOfDeletedInstances,
                errorList);
        }else{
            log.error(AppLcmError.APP_LCM_PARTIAL_DELETE_FAILURE.getErrorMessage());
            throw new FailureToDeleteException(AppLcmError.APP_LCM_PARTIAL_DELETE_FAILURE,
                numOfDeletedInstances,
                errorList);
        }
    }
}
