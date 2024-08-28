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

package com.ericsson.oss.ae.clients.helmorchestrator.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class used for representing a Workload Instance from Helm Orchestrator.
 * <p>
 * * Workload Instance Dto {@link com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto} can be mapped to this model class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkloadInstance {

    private String workloadInstanceId;
    @Default
    private String workloadInstanceName = UUID.randomUUID().toString();
    private String namespace;
    private String crdNamespace;
    private String cluster;
    private List<String> helmSourceVersions;
    private Integer version;
    private Map<String, Object> additionalParameters;
}
