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
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InstanceValidatorTest {

    @Autowired
    private InstanceValidator classValidatorUnderTest;
    private List<MultiDeleteFailureDetails>objectUnderTest;

    @Test
    void givenValidAppInstanceList_WhenValidateAppResources_ThenReturnEmptyErrorList() {

        final List<AppInstance> instanceList = getAppInstanceList(3, 1, false, null);

        final List<MultiDeleteFailureDetails>objectUnderTest = classValidatorUnderTest
            .validateAppInstancesForDeletion(instanceList);

        assertEquals(0, objectUnderTest.size());
    }

    @Test
    void givenUnhealthyAppInstanceInList_WhenValidateAppResources_ThenReturnErrorListForInstancesNotTerminatedNotFailed() {
        int numOfInstances = 3;
        final List<AppInstance> instanceList = getAppInstanceList(numOfInstances, 1, true, null);

        final List<MultiDeleteFailureDetails>objectUnderTest = classValidatorUnderTest
            .validateAppInstancesForDeletion(instanceList);

        assertEquals(numOfInstances, objectUnderTest.size());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(0).getAppLcmErrorCode());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(0).getFailureMessage());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(1).getAppLcmErrorCode());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(1).getFailureMessage());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(2).getAppLcmErrorCode());
        assertEquals(APP_LCM_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(2).getFailureMessage());
    }

    @Test
    void givenUnhealthyArtifactsInList_WhenValidateAppResources_ThenReturnErrorListForArtifactsNotTerminatedNotFailed() {
        int numOfInstances = 3;
        final List<AppInstance> instanceList = getAppInstanceList(numOfInstances, 1, false, true);

        final List<MultiDeleteFailureDetails>objectUnderTest = classValidatorUnderTest
            .validateAppInstancesForDeletion(instanceList);

        assertEquals(numOfInstances, objectUnderTest.size());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(0).getAppLcmErrorCode());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(0).getFailureMessage());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(1).getAppLcmErrorCode());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(1).getFailureMessage());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorCode(), objectUnderTest.get(2).getAppLcmErrorCode());
        assertEquals(ARTIFACT_INSTANCE_NOT_TERMINATED_NOT_FAILED.getErrorMessage(), objectUnderTest.get(2).getFailureMessage());
    }

    @Test
    void givenDifferentLists_WhenCompareTwoList_ThenReturnErrorList(){
        final int numOfInstances = 3;
        final List<AppInstance> instanceList = getAppInstanceList(numOfInstances, HealthStatus.TERMINATED);
        final AppInstanceListRequestDto instanceListRequestDto = getAppInstanceListRequestDto(6);

        final List<MultiDeleteFailureDetails>objectUnderTest = classValidatorUnderTest
            .compareTwoList(instanceListRequestDto, instanceList);

        assertEquals(true, !objectUnderTest.isEmpty());
        assertEquals(3, objectUnderTest.size());
        assertEquals(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage(), objectUnderTest.get(0).getFailureMessage());
        assertEquals(SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode(), objectUnderTest.get(0).getAppLcmErrorCode());
    }

    @Test
    void givenTwoValidLists_WhenCompareTwoList_ThenReturnEmptyErrorList(){
        final int numOfInstances = 3;
        final List<AppInstance> instanceList = getAppInstanceList(numOfInstances, HealthStatus.TERMINATED);
        final AppInstanceListRequestDto instanceListRequestDto = getAppInstanceListRequestDto(3);

        final List<MultiDeleteFailureDetails>objectUnderTest = classValidatorUnderTest
            .compareTwoList(instanceListRequestDto, instanceList);

        assertEquals(true, objectUnderTest.isEmpty());
    }

    private AppInstanceListRequestDto getAppInstanceListRequestDto(int numOfInstances){
        AppInstanceListRequestDto instanceListRequestDto = new AppInstanceListRequestDto();
        final List<Long> instances = new ArrayList<>();
        for(long i = 1; i <= numOfInstances; i++){
            instances.add(i);
        }
        instanceListRequestDto.setAppInstanceId(instances);
        return instanceListRequestDto;
    }

    private List<AppInstance> getAppInstanceList(int numOfInstances, HealthStatus status) {
        final List<AppInstance> appInstanceList = new ArrayList<>();
        for(long i = 1; i <= numOfInstances; i++){
            AppInstance appInstance = new AppInstance();
            appInstance.setId(i);
            appInstance.setAppOnBoardingAppId(1L);
            appInstance.setHealthStatus(status);
            appInstanceList.add(appInstance);
        }
        return appInstanceList;
    }

    private List<AppInstance> getAppInstanceList(int numOfInstances, long appId, boolean isActiveInstanceHealth, Boolean isActiveArtifactHealth) {
        final List<AppInstance> appInstanceList = new ArrayList<>(numOfInstances);

        for(long i = 1; i <= numOfInstances; i++){
            final HealthStatus instanceHealthStatus = getHealthStatus((int) i, isActiveInstanceHealth);
            final HealthStatus artifactHealthStatus = isActiveArtifactHealth != null ?
                getHealthStatus((int) i, isActiveArtifactHealth) : instanceHealthStatus;
            final ArtifactInstance artifactInstance = ArtifactInstance.builder().appOnBoardingArtifactId(appId).workloadInstanceId("workloadId").build();
            final AppInstance appInstance = AppInstance.builder().appOnBoardingAppId(appId).build();
            appInstance.setId(i);
            appInstance.setHealthStatus(instanceHealthStatus);
            artifactInstance.setAppInstance(appInstance);
            artifactInstance.setId(i+numOfInstances);
            artifactInstance.setHealthStatus(artifactHealthStatus);
            appInstance.setArtifactInstances(Arrays.asList(artifactInstance));
            appInstanceList.add(appInstance);
        }
        return appInstanceList;
    }

    private HealthStatus getHealthStatus(int i, boolean isActiveHealth) {
        if(isActiveHealth){
            switch (i){
                case 1: return HealthStatus.INSTANTIATED;
                case 2: return HealthStatus.PENDING;
                default: return HealthStatus.DELETING;
            }
        }else {
            switch (i){
                case 1: return HealthStatus.TERMINATED;
                case 2: return HealthStatus.FAILED;
                default: return HealthStatus.DELETED;
            }
        }
    }
}