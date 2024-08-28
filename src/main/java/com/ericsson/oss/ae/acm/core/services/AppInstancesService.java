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

package com.ericsson.oss.ae.acm.core.services;

import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceItems;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

public interface AppInstancesService {

    /**
     * Get all app instances for appId
     *
     * @param appId  The App ID
     * @return AppInstanceItems containing a list of App Instances
     */
    AppInstanceItems getAppInstances(final String appId);

    /**
     * Get the app instance for appId and appInstanceId
     *
     * @param appInstanceId - The App Instance ID
     * @return AppInstance
     */
    AppInstance getAppInstanceById(final String appInstanceId);

    /**
     * Creates app instance
     *
     * @param createAppInstanceRequest - Create App Instance request
     * @return AppInstance
     */
    AppInstance createAppInstance(final CreateAppInstanceRequest createAppInstanceRequest);

    /**
     * Deploy, undeploy or upgrade a given App Instance based on the action specified
     *
     * @param instanceId - ID of the App Instance to be deployed, undeployed or upgraded
     * @param appInstanceManagementRequest - Type of action to be performed, Deploy, Undeploy or Upgrade
     * @return AppInstance
     */
    AppInstanceManagementResponse manageAppInstance(final String instanceId, final AppInstanceManagementRequest appInstanceManagementRequest);

    /**
     * Delete an app instance by instance ID
     *
     * @param instanceId - Instance ID of the App Instance to be deleted
     * @return AppInstanceOperationResponse for the app instance deleted
     */
    AppInstanceOperationResponse deleteAppInstance(final String instanceId);

    /**
     * Update an app instance by appInstanceId
     *
     * @param appInstanceId             - App Instance Id
     * @param updateAppInstanceRequest  - Update App Instance Request
     * @return AppInstanceOperationResponse
     */
    AppInstanceUpdateResponse updateAppInstance(final String appInstanceId, final UpdateAppInstanceRequest updateAppInstanceRequest);

}
