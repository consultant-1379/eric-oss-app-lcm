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

import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_SOURCE_FILE;
import static com.ericsson.oss.ae.constants.helmorchestrator.HelmOrchestratorConstants.HELM_VALUES_FILE;

import org.springframework.core.io.ByteArrayResource;

import com.ericsson.oss.ae.utils.file.FileNameAwareByteArrayResource;
import com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO class used to model requests to helm orchestrator for app instantiation. It handles the files values.yaml, helm source and cluster connection
 * info as ByteArrayResource objects.
 * <p>
 * <ul>
 * <li>{@link WorkloadInstancePostRequestDto} is imported from helm orchestrator project. It is a request object, where user can set parameters to
 * describe workload instance.</li>
 * <li>{@link #helmSource} is a {@link ByteArrayResource} containing the helm charts of the application.
 * <li>{@link #values} is a {@link ByteArrayResource} containing the values.yaml file required to deploy an application.
 * <li>{@link #clusterConnectionInfo} is a {@link ByteArrayResource} containing Kube Config file to connect to the kubernetes cluster.
 * </ul>
 */
@Data
@Builder
public final class InstantiateWorkloadDto {

    private WorkloadInstancePostRequestDto workloadInstancePostRequestDto;
    private ByteArrayResource helmSource;
    private ByteArrayResource values;
    private ByteArrayResource clusterConnectionInfo;

    /**
     * Builder class for {@link InstantiateWorkloadDto}.
     */
    public static class InstantiateWorkloadDtoBuilder {
        public InstantiateWorkloadDtoBuilder workloadInstancePostRequestDto(final WorkloadInstancePostRequestDto workloadInstancePostRequestDto) {
            this.workloadInstancePostRequestDto = workloadInstancePostRequestDto;
            return this;
        }

        public InstantiateWorkloadDtoBuilder helmSource(final ByteArrayResource helmSource) {
            this.helmSource = new FileNameAwareByteArrayResource(HELM_SOURCE_FILE, helmSource.getByteArray(), HELM_SOURCE_FILE);
            return this;
        }

        public InstantiateWorkloadDtoBuilder clusterConnectionInfo(final ByteArrayResource clusterConnectionInfo) {
            this.clusterConnectionInfo = new FileNameAwareByteArrayResource("clusterConnectionInfo", clusterConnectionInfo.getByteArray());
            return this;
        }

        public InstantiateWorkloadDtoBuilder values(final ByteArrayResource values) {
            this.values = new FileNameAwareByteArrayResource(HELM_VALUES_FILE, values.getByteArray(), HELM_VALUES_FILE);
            return this;
        }

        public InstantiateWorkloadDtoBuilder defaultValues() {
            values = ResourceLoaderUtils.getClasspathResourceAsFileNameAwareByteArrayResource("default_helm_orchestrator_values/default.yaml",
                    HELM_VALUES_FILE);
            return this;
        }
    }
}