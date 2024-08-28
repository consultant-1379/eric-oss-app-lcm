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

package com.ericsson.oss.ae.clients.helmorchestrator;

import com.ericsson.oss.ae.clients.helmorchestrator.dto.InstantiateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.TerminateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.UpdateWorkloadDto;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.utils.rest.RequestHandler;
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import static com.ericsson.oss.ae.utils.mapper.MapperUtils.mapRequestModelToMultiValueMap;

/**
 * Implementation of helm orchestrator REST client class {@link HelmOrchestratorClient}.
 * <p>
 * Contains methods used to make REST requests to Helm Orchestrator service.
 */
@Service
@Slf4j
public class HelmOrchestratorClientImpl implements HelmOrchestratorClient {

    @Autowired
    private RequestHandler requestHandler;

    @Autowired
    private UrlGenerator urlGenerator;

    /**
     * Invokes a rest request using {@link RequestHandler} to helm orchestrator service to instantiate an app.
     *
     * @param instantiateWorkloadDto
     *            Map containing files required for helm orchestrator.
     * @return Returns app attributes from helm orchestrator after instantiation of an app.
     */
    @Override
    public ResponseEntity instantiateApp(final InstantiateWorkloadDto instantiateWorkloadDto) {
        final String url = urlGenerator.generateWorkloadInstancesUrl();
        log.debug("Helm Send Rest Request Using URL: {},to Instantiate App", url);
        try {
            return requestHandler.sendRestRequestUsingParticipant(mapRequestModelToMultiValueMap(instantiateWorkloadDto), url,
                    WorkloadInstanceDto.class, HttpMethod.POST);
        } catch (final RestClientException exception) {
            instantiateWorkloadDto.getWorkloadInstancePostRequestDto().setAdditionalParameters(null);
            final String message = "Error instantiating application with Request Entity: " + instantiateWorkloadDto;
            log.error(message, exception.getMessage());
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP, message, url, exception);
        }
    }

    
    @Override
    public ResponseEntity updateApp(final UpdateWorkloadDto updateWorkloadDto, final String workloadInstanceId, final Long artifactInstanceId) {
        final String url = urlGenerator.generateWorkloadInstanceByIdUrl(workloadInstanceId);
        log.info("Helm Update App Using URL: {}", url);
        try {
            return requestHandler.sendRestRequestUsingParticipant(mapRequestModelToMultiValueMap(updateWorkloadDto), url, WorkloadInstanceDto.class,
                    HttpMethod.PUT);
        } catch (final RestClientException exception) {
            final String message = "Error updating application with Request Entity: " + updateWorkloadDto;
            log.error(message, exception.getMessage());
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_FAILURE_TO_UPDATE_APP, message, url, exception);
        }
    }

    /**
     * Makes a rest request using {@link RequestHandler} to helm file executor to get operations.
     *
     * @param operationId
     *            OperationId used to generate response by helm file executor.
     * @return Returns app attributes from helm file executor.
     */
    @Override
    public ResponseEntity getOperation(final String operationId) {
        final String url = urlGenerator.generateOperationsByIdUrl(operationId);
        log.info("Helm Get Operation Using URL: {}", url);
        try {
            return requestHandler.createAndSendRestRequest(url, OperationDto.class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestClientException exception) {
            final String message = "Error retrieving operation for operation ID " + operationId;
            log.error(message, exception.getMessage());
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_OPERATION_ERROR, message, url, exception);
        }
    }

    /**
     * Makes a rest request using {@link RequestHandler} to helm file executor to get operations logs.
     *
     * @param operationId
     *            OperationId used to generate response by helm file executor.
     * @return Returns artefacts logs.
     */
    @Override
    public ResponseEntity getOperationLogs(final String operationId) {
        final String url = urlGenerator.generateOperationsLogsByIdUrl(operationId);
        log.info("Helm Get Operation Logs Using URL: {}", url);
        try {
            return requestHandler.createAndSendRestRequest(url, String.class, MediaType.APPLICATION_JSON, HttpMethod.GET);
        } catch (final RestClientException exception) {
            final String message = "Error retrieving operation logs for operation ID " + operationId;
            log.error(message, exception.getMessage());
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_OPERATION_LOGS_ERROR, message, url, exception);
        }
    }

    /**
     * Makes a rest request using {@link RequestHandler} to helm file executor to terminate a workload instance.
     *
     * @param workloadInstanceId
     *            The Workload Instance ID.
     * @param terminateWorkloadDto
     *            The request body needed to terminate a workload instance.
     * @return The response is returned without body.
     *         <p>
     *         It contains the operationId in the header.
     */
    @Override
    public ResponseEntity terminateAppInstanceById(final String workloadInstanceId, final TerminateWorkloadDto terminateWorkloadDto) {
        final String url = urlGenerator.generateWorkloadInstanceOperationsByIdUrl(workloadInstanceId);
        log.info("Send Helm Terminate Request Using URL: {}", url);
        try {
            return requestHandler.sendRestRequestUsingParticipant(mapRequestModelToMultiValueMap(terminateWorkloadDto), url, Void.class,
                    HttpMethod.POST);
        } catch (final RestClientException exception) {
            final String message = "Error terminating app instance with for Workload Instance with ID " + workloadInstanceId;
            log.error(message, exception.getMessage());
            throw new HelmOrchestratorException(AppLcmError.HELM_ORCHESTRATOR_TERMINATION_ERROR, message, url, exception);
        }
    }

    /**
     * Makes a rest request using {@link RequestHandler} to helm file executor to delete a workload instance.
     *
     * @param instanceId
     *            The workload instance ID.
     * @return The response is returned without body.
     *         <p>
     *         It contains the operationId in the header.
     */
    @Override
    public ResponseEntity deleteWorkloadInstanceId(final String instanceId) {
        final String url = urlGenerator.generateWorkloadInstanceByIdUrl(instanceId);
        log.info("Helm Delete Workload Instance Id Using URL: {}", url);
        return requestHandler.createAndSendRestRequest(url, Void.class, MediaType.APPLICATION_JSON, HttpMethod.DELETE);
    }
}