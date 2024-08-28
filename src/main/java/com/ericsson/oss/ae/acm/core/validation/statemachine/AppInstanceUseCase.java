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
 * Class to map app instance use cases with their allowed instance statuses and app modes
 */
public enum AppInstanceUseCase implements UseCase {

    CREATE,
    DEPLOY,
    UNDEPLOY,
    UPDATE,
    UPGRADE,
    DELETE;
}
