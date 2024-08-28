/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

package com.ericsson.oss.ae.acm.core.services.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@ExtendWith(MockitoExtension.class)
public class DeployActionHandlerTest {

    @Mock
    private AcmService mockAcmService;
    @Mock
    private AppInstancesRepository mockAppInstancesRepository;

    private DeployActionHandler deployActionHandlerUnderTest;

    @BeforeEach
    public void setUp() {
        deployActionHandlerUnderTest = new DeployActionHandler(mockAcmService, mockAppInstancesRepository);
    }

    @Test
    public void testDeployAppInstance() {
        // Setup
        final App appEntity = getAppDetailsForDeploy();
        final AppInstances appInstancesUnderTest = appEntity.getAppInstances().get(0);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        // Run the test
        deployActionHandlerUnderTest.deployAppInstance(appInstancesUnderTest, deployRequest);

        // Verify the results
        verify(mockAcmService).deployAutomationCompositionInstance(
                appEntity.getCompositionId(),
                appInstancesUnderTest.getCompositionInstanceId());
    }

    @Test
    public void testDeployAppInstance_withNoUpdateInstanceProperties() {
        // Setup
        final App appEntity = getAppDetailsForDeploy();
        final AppInstances appInstancesUnderTest = appEntity.getAppInstances().get(0);

        final AppInstanceManagementRequest deployRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(null);

        // Run the test
        deployActionHandlerUnderTest.deployAppInstance(appInstancesUnderTest, deployRequest);

        // Verify the results
        verify(mockAcmService).deployAutomationCompositionInstance(
                appEntity.getCompositionId(),
                appInstancesUnderTest.getCompositionInstanceId());
    }

    @Test
    public void testDeployAppInstance_AcmServiceDeployAutomationCompositionInstanceThrowsRestRequestFailedException() {
        // Setup
        final App appEntity = getAppDetailsForDeploy();
        final AppInstances appInstancesUnderTest = appEntity.getAppInstances().get(0);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.UNDEPLOYED)));
        final AppInstanceManagementRequest deployRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(additionalData);

        doThrow(RestRequestFailedException.class).when(mockAcmService).deployAutomationCompositionInstance(
                appEntity.getCompositionId(),
                appInstancesUnderTest.getCompositionInstanceId());

        // Run the test
        assertThatThrownBy(
                () -> deployActionHandlerUnderTest.deployAppInstance(appInstancesUnderTest, deployRequest))
                .isInstanceOf(AppLcmException.class);
    }

    private App getAppDetailsForDeploy(){
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setId(UUID.randomUUID());
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        appEntity.setCompositionId(UUID.randomUUID());
        appEntity.getAppComponents().get(0).setId(UUID.randomUUID());

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(appEntity);
        appInstancesUnderTest.setId(UUID.randomUUID());
        appInstancesUnderTest.setCompositionInstanceId(UUID.randomUUID());
        appInstancesUnderTest.getAppComponentInstances().get(0).setId(UUID.randomUUID());
        appEntity.setAppInstances(List.of(appInstancesUnderTest));
        return appEntity;
    }

}
