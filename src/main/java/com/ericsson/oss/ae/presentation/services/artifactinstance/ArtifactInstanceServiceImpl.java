/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.ae.presentation.services.artifactinstance;

import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.TerminateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.UpdateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.mapper.WorkloadInstanceDtoMapper;
import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.presentation.mappers.ArtifactInstanceMapper;
import com.ericsson.oss.ae.repositories.ArtifactInstanceRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.validator.HelmAppValidator;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceOperationPostRequestDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePutRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation for Artifact Instance Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactInstanceServiceImpl implements ArtifactInstanceService {

    private final ArtifactInstanceRepository artifactInstanceRepository;
    private final ArtifactInstanceMapper artifactInstanceMapper;
    private final WorkloadInstanceDtoMapper workloadInstanceDtoMapper;

    @Autowired
    private UrlGenerator urlGenerator;
    @Autowired
    private HelmOrchestratorClient helmOrchestratorClient;

    @Override
    public ArtifactInstanceDto getArtifactInstance(final Long appInstanceId, final Long artifactInstanceId) {
        return getArtifactInstance(appInstanceId, artifactInstanceId, Version.V1);
    }

    /**
     * Method used to get artifact instance as an {@link ArtifactInstanceDto} by a given artifact instance {@link ArtifactInstance} ID and app
     * instance {@link AppInstance} ID.
     *
     * @param appInstanceId
     *            The app instance ID.
     * @param artifactInstanceId
     *            The artifact instance ID.
     * @param version
     *            V1 or V2.
     * @return The details about artifact instance as an {@link ArtifactInstanceDto}.
     */
    @Override
    public ArtifactInstanceDto getArtifactInstance(final Long appInstanceId, final Long artifactInstanceId, Version version) {

        log.info("Get Artifact Instance for appInstanceId {} and artifactInstanceId {}", appInstanceId, artifactInstanceId);
        final Optional<ArtifactInstance> artifactInstance = artifactInstanceRepository.findByAppInstanceIdAndId(appInstanceId, artifactInstanceId);

        if (artifactInstance.isEmpty()) {

            log.error("Artifact Instance with artifactInstanceId = {} belonging to appInstanceId = {} was not found", artifactInstanceId,appInstanceId);
            if(Version.V2.equals(version)){
                return null;
            }
            throw new ResourceNotFoundException(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND,
                    String.format("Artifact Instance with artifactInstanceId = %s belonging to appInstanceId = %s was not found", artifactInstanceId,
                            appInstanceId),
                    urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdUrl(appInstanceId, artifactInstanceId));
        } else {
            return artifactInstanceMapper.map(artifactInstance.get(), ArtifactInstanceDto.class);
        }
    }

    @Override
    public ArtifactInstancesDto getAllArtifactInstances(final Long appInstanceId) {
        return getAllArtifactInstances(appInstanceId, Version.V1);
    }

    /**
     * Method used to get all artifact instances as an {@link ArtifactInstancesDto} by a given app instance {@link AppInstance} ID.
     *
     * @param appInstanceId
     *            The artifact instance ID.
     * @param version
     *            V1 or V2.
     * @return A list of {@link ArtifactInstanceDto} as an {@link ArtifactInstanceDto}.
     */
    @Override
    public ArtifactInstancesDto getAllArtifactInstances(final Long appInstanceId, Version version) {

        log.info("Get All Artifact Instances for appInstanceId: {}", appInstanceId);
        final Optional<List<ArtifactInstance>> artifactInstances = artifactInstanceRepository.findAllArtifactInstancesByAppInstanceId(appInstanceId);

        if (artifactInstances.isEmpty() || artifactInstances.get().isEmpty()) {
            log.error("Artifact Instances belonging to appInstanceId = {} were not found", appInstanceId);

            if(Version.V2.equals(version)){
                List<ArtifactInstanceDto> artifactInstanceDtoList = new ArrayList<>();
                ArtifactInstancesDto artifactInstancesDto = new ArtifactInstancesDto();
                artifactInstancesDto.setArtifactInstances(artifactInstanceDtoList);

                return artifactInstancesDto;
            }
            throw new ResourceNotFoundException(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND,
                    String.format("Artifact Instances belonging to appInstanceId = %s were not found", appInstanceId),
                    urlGenerator.generateArtifactInstancesByAppIdUrl(appInstanceId));
        }
        final List<ArtifactInstanceDto> artifactInstanceDtoList = artifactInstanceMapper.mapAsList(artifactInstances.get(),
                ArtifactInstanceDto.class);
        return new ArtifactInstancesDto().artifactInstances(artifactInstanceDtoList);
    }

    /**
     * Method used to save {@link ArtifactInstance} to the DB.
     *
     * @param artifactInstance
     *            The {@link ArtifactInstance} object provided.
     */
    @Override
    public void saveArtifactInstance(final ArtifactInstance artifactInstance) {
        log.info("Save Artifact Instance for artifactInstance {}", artifactInstance);
        try {
            artifactInstanceRepository.save(artifactInstance);
        } catch (final DataAccessException e) {
            final String error = "Failed to save artifact instance Id " + artifactInstance;
            log.error(error, e);
        }
    }

    /**
     * Service method used to request a workloadInstance termination to Helm file executor of a given {@link ArtifactInstance}.
     *
     * @param artifactInstance
     *            The {@link ArtifactInstance} object provided.
     */
    @Override
    public void terminateArtifactInstance(final ArtifactInstance artifactInstance) {
        log.info("Sending request to Helm Orchestrator to terminate instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());

        final WorkloadInstanceOperationPostRequestDto workloadInstanceOperationPostRequestDto = new WorkloadInstanceOperationPostRequestDto();
        workloadInstanceOperationPostRequestDto.type("terminate");
        final TerminateWorkloadDto terminateWorkloadDto = TerminateWorkloadDto.builder()
                .workloadInstanceOperationPostRequestDto(workloadInstanceOperationPostRequestDto).build();

        final ResponseEntity<Void> terminateResponse = helmOrchestratorClient.terminateAppInstanceById(artifactInstance.getWorkloadInstanceId(),
                terminateWorkloadDto);
        if (terminateResponse.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            log.info("Helm Orchestrator has begun terminate of instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());
            log.debug("Retrieving operationId from Helm Orchestrator");
            final String operationId = extractOperationId(terminateResponse);

            log.debug("Changing operationId to {} and health status PENDING in Artifact Instance", operationId);
            artifactInstance.setOperationId(operationId);
            artifactInstance.setHealthStatus(HealthStatus.PENDING);
        } else {
            artifactInstance.setHealthStatus(HealthStatus.FAILED);
            log.info("Failure to terminate workload instance in Helm Orchestrator with workloadInstance id: {}",
                    artifactInstance.getWorkloadInstanceId());
        }
        artifactInstanceRepository.save(artifactInstance);
    }

    /**
     * Service method used to request a workloadInstance deletion to Helm file executor of a given {@link ArtifactInstance}.
     *
     * @param artifactInstance
     *            The {@link ArtifactInstance} object provided.
     */
    @Override
    public void deleteArtifactInstance(final ArtifactInstance artifactInstance) {
        log.info("Sending request to Helm Orchestrator to delete instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());
        final ResponseEntity<Void> deleteResponse = helmOrchestratorClient.deleteWorkloadInstanceId(artifactInstance.getWorkloadInstanceId());
        if (deleteResponse.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            log.info("Helm Orchestrator has begun delete of instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());
            log.debug("Changing health status in Artifact Instance to 'DELETED'");
            artifactInstance.setHealthStatus(HealthStatus.DELETED);
        } else {
            artifactInstance.setHealthStatus(HealthStatus.FAILED);
            log.info("Failure to delete workload instance in Helm Orchestrator with workloadInstance id: {}",
                    artifactInstance.getWorkloadInstanceId());
        }
        artifactInstanceRepository.save(artifactInstance);
    }

    /**
     * Service method used to request a workloadInstance update to Helm file executor of a given {@link ArtifactInstance}.
     *
     * @param artifactInstance
     *            The {@link ArtifactInstance} object provided.
     */
    @Override
    public void updateArtifactInstance(final ArtifactInstance artifactInstance, final ByteArrayResource artifactHelmFile,
            final AppInstancePutRequestDto appInstancePutRequestDto) {
        log.info("Sending request to Helm Orchestrator to update instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());
        if (artifactInstance.getHealthStatus().equals(HealthStatus.INSTANTIATED)) {
            final ResponseEntity<WorkloadInstanceDto> response = createAndSendUpdateAppRequest(artifactInstance.getWorkloadInstanceId(),
                    artifactInstance.getId(), artifactHelmFile, appInstancePutRequestDto);
            log.debug("PUT Request successfully sent to helm orchestrator to update app");
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Helm Orchestrator has begun update of instance with workloadInstance id: {}", artifactInstance.getWorkloadInstanceId());
                log.debug("Changing health status in Artifact Instance to 'PENDING'");
                artifactInstance.setHealthStatus(HealthStatus.PENDING);
                artifactInstance.setOperationId(extractOperationId(response));
            } else {
                log.info("Helm orchestrator failed to update app.");
            }
        }
    }

    private ResponseEntity<WorkloadInstanceDto> createAndSendUpdateAppRequest(final String workloadInstanceId, final Long artifactInstanceId,
            final ByteArrayResource artifactHelmFile, final AppInstancePutRequestDto appInstancePutRequestDto) {
        log.debug("Create And Send Update App Request");
        final WorkloadInstance workloadInstance = workloadInstanceDtoMapper.map(appInstancePutRequestDto, WorkloadInstance.class);
        HelmAppValidator.validate(workloadInstance);
        final WorkloadInstancePutRequestDto workloadInstancePutRequestDto = workloadInstanceDtoMapper.map(workloadInstance,
                WorkloadInstancePutRequestDto.class);
        final UpdateWorkloadDto updateWorkloadDto = UpdateWorkloadDto.builder()
                .workloadInstancePutRequestDto(workloadInstancePutRequestDto).helmSource(artifactHelmFile).defaultValues().build();
        log.debug("Sending request to helm orchestrator to update app");
        return helmOrchestratorClient.updateApp(updateWorkloadDto, workloadInstanceId, artifactInstanceId);
    }

    private String extractOperationId(final ResponseEntity<?> response) {
        log.debug("Extract Operation Id");
        final HttpHeaders headers = response.getHeaders();
        final String path = Objects.requireNonNull(headers.getLocation()).getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}