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

package com.ericsson.oss.ae.utils.validator;

import com.ericsson.oss.ae.api.model.AppInstanceListRequestDto;
import com.ericsson.oss.ae.api.model.MultiDeleteFailureDetails;
import com.ericsson.oss.ae.constants.AppLcmConstants;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.*;

/**
 * The type Instance validator to validate instances.
 */
@Slf4j
@Component
public class InstanceValidator {

    /**
     * Validate app instances for app instance deletion
     *
     * @param appInstanceList the app instance list
     * @return list
     */
    public List<MultiDeleteFailureDetails> validateAppInstancesForDeletion(List<AppInstance> appInstanceList) {
        log.info("Validate App Instances For Deletion");
        ArrayList<MultiDeleteFailureDetails> errorList = new ArrayList<>();
        Comparator<AppInstance> comparatorById = Comparator.comparing(AppInstance::getId);
        Collections.sort(appInstanceList, comparatorById);
        appInstanceList.forEach(instance -> validateInstance(instance, errorList));
        return errorList;
    }

    private void validateInstance(AppInstance appInstance, ArrayList<MultiDeleteFailureDetails> errorList) {
        log.debug("Validate Instances id: {}, App ID: {}", appInstance.getId(), appInstance.getAppOnBoardingAppId());
        if(AppLcmConstants.getUnhealthyStatusList().contains(appInstance.getHealthStatus())){
            validateArtifact(appInstance.getArtifactInstances(), appInstance.getId(), errorList);
        }else{
            addErrorMessageForInstanceNotTerminatedFailed(appInstance.getId(), errorList);
        }
    }

    private void validateArtifact(List<ArtifactInstance> artifactInstanceList, Long instanceId, ArrayList<MultiDeleteFailureDetails> errorList) {
        log.debug("Validate Artifact for instance Id: {}", instanceId);
        if(artifactInstanceList != null && !artifactInstanceList.isEmpty()){
            artifactInstanceList.stream()
                .filter(artifactInstance -> !AppLcmConstants.getUnhealthyStatusList().contains(artifactInstance.getHealthStatus()))
                .findAny().ifPresent(artifact ->
                                         errorList.add(getMultiDeleteFailureDetails(instanceId,
                                                                                    ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(),
                                                                                    ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage()))
                );
        }
    }

    private void addErrorMessageForInstanceNotTerminatedFailed(Long instanceId, ArrayList<MultiDeleteFailureDetails> errorList) {
        log.debug("Add Error Message For Instance Id {}, Not Terminated Failed", instanceId);
        errorList.add(getMultiDeleteFailureDetails(instanceId, APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(),
                                                   APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage()));
    }

    private MultiDeleteFailureDetails getMultiDeleteFailureDetails(Long instanceId, int errorCode, String errorMessage) {
        log.debug("getMultiDeleteFailureDetails - instanceId: {}, errorCode: {}, errorMessage: {}", instanceId, errorCode, errorMessage);
        MultiDeleteFailureDetails errorDetails = new MultiDeleteFailureDetails();
        errorDetails.setAppInstanceId(instanceId);
        errorDetails.setAppLcmErrorCode(errorCode);
        errorDetails.setFailureMessage(errorMessage);
        return errorDetails;
    }

    /**
     * Method to compare list requested to delete from AppInstanceListRequestDto,
     * and list of instances found in App-Lcm DB
     *
     * @param instanceListRequestDto the instance list request dto
     * @param appInstanceList        the app instance list
     * @return list
     */
    public List<MultiDeleteFailureDetails> compareTwoList(AppInstanceListRequestDto instanceListRequestDto,
                                                          List<AppInstance> appInstanceList) {
        log.info("Compare Two List");
        List<MultiDeleteFailureDetails> errorList = new ArrayList<>();

        instanceListRequestDto.getAppInstanceId().forEach(
            id -> {
                if(appInstanceList.stream()
                    .filter(instance -> instance.getId().equals(id))
                    .collect(Collectors.toList()).isEmpty()){
                    errorList.add(getMultiDeleteFailureDetails(id, SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode(),
                                                               SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage()));
                }
            }
        );
        return errorList;
    }

}
