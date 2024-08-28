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

package com.ericsson.oss.ae.clients.apponboarding.model;

/**
 * Enum for an app's artifact type from app onboarding.
 * <p>
 * <li>HELM {@link #HELM} - This type is representing a helm chart.
 * <li>IMAGE {@link #IMAGE} - This type is representing a docker image.
 */
public enum ArtifactType {
    HELM, IMAGE
}