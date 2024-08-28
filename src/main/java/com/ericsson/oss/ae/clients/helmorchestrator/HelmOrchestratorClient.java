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
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import org.springframework.http.ResponseEntity;

/**
 * Interface specifying helm orchestrator client calls.
 * <p>
 * Implementation of this interface {@link com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClientImpl}.
 */
public interface HelmOrchestratorClient {

    ResponseEntity<WorkloadInstanceDto> instantiateApp(InstantiateWorkloadDto instantiateWorkloadDto);

    ResponseEntity<WorkloadInstanceDto> updateApp(UpdateWorkloadDto updateWorkloadDto, String workloadInstanceId, Long artifactInstanceId);

    ResponseEntity<OperationDto> getOperation(String operationId);

    ResponseEntity<Void> terminateAppInstanceById(String workloadInstanceId, TerminateWorkloadDto terminateWorkloadDto);

    ResponseEntity<Void> deleteWorkloadInstanceId(String instanceId);

    ResponseEntity<String> getOperationLogs(String operationId);

}
