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

package com.ericsson.oss.ae.acm.presentation.mapper;

import static com.ericsson.oss.ae.acm.TestConstants.ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@ExtendWith(MockitoExtension.class)
class AppInstancesMapperTest {

    @Mock
    private ModelMapper mockModelMapper;
    @Mock
    private LcmUrlGenerator mockLcmUrlGenerator;

    private AppInstancesMapper appInstancesMapperUnderTest;

    @BeforeEach
    void setUp() {
        appInstancesMapperUnderTest = new AppInstancesMapper(mockModelMapper, mockLcmUrlGenerator);
    }

    @Test
    void testToAppInstance() {
        // Setup
        final App app = App.builder().build();

        // Create a UUID for the AppInstance
        final UUID appInstanceId = UUID.randomUUID();

        // Run the test
        final AppInstances result = appInstancesMapperUnderTest.toAppInstanceEntity(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED, app, AppInstanceStatus.UNDEPLOYED, appInstanceId);

        assertThat(result.getCompositionInstanceId()).isEqualTo(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED);
        assertThat(result.getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
    }

    @Test
    public void testGenerateAppInstanceEntity() {
        // Given
        UUID appInstanceId = UUID.randomUUID();
        final App app = App.builder().build();

        // When
        AppInstances appInstances = appInstancesMapperUnderTest.generateAppInstanceEntity(appInstanceId, app);

        // Then
        Assertions.assertNotNull(appInstances);
        Assertions.assertEquals(appInstanceId, appInstances.getId());
        Assertions.assertEquals(app, appInstances.getApp());
    }

    @Test
    public void testToAppInstanceUpgradeResponse() {
        // Given
        final App appEntity = TestUtils.generateAppEntity();
        final UUID appId = UUID.randomUUID();
        appEntity.setId(appId);
        final UUID appInstanceId = UUID.randomUUID();
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(appEntity);
        appInstancesUnderTest.setStatus(AppInstanceStatus.UPGRADING);
        appInstancesUnderTest.setId(appInstanceId);
        appInstancesUnderTest.setTargetApp(appEntity);
        appInstancesUnderTest.setApp(appEntity);
        appEntity.setAppInstances(List.of(appInstancesUnderTest));
        Mockito.when(mockLcmUrlGenerator.getAppsInstanceUrlById(String.valueOf(appInstanceId))).thenReturn("http://localhost:8080/v3/app-instances");
        // When
        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of());
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(appEntity.getId().toString())
                .additionalData(additionalData);
        final AppInstanceManagementResponse appInstanceUpgradeResponse = appInstancesMapperUnderTest.toAppInstanceManagementResponse(upgradeAppInstanceRequest, appInstancesUnderTest);

        // Then
        Assertions.assertNotNull(appInstanceUpgradeResponse);
        Assertions.assertNotNull(appInstanceUpgradeResponse.getTargetAppId());
        Assertions.assertNotNull(appInstanceUpgradeResponse.getAdditionalData());
    }
}
