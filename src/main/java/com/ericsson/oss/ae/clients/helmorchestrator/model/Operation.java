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

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class used for representing a response from Helm Orchestrator's "/operations" endpoint.
 * <p>
 *  * Operation Dto {@link com.ericsson.oss.management.lcm.api.model.OperationDto} is mapped to this model class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Operation {

    private String operationId;
    private String workloadInstanceId;
    private String state;
    private String type;
    private LocalDateTime startTime;
}