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

package com.ericsson.oss.ae.acm.core.validation.statemachine;

/**
 * Model class for app statuses.
 */
public enum AppStatus implements EntityStatus {

    CREATED,
    INITIALIZING,
    INITIALIZED,
    INITIALIZE_ERROR,
    DEINITIALIZING,
    DEINITIALIZED,
    DEINITIALIZE_ERROR,
    DELETE_ERROR;

    @Override
    public String toString() {
        return name();
    }
}