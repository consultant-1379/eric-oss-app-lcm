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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Representation of an AutomationCompositionDefinition.
 *
 * Note that fields sent in the acm payload, other that those needed for LCM use cases are ignored when marshalling the response
 * body into an AutomationCompositionDefinition instance.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationCompositionDefinition {

    @NonNull
    private UUID compositionId;

    @NonNull
    private AcTypeState state;

    private StateChangeResult stateChangeResult;

    @NonNull
    private Map<String, NodeTemplateState> elementStateMap = new HashMap<>();

}
