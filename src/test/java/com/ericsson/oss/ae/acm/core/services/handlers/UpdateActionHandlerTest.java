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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.utils.MapperUtil;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@ExtendWith(MockitoExtension.class)
public class UpdateActionHandlerTest {

    @Mock
    private AcmService mockAcmService;
    @Mock
    private KeycloakHandler mockKeycloakHandler;
    @Mock
    private AcmFileGenerator mockAcmFileGenerator;
    @Mock
    private MapperUtil mockMapperUtil;

    private UpdateActionHandler updateActionHandlerUnderTest;

    AppInstances appInstancesUnderTest;

    App appEntity;

    @BeforeEach
    public void setUp() {
        updateActionHandlerUnderTest = new UpdateActionHandler(mockAcmService, mockAcmFileGenerator);
        appEntity = getAppDetailsForUpdate();
        appInstancesUnderTest = appEntity.getAppInstances().get(0);

    }

    @Test
    public void testUpdateAppInstance() throws IOException {
        final String expectedAcmInstance = new String(
                Files.readAllBytes(Paths.get(
                        "src/test/resources/acmfiles/AutomationCompositionInstancePropertiesWithInstanceId.yaml"
                )));

        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any(CompositionInstanceData.class)))
                .thenReturn(expectedAcmInstance);

        // Run the test
        updateActionHandlerUnderTest.updateAppInstance(appInstancesUnderTest, List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.DEPLOYED)));

        // Verify the results
        verify(mockAcmService).updateAutomationCompositionInstance(expectedAcmInstance,
                appEntity.getCompositionId());
    }

    @Test
    public void testUpdateAppInstance_MapperUtilParsePropertyObjectToHashMapListReturnsNoItems() throws IOException {

        final String expectedAcmInstance = new String(
                Files.readAllBytes(Paths.get(
                        "src/test/resources/acmfiles/AutomationCompositionInstancePropertiesWithInstanceId.yaml"
                )));

        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any(CompositionInstanceData.class)))
                .thenReturn(expectedAcmInstance);

        // Run the test
        updateActionHandlerUnderTest.updateAppInstance(appInstancesUnderTest, List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.DEPLOYED)));

        // Verify the results
        verify(mockAcmService).updateAutomationCompositionInstance(expectedAcmInstance,
                appEntity.getCompositionId());

    }

    @Test
    public void testUpdateAppInstance_AcmFileGeneratorThrowsAppLcmException()  throws IOException {
        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any(CompositionInstanceData.class)))
                .thenThrow(AppLcmException.class);

        // Run the test
        assertThatThrownBy(
                () -> updateActionHandlerUnderTest.updateAppInstance(appInstancesUnderTest, List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.DEPLOYED))))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testUpdateAppInstance_AcmServiceUpdateAutomationCompositionInstanceThrowsRestRequestFailedException()  throws IOException {

        final String expectedAcmInstance = new String(
                Files.readAllBytes(Paths.get(
                        "src/test/resources/acmfiles/AutomationCompositionInstancePropertiesWithInstanceId.yaml"
                )));

        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any(CompositionInstanceData.class)))
                .thenReturn(expectedAcmInstance);

        when(mockAcmService.updateAutomationCompositionInstance(expectedAcmInstance,
                appEntity.getCompositionId())).thenThrow(RestRequestFailedException.class);

        // Run the test
        assertThatThrownBy(
                () -> updateActionHandlerUnderTest.updateAppInstance(appInstancesUnderTest, List.of(TestUtils.createAsdComponentInstance(appEntity, AppInstanceStatus.DEPLOYED))))
                .isInstanceOf(AppLcmException.class);
    }

    private App getAppDetailsForUpdate(){
        final App appEntity = TestUtils.generateAppEntity();
        appEntity.setId(UUID.fromString("478b6072-e32e-417f-84f3-f9a33fabaacc"));
        appEntity.setMode(AppMode.ENABLED);
        appEntity.setStatus(AppStatus.INITIALIZED);
        appEntity.setCompositionId(UUID.fromString("f24485ad-ad5f-4246-adfe-2a9743343c61"));
        appEntity.getAppComponents().get(0).setId(UUID.fromString("478b6072-e32e-417f-84f3-f9a33fabaadd"));

        final AppInstances appInstancesUnderTest = TestUtils.generateAppInstanceEntityForDeployInstance(appEntity);
        appInstancesUnderTest.setId(UUID.fromString("e2e6e484-6583-4b6d-896e-dc7650ef20b1"));
        appInstancesUnderTest.setCompositionInstanceId(UUID.fromString("fb9234bf-3d8a-442b-ac79-46d0e6124e0b"));
        appInstancesUnderTest.getAppComponentInstances().get(0).setId(UUID.fromString("478b6072-e32e-417f-84f3-f9a33fabaadd"));
        appEntity.setAppInstances(List.of(appInstancesUnderTest));
        return appEntity;
    }

}
