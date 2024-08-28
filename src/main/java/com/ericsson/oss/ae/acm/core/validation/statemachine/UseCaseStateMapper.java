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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UseCaseStateMapper {

    private static final Map<UseCase, List<AppMode>> validModes = new HashMap<>();
    private static final Map<UseCase, List<EntityStatus>> validStatuses = new HashMap<>();

    static {
        setupAppUseCaseStateMapping();
        setupAppInstanceUseCaseStateMapping();
    }

    private static void setupAppUseCaseStateMapping() {
        validModes.put(AppUseCase.INITIALIZE, Arrays.asList(AppMode.DISABLED));
        validModes.put(AppUseCase.ENABLE, Arrays.asList(AppMode.DISABLED));
        validModes.put(AppUseCase.DISABLE, Arrays.asList(AppMode.ENABLED));
        validModes.put(AppUseCase.DEINITIALIZE, Arrays.asList(AppMode.DISABLED));
        validModes.put(AppUseCase.DELETE, Arrays.asList(AppMode.DISABLED));

        validStatuses.put(AppUseCase.INITIALIZE, Arrays.asList(AppStatus.CREATED, AppStatus.INITIALIZE_ERROR));
        validStatuses.put(AppUseCase.ENABLE, Arrays.asList(AppStatus.INITIALIZED));
        validStatuses.put(AppUseCase.DISABLE, Arrays.asList(AppStatus.INITIALIZED));
        validStatuses.put(AppUseCase.DEINITIALIZE, Arrays.asList(AppStatus.INITIALIZED, AppStatus.INITIALIZE_ERROR, AppStatus.DEINITIALIZE_ERROR));
        validStatuses.put(AppUseCase.DELETE, Arrays.asList(AppStatus.CREATED, AppStatus.DEINITIALIZED, AppStatus.DELETE_ERROR));
    }

    private static void setupAppInstanceUseCaseStateMapping() {
        validModes.put(AppInstanceUseCase.CREATE, Arrays.asList(AppMode.ENABLED));

        validStatuses.put(AppInstanceUseCase.DEPLOY, Arrays.asList(AppInstanceStatus.UNDEPLOYED));
        validStatuses.put(AppInstanceUseCase.UNDEPLOY,
            Arrays.asList(AppInstanceStatus.DEPLOYED, AppInstanceStatus.DEPLOY_ERROR, AppInstanceStatus.UNDEPLOY_ERROR,
                AppInstanceStatus.UPDATE_ERROR));
        validStatuses.put(AppInstanceUseCase.UPDATE, Arrays.asList(AppInstanceStatus.UNDEPLOYED, AppInstanceStatus.DEPLOYED, AppInstanceStatus.UPDATE_ERROR));
        validStatuses.put(AppInstanceUseCase.UPGRADE, Arrays.asList(AppInstanceStatus.DEPLOYED));
        validStatuses.put(AppInstanceUseCase.DELETE, Arrays.asList(AppInstanceStatus.UNDEPLOYED, AppInstanceStatus.DELETE_ERROR));
    }

    public static List<AppMode> getValidModesByUseCase(final UseCase useCase) {
        List<AppMode> appModes = validModes.get(useCase);
        return appModes == null ? new ArrayList<>() : appModes;
    }

    public static List<EntityStatus> getValidStatusesByUseCase(final UseCase useCase) {
        List<EntityStatus> appStatuses = validStatuses.get(useCase);
        return appStatuses == null ? new ArrayList<>() : appStatuses;
    }
}
