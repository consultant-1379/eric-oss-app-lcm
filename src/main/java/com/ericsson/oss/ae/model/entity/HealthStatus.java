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

package com.ericsson.oss.ae.model.entity;

/**
 * Enum for an app's health status.
 * <p>
 * <li>INSTANTIATED {@link #INSTANTIATED} - The health status of an app is instantiated.
 * <li>TERMINATED {@link #TERMINATED} - The health status of an app is terminated.
 * <li>FAILED {@link #FAILED} - The health status of an app is failed.
 * <li>PENDING {@link #PENDING} - The health status of an app is pending.
 * <li>DELETED {@link #DELETED} - The health status of an app is deleted.
 */

public enum HealthStatus {
    INSTANTIATED, TERMINATED, FAILED, PENDING, DELETED , CREATED, DELETING;
}
