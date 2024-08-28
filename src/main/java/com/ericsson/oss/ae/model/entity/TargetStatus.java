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
 * Enum for an app's target status.
 * <p>
 *  <li>INSTANTIATED {@link #INSTANTIATED} - The target status of an app is that it will become instantiated.
 *  <li>TERMINATED {@link #TERMINATED} - The target status of an app is that it will become terminated.
 *  <li>DELETED {@link #DELETED} - The target status of an app is that it will become deleted.
 */
public enum TargetStatus {
    INSTANTIATED, TERMINATED, DELETED, APP_DELETED
}