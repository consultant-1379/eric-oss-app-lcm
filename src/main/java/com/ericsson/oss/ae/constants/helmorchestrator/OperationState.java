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
 * Enum for an app's operation state from helm orchestrator.
 * <p>
 * <li>PROCESSING {@link #PROCESSING} - The operation is still being processed by helm orchestrator.
 * <li>COMPLETED {@link #COMPLETED} - The operation is completed successfully by helm orchestrator.
 * <li>FAILED {@link #FAILED} - The operation has failed.
 * <li>INVALID {@link #INVALID} - The operation is invalid.
 */

public enum OperationState {
    PROCESSING, COMPLETED, FAILED, INVALID
}
