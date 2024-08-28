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

package com.ericsson.oss.ae.constants.helmorchestrator;

/**
 * Enum for an app's operation type from helm orchestrator.
 * <p>
 * <li>INSTANTIATE {@link #INSTANTIATE} - Operation wants to instantiate an app.
 * <li>UPDATE {@link #UPDATE} - Operation wants to update an app.
 * <li>REINSTANTIATE {@link #REINSTANTIATE} - Operation wants to re-instantiate an app.
 * <li>ROLLBACK {@link #ROLLBACK} - Operation wants to roll back an app.
 * <li>TERMINATE {@link #TERMINATE} - Operation wants to terminate an app.
 */
public enum OperationType {
    INSTANTIATE, UPDATE, REINSTANTIATE, ROLLBACK, TERMINATE, PENDING
}