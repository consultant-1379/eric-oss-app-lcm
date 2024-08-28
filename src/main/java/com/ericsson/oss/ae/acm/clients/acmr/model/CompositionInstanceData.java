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

package com.ericsson.oss.ae.acm.clients.acmr.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;

/**
 * Class for a Composition Instance Data.
 */
@Getter
@ToString
@AllArgsConstructor
public class CompositionInstanceData {
    private final AppInstances appInstance;
    private final List<ComponentInstances> componentInstancesProperties;

    // Constructor with AppInstance
    public CompositionInstanceData(AppInstances instances) {
        this.appInstance = instances;
        this.componentInstancesProperties = new ArrayList<>();
    }
}