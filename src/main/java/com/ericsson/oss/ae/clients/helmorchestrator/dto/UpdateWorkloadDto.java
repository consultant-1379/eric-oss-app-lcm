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

package com.ericsson.oss.ae.clients.helmorchestrator.dto;

import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_SOURCE_FILE;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_VALUES_FILE;

import org.springframework.core.io.ByteArrayResource;

import com.ericsson.oss.ae.utils.file.FileNameAwareByteArrayResource;
import com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePutRequestDto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO class used to model requests to helm orchestrator for app updating. It handles the files values.yaml, helm source and cluster connection
 * info as ByteArrayResource objects.
 * <p>
 * <ul>
 * <li>{@link WorkloadInstancePutRequestDto} is imported from helm orchestrator project. It is a request object, where user can set parameters to
 * describe workload instance.</li>
 * <li>{@link #helmSource} is a {@link ByteArrayResource} containing the helm charts of the application.
 * <li>{@link #values} is a {@link ByteArrayResource} containing the values.yaml file required to deploy an application.
 * <li>{@link #clusterConnectionInfo} is a {@link ByteArrayResource} containing Kube Config file to connect to the kubernetes cluster.
 * </ul>
 */
@Data
@Builder
public class UpdateWorkloadDto {
    private WorkloadInstancePutRequestDto workloadInstancePutRequestDto;
    private ByteArrayResource helmSource;
    private ByteArrayResource values;
    private ByteArrayResource clusterConnectionInfo;

    /**
     * Builder class for {@link UpdateWorkloadDto}.
     */
    public static class UpdateWorkloadDtoBuilder {
        public UpdateWorkloadDto.UpdateWorkloadDtoBuilder workloadInstancePutRequestDto(
                final WorkloadInstancePutRequestDto workloadInstancePutRequestDto) {
            this.workloadInstancePutRequestDto = workloadInstancePutRequestDto;
            return this;
        }

        public UpdateWorkloadDto.UpdateWorkloadDtoBuilder helmSource(final ByteArrayResource helmSource) {
            this.helmSource = new FileNameAwareByteArrayResource(HELM_SOURCE_FILE, helmSource.getByteArray(), HELM_SOURCE_FILE);
            return this;
        }

        public UpdateWorkloadDto.UpdateWorkloadDtoBuilder clusterConnectionInfo(final ByteArrayResource clusterConnectionInfo) {
            this.clusterConnectionInfo = new FileNameAwareByteArrayResource("clusterConnectionInfo", clusterConnectionInfo.getByteArray());
            return this;
        }

        public UpdateWorkloadDto.UpdateWorkloadDtoBuilder values(final ByteArrayResource values) {
            this.values = new FileNameAwareByteArrayResource(HELM_VALUES_FILE, values.getByteArray(), HELM_VALUES_FILE);
            return this;
        }

        public UpdateWorkloadDto.UpdateWorkloadDtoBuilder defaultValues() {
            values = ResourceLoaderUtils.getClasspathResourceAsFileNameAwareByteArrayResource("default_helm_orchestrator_values/default.yaml",
                HELM_VALUES_FILE);
            return this;
        }
    }
}
