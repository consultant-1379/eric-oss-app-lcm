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

import com.ericsson.oss.ae.acm.presentation.filter.GetAppsFilter;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppInitializeOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppItems;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponse;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.EnableDisableAppRequest;
import com.ericsson.oss.ae.v3.api.model.InitializeActionRequest;

public interface AppService {

    /**
     * Create app using createAppRequest
     *
     * @param createAppRequest - createAppRequest
     * @return AppDetails
     */
    AppDetails createApp(final CreateAppRequest createAppRequest);

    /**
     * Get all Apps
     *
     * @param getAppsFilter - get Apps filter (name, version, mode, status, type, offset, limit)
     * @return List of Apps
     */
    AppItems getApps(final GetAppsFilter getAppsFilter);

    /**
     * Get App for specific appId
     *
     * @param appId - The App ID
     * @return AppDetails
     */
    AppDetails getAppById(final String appId);

    /**
     * Execute a specified action for the App. Used to Enable or Disable the App.
     *
     * @param appId - The App ID
     * @param enableDisableAppRequest - enable/disable request
     * @return  AppDetails
     */
    AppOperationResponse enableDisableApp(final String appId, final EnableDisableAppRequest enableDisableAppRequest);

    /**
     * Delete app by app ID
     *
     * @param appId - App ID of the App to be deleted
     */
    void deleteAppById(final String appId);

    /**
     * Execute action to initialize an App
     *
     * @param appId - App ID of the App to be initialized
     * @param initializeActionRequest - Initialize app action request
     * @return the AppInitializeOperationResponse instance
     */
    AppInitializeOperationResponse executeInitializeAction(final String appId, final InitializeActionRequest initializeActionRequest);
}
