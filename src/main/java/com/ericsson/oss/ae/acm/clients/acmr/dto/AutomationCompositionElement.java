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
package com.ericsson.oss.ae.acm.clients.acmr.dto;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationCompositionElement {

    @NonNull
    private UUID id;

    /**
     * Identifier for the related Automation Composition Element definition in the tosca service template for the AC Type
     */
    @NonNull
    private ToscaIdentifier definition;

    @NonNull
    private UUID participantId;

    @NonNull
    private DeployState deployState;

    @NonNull
    private LockState lockState;

    private StateChangeResult stateChangeResult;
    private String message;
    private Map<String, Object> properties;
}
