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

package com.ericsson.oss.ae.clients.helmorchestrator.dto;

import com.ericsson.oss.ae.utils.file.FileNameAwareByteArrayResource;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceOperationPostRequestDto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO class used to model REST requests to terminate workloadInstance in Helm Orchestrator.
 * <p>
 * WorkloadInstanceOperationPostRequestDto {@link WorkloadInstanceOperationPostRequestDto} is the object used for terminate workloadInstance. It is
 * imported from the helm orchestrator project.
 * <p>
 * workloadInstanceId is the workload instance id of the App instance to be terminated. This id will be unique to every app instance deployed by helm
 * orchestration.
 */
@Data
@Builder
public class TerminateWorkloadDto {
    private final WorkloadInstanceOperationPostRequestDto workloadInstanceOperationPostRequestDto;
    private final FileNameAwareByteArrayResource clusterConnectionInfo;
}
