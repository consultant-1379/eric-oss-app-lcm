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

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StateTransitionManagerTest {

    @Mock
    private AppRepository mockAppRepository;
    @Mock
    private AppInstancesRepository mockAppInstancesRepository;

    private StateTransitionValidator stateTransitionManagerUnderTest;

    @BeforeEach
    public void setUp() {
        stateTransitionManagerUnderTest = new StateTransitionValidator(mockAppRepository, mockAppInstancesRepository);
    }

    @Test
    public void testValidateAppState() {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
        appUnderTest.setId(UUID.randomUUID());
        final Optional<App> app = Optional.of(appUnderTest);
        when(mockAppRepository.findById(appUnderTest.getId())).thenReturn(app);

        // Run the test
        stateTransitionManagerUnderTest.validateAppState(appUnderTest.getId(), AppUseCase.INITIALIZE);

        assertThat(stateTransitionManagerUnderTest).isNotNull();
    }

    @Test
    public void testValidateAppState_Mode_Not_Matching() {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        appUnderTest.setId(UUID.randomUUID());
        final Optional<App> app = Optional.of(appUnderTest);
        when(mockAppRepository.findById(appUnderTest.getId())).thenReturn(app);

        assertThatThrownBy(() -> stateTransitionManagerUnderTest.validateAppState(
                appUnderTest.getId(), AppUseCase.DELETE))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateAppState_Status_Not_Matching() {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZE_ERROR);
        appUnderTest.setId(UUID.randomUUID());
        final Optional<App> app = Optional.of(appUnderTest);
        when(mockAppRepository.findById(appUnderTest.getId())).thenReturn(app);

        assertThatThrownBy(() -> stateTransitionManagerUnderTest.validateAppState(
                appUnderTest.getId(), AppUseCase.ENABLE))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateAppState_AppRepositoryReturnsAbsent() {
        // Setup
        final UUID appId = UUID.randomUUID();
        when(mockAppRepository.findById(appId))
                .thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> stateTransitionManagerUnderTest.validateAppState(appId, AppUseCase.INITIALIZE))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateAppInstanceState() {
        final AppInstances appInstance = TestUtils.createAppInstance();
        appInstance.setId(UUID.randomUUID());
        appInstance.setStatus(AppInstanceStatus.UNDEPLOYED);

        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        appUnderTest.setId(UUID.randomUUID());
        appInstance.setApp(appUnderTest);

        final Optional<AppInstances> appInstances = Optional.of(appInstance);
        when(mockAppInstancesRepository.findById(appInstance.getId()))
                .thenReturn(appInstances);

        // Run the test
        stateTransitionManagerUnderTest.validateAppInstanceState(appInstance.getId(), AppInstanceUseCase.DEPLOY);

        assertThat(stateTransitionManagerUnderTest).isNotNull();
    }

    @Test
    public void testValidateAppInstanceState_AppId_Not_Null() {
        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        appUnderTest.setId(UUID.randomUUID());
        final Optional<App> app = Optional.of(appUnderTest);
        when(mockAppRepository.findById(appUnderTest.getId())).thenReturn(app);

        // Run the test
        stateTransitionManagerUnderTest.validateAppInstanceState(appUnderTest.getId(), AppInstanceUseCase.CREATE);
        assertThat(stateTransitionManagerUnderTest).isNotNull();
    }

    @Test
    public void testValidateAppInstanceState_AppId_Doesnot_Exists() {
        final UUID appId = UUID.randomUUID();
        when(mockAppRepository.findById(appId)).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> stateTransitionManagerUnderTest.validateAppInstanceState(appId,
                AppInstanceUseCase.CREATE)).isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateAppInstanceState_Status_Not_Matching() {
        final AppInstances appInstance = TestUtils.createAppInstance();
        appInstance.setId(UUID.randomUUID());
        appInstance.setStatus(AppInstanceStatus.DEPLOYED);

        final App appUnderTest = TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        appUnderTest.setId(UUID.randomUUID());
        appInstance.setApp(appUnderTest);

        final Optional<AppInstances> appInstances = Optional.of(appInstance);
        when(mockAppInstancesRepository.findById(appInstance.getId()))
                .thenReturn(appInstances);

        // Run the test
        assertThatThrownBy(() -> stateTransitionManagerUnderTest
                .validateAppInstanceState(appInstance.getId(), AppInstanceUseCase.DELETE))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateAppInstanceState_AppInstancesRepositoryReturnsAbsent() {
        // Setup
        final UUID appInstanceId = UUID.randomUUID();
        when(mockAppInstancesRepository.findById(appInstanceId))
                .thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> stateTransitionManagerUnderTest.validateAppInstanceState(appInstanceId,
                AppInstanceUseCase.DEPLOY)).isInstanceOf(AppLcmException.class);
    }
}
