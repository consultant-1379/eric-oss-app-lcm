/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.presentation.services.appinstance;

import com.ericsson.oss.ae.api.model.*;
import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.presentation.enums.Version;

import java.util.List;

/**
 * Interface for App Instance Service.
 */
public interface AppInstanceService {

    AppInstanceDto create(AppInstancePostRequestDto appInstancePostRequestDto);

    AppInstanceDto getAppInstance(Long appInstanceId);

    AppInstanceDto getAppInstance(Long appInstanceId, Version version);

    AppInstancesDto getAllAppInstances(Long id);

    AppInstancesDto getAllAppInstances(Long id, Version version);

    void terminate(Long appInstanceId);

    AppInstanceDto updateAppInstance(AppInstancePutRequestDto appInstancePutRequestDto);

    void deleteAppInstances(Long appId, AppInstanceListRequestDto instanceList);

    void deleteApp(Long appId);

    boolean deleteAppInstancesResources(Long appId, List<AppInstance> appInstanceList);

    List<AppInstance> getAllAppInstancesForRequestedFilter(AppInstanceFilter appInstanceRequest);
}
