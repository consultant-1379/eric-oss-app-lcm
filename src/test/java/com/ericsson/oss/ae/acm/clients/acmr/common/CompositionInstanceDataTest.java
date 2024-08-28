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

package com.ericsson.oss.ae.acm.clients.acmr.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;

public class CompositionInstanceDataTest {
    @Test
    public void testCompositionInstanceDataConstructor_Assignments() {
        // Given
        AppInstances appInstances = new AppInstances();

        // When
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances, new ArrayList<>());

        // Then - Check constructor assignments
        assertEquals(appInstances, compositionInstanceData.getAppInstance());
    }

    @Test
    public void testCompositionInstanceData_EmptyComponentInstancesProperties() {
        // Given
        AppInstances appInstances = new AppInstances();

        // When
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances, new ArrayList<>());

        // Then - Check empty componentInstancesProperties map
        assertTrue(compositionInstanceData.getComponentInstancesProperties().isEmpty());
    }

}
