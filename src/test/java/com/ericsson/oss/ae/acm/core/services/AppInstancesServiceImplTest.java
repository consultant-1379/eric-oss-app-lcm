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

import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22233;
import static com.ericsson.oss.ae.acm.TestConstants.EEFD_6C8CC;
import static com.ericsson.oss.ae.acm.TestConstants.ELEMENT_INSTANCE_DATA_MANAGEMENT;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.VERSION_2_0_0;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcInstanceResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionElement;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionInstance;
import com.ericsson.oss.ae.acm.clients.acmr.dto.DeployState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.LockState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.clients.acmr.dto.ToscaIdentifier;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakHandler;
import com.ericsson.oss.ae.acm.clients.keycloak.common.KeycloakUrlGenerator;
import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.core.services.builders.AppComponentInstancePropertyBuilder;
import com.ericsson.oss.ae.acm.core.services.handlers.DeployActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UndeployActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UpdateActionHandler;
import com.ericsson.oss.ae.acm.core.services.handlers.UpgradeActionHandler;
import com.ericsson.oss.ae.acm.core.validation.statemachine.AppInstanceUseCase;
import com.ericsson.oss.ae.acm.core.validation.statemachine.StateTransitionValidator;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentInstanceRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppComponentRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.persistence.repository.ClientCredentialRepository;
import com.ericsson.oss.ae.acm.presentation.mapper.AppInstancesMapper;
import com.ericsson.oss.ae.presentation.exceptions.InvalidInputException;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequest;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.UpdateAppInstanceRequest;

@ExtendWith(MockitoExtension.class)
public class AppInstancesServiceImplTest {

    private static final String AC_INSTANCE_PROPERTIES_FILE_FOR_AN_APP_LCM_APP_INSTANCE = "AC instance properties file for an App LCM app instance";
    private static final String REQUEST_CONTENT = "requestContent";
    private static final String CREATE_INSTANCE_FAILED = "Create instance failed";
    @Mock
    private AcmFileGenerator mockAcmFileGenerator;
    @Mock
    private AppRepository mockAppRepository;
    @Mock
    private AcmService mockAcmService;
    @Mock
    private ClientCredentialRepository mockClientCredentialRepository;
    @Mock
    private AppInstancesMapper mockAppInstancesMapper;
    @Mock
    private AppInstancesRepository mockAppInstancesRepository;
    @Mock
    private KeycloakHandler mockKeycloakHandler;
    @Mock
    private AppInstanceEventRepository mockAppInstancesEventrepository;
    @Mock
    private AppComponentInstanceRepository mockAppComponentInstanceRepository;
    @Mock
    private AppComponentRepository mockAppComponentRepository;
    @Mock
    private KeycloakUrlGenerator mockKeycloakUrlGenerator;

    @Mock
    private StateTransitionValidator mockStateTransition;
    @Mock
    private DeployActionHandler deployActionHandler;
    @Mock
    private UndeployActionHandler undeployActionHandler;
    @Mock
    private UpdateActionHandler updateActionHandler;

    @Mock
    private AppComponentInstancePropertyBuilder appComponentInstancePropertyBuilder;

    @Autowired
    @InjectMocks
    AppInstancesServiceImpl appInstancesService;

    @Mock
    private UpgradeActionHandler upgradeActionHandler;

    public AppInstancesServiceImplTest() {
    }

    @BeforeEach
    void setUp() {
        appInstancesService = new AppInstancesServiceImpl(mockAcmFileGenerator, mockAcmService, mockAppInstancesMapper, mockAppRepository,
                mockAppInstancesRepository, mockClientCredentialRepository, mockKeycloakHandler, mockAppInstancesEventrepository, mockStateTransition, mockAppComponentInstanceRepository, mockAppComponentRepository,
                appComponentInstancePropertyBuilder, deployActionHandler, updateActionHandler, undeployActionHandler, mockKeycloakUrlGenerator, upgradeActionHandler);
        appInstancesService.defaultAcmTimeout = 1200000L; // Set the defaultAcmTimeout value

    }

    @Test
    void testCreateAppInstance_Success() throws Exception {
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        final App app = TestUtils.generateAppResponseForCreateInstance();
        final UUID instanceId = UUID.randomUUID();
        final AppInstances appInstances = TestUtils.createAppInstance();

        app.setMode(AppMode.ENABLED);
        appInstances.setStatus(AppInstanceStatus.DELETING);
        appInstances.setId(instanceId);
        appInstances.setApp(app);
        appInstances.setCompositionInstanceId(UUID.randomUUID());
        String expectedBasePath = "http://example.com"; // Define an expected base path
        final Optional<App> app1 = Optional.of(app);

        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(UUID.randomUUID(),
            new ToscaIdentifier(AC_INSTANCE_PROPERTIES_FILE_FOR_AN_APP_LCM_APP_INSTANCE, VERSION_2_0_0));

        AutomationCompositionInstance compositionInstance = new AutomationCompositionInstance(instanceId, instanceId,
            DeployState.DEPLOYED, LockState.NONE, new HashMap<>(), StateChangeResult.NO_ERROR);

        Map<UUID, AutomationCompositionElement> elements = new HashMap<>();
        elements.put(instanceId, new AutomationCompositionElement(instanceId, new ToscaIdentifier("",""), UUID.randomUUID(),
            DeployState.DEPLOYED, LockState.NONE, StateChangeResult.NO_ERROR, "" , new HashMap<>()));

        compositionInstance.setElements(elements);

        AppComponentInstance appComponentInstance = new AppComponentInstance();
        appComponentInstance.setAppInstance(appInstances);
        appComponentInstance.setCompositionElementInstanceId(instanceId);

        AppComponent appComponent = new AppComponent();
        appComponent.setId(UUID.randomUUID());
        appComponentInstance.setAppComponent(appComponent);
        appComponentInstance.setAppId(app.getId());
        List<AppComponentInstance> appComponentInstances = new ArrayList<>();
        appComponentInstances.add(appComponentInstance);

        // Mock required methods or services
        when(mockAppRepository.findById(UUID.fromString(createAppInstanceRequest.getAppId()))).thenReturn(app1);
        doNothing().when(mockStateTransition).validateAppInstanceState(UUID.fromString(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);
        when(mockKeycloakHandler.generateKeycloakCredentials(any())).thenReturn(TestUtils.getClientCredential());
        when(mockKeycloakUrlGenerator.generateBasePath()).thenReturn(expectedBasePath);
        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any()))
            .thenReturn(REQUEST_CONTENT);
        when(mockAcmService.commissionAutomationCompositionInstance(any(), any()))
            .thenReturn(acInstanceResponse);
        when(mockAppInstancesMapper.generateAppInstanceEntity(any(UUID.class), any(App.class)))
            .thenReturn(appInstances);
        when(mockAppInstancesRepository.save(any(AppInstances.class))).thenReturn(appInstances);
        when(mockAppComponentRepository.findById(any())).thenReturn(Optional.of(appComponent));
        when(mockAcmService.getAutomationCompositionInstance(any(), any())).thenReturn(compositionInstance);
        when(mockAppInstancesMapper.toAppInstance(any(), any(), any())).thenReturn(new AppInstance());
        when(mockAppComponentInstanceRepository.findByAppInstanceId(any())).thenReturn(appComponentInstances);

        // Run the test
        final AppInstance result = appInstancesService.createAppInstance(createAppInstanceRequest);

        // Verify the results
        assertThat(result).isNotNull();

        // Verify that generateAppInstanceEntity was called with the expected arguments
        verify(mockAppInstancesMapper).generateAppInstanceEntity(any(UUID.class), any(App.class));
    }

    @Test
    void testCreateAppInstance_MultiComponent_Success() throws Exception {
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        final App app = TestUtils.generateAppWithMultipleComponentsForCreateInstance();
        final UUID instanceId = UUID.fromString(EEFD_6C8CC);
        final UUID dataManagementElementId = UUID.fromString(ELEMENT_INSTANCE_DATA_MANAGEMENT);
        final UUID asdComponentId = UUID.randomUUID();
        final UUID dataManagementComponentId = UUID.randomUUID();
        AppComponent asdComponent = getAppComponent(app, "Microservice");
        asdComponent.setId(asdComponentId);
        AppComponent dataManagementComponent = getAppComponent(app, "DataManagement");
        dataManagementComponent.setId(dataManagementComponentId);

        final AppInstances appInstances = TestUtils.createAppInstance();

        app.setMode(AppMode.ENABLED);
        appInstances.setStatus(AppInstanceStatus.DELETING);
        appInstances.setId(instanceId);
        appInstances.setApp(app);
        appInstances.setCompositionInstanceId(UUID.randomUUID());
        String expectedBasePath = "http://example.com"; // Define an expected base path
        final Optional<App> app1 = Optional.of(app);

        List<AppComponentInstance> appComponentInstances = new ArrayList<>();
        AppComponentInstance appComponentInstance = null;
        for (AppComponent appComponent : app.getAppComponents()) {
            appComponentInstance = TestUtils.createAppComponentInstanceForTestCase(appComponent, appInstances);
            appComponentInstances.add(appComponentInstance);
        }

        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(UUID.randomUUID(),
            new ToscaIdentifier(AC_INSTANCE_PROPERTIES_FILE_FOR_AN_APP_LCM_APP_INSTANCE, VERSION_2_0_0));

        AutomationCompositionInstance compositionInstance = new AutomationCompositionInstance(instanceId, instanceId,
            DeployState.DEPLOYED, LockState.NONE, new HashMap<>(), StateChangeResult.NO_ERROR);

        Map<UUID, AutomationCompositionElement> elements = new HashMap<>();
        elements.put(instanceId, new AutomationCompositionElement(instanceId,
            new ToscaIdentifier("AppLcmMicroserviceAutomationCompositionElement","1.0.0"), UUID.randomUUID(),
            DeployState.DEPLOYED, LockState.NONE, StateChangeResult.NO_ERROR, "" , new HashMap<>()));
        elements.put(dataManagementElementId, new AutomationCompositionElement(dataManagementElementId,
            new ToscaIdentifier("DataManagementAutomationCompositionElement","1.0.0"), UUID.randomUUID(),
            DeployState.DEPLOYED, LockState.NONE, StateChangeResult.NO_ERROR, "" , new HashMap<>()));

        compositionInstance.setElements(elements);

        // Mock required methods or services
        when(mockAppRepository.findById(UUID.fromString(createAppInstanceRequest.getAppId()))).thenReturn(app1);
        doNothing().when(mockStateTransition).validateAppInstanceState(UUID.fromString(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);
        when(mockKeycloakHandler.generateKeycloakCredentials(any())).thenReturn(TestUtils.getClientCredential());
        when(mockKeycloakUrlGenerator.generateBasePath()).thenReturn(expectedBasePath);
        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any()))
            .thenReturn(REQUEST_CONTENT);
        when(mockAcmService.commissionAutomationCompositionInstance(any(), any()))
            .thenReturn(acInstanceResponse);
        when(mockAppInstancesMapper.generateAppInstanceEntity(any(UUID.class), any(App.class)))
            .thenReturn(appInstances);
        when(mockAppInstancesRepository.save(any(AppInstances.class))).thenReturn(appInstances);
        when(mockAppComponentRepository.findById(asdComponentId)).thenReturn(Optional.of(asdComponent));
        when(mockAppComponentRepository.findById(dataManagementComponentId)).thenReturn(Optional.of(dataManagementComponent));
        when(mockAcmService.getAutomationCompositionInstance(any(), any())).thenReturn(compositionInstance);
        when(mockAppInstancesMapper.toAppInstance(any(), any(), any())).thenReturn(new AppInstance());
        when(mockAppComponentInstanceRepository.findByAppInstanceId(any())).thenReturn(appComponentInstances);

        // Run the test
        final AppInstance result = appInstancesService.createAppInstance(createAppInstanceRequest);

        // Verify the results
        assertThat(result).isNotNull();

        // Verify that generateAppInstanceEntity was called with the expected arguments
        verify(mockAppInstancesMapper).generateAppInstanceEntity(any(UUID.class), any(App.class));
    }

    @Test
    void testCreateAppInstance_should_throw_exception_if_app_not_found() throws Exception {
        // Mocked request and expected response
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        // Setup
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setMode(AppMode.ENABLED);
        final AppInstances appInstances = TestUtils.createAppInstance();
        appInstances.setStatus(AppInstanceStatus.UNDEPLOYED);
        appInstances.setId(UUID.randomUUID());
        appInstances.setApp(app);
        // Mock required methods or services
        when(mockAppRepository.findById(UUID.fromString(createAppInstanceRequest.getAppId()))).thenReturn(Optional.empty());

        doNothing().when(mockStateTransition).validateAppInstanceState(UUID.fromString(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);

        // Verify the results
        assertThatThrownBy(() -> appInstancesService.createAppInstance(createAppInstanceRequest))
                .isInstanceOf(AppLcmException.class);
    }

    @Test
    void testCreateAppInstance_WhenAcmFileGeneratorThrowsAppLcmException() {
        // Mocked request and expected response
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        // Setup
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setMode(AppMode.ENABLED);
        final AppInstances appInstances = TestUtils.createAppInstance();
        appInstances.setStatus(AppInstanceStatus.DEPLOYED);
        appInstances.setId(UUID.randomUUID());
        appInstances.setApp(app);
        String expectedBasePath = "http://example.com"; // Define an expected base path
        final Optional<App> app1 = Optional.of(app);
        // Mock required methods or services
        when(mockAppRepository.findById(UUID.fromString(createAppInstanceRequest.getAppId()))).thenReturn(app1);
        doNothing().when(mockStateTransition).validateAppInstanceState(UUID.fromString(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);
        when(mockKeycloakHandler.generateKeycloakCredentials(any())).thenReturn(TestUtils.getClientCredential());
        when(mockKeycloakUrlGenerator.generateBasePath()).thenReturn(expectedBasePath);
        // Simulate AcmFileGenerator throwing AppLcmException
        AppLcmException exception = new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.AC_INSTANCE_PROPERTIES_GENERATION_ERROR);
        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any()))
            .thenThrow(exception);
        // Ensure the mocked method returns a non-null AppInstances object
        when(mockAppInstancesMapper.generateAppInstanceEntity(any(UUID.class), any(App.class)))
            .thenReturn(appInstances);
        // Run the test and assert that it throws AppLcmException
        AssertionsForClassTypes.assertThatThrownBy(() -> appInstancesService.createAppInstance(createAppInstanceRequest))
            .isInstanceOf(AppLcmException.class)
            .satisfies(e -> assertThat(((AppLcmException) e).getAppLcmError()).isEqualTo(AppLcmError.AC_INSTANCE_PROPERTIES_GENERATION_ERROR));
    }
    @Test
    void testCreateAppInstance_ThrowsAppLcmExceptionOnRestRequestFailedException() throws URISyntaxException {
        // Mocked request and expected response
        final CreateAppInstanceRequest createAppInstanceRequest = TestUtils.generateCreateAppInstanceRequest();
        // Setup
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setMode(AppMode.ENABLED);
        final AppInstances appInstances = TestUtils.createAppInstance();
        appInstances.setStatus(AppInstanceStatus.DEPLOYED);
        appInstances.setId(UUID.randomUUID());
        appInstances.setApp(app);
        String expectedBasePath = "http://example.com"; // Define an expected base path
        final Optional<App> app1 = Optional.of(app);
        // Mock required methods or services
        when(mockAppRepository.findById(UUID.fromString(createAppInstanceRequest.getAppId()))).thenReturn(app1);
        doNothing().when(mockStateTransition).validateAppInstanceState(UUID.fromString(createAppInstanceRequest.getAppId()), AppInstanceUseCase.CREATE);
        when(mockKeycloakHandler.generateKeycloakCredentials(any())).thenReturn(TestUtils.getClientCredential());
        when(mockKeycloakUrlGenerator.generateBasePath()).thenReturn(expectedBasePath);
        // Simulate AcmFileGenerator throwing RestRequestFailedException
        RestRequestFailedException exception = new RestRequestFailedException(HttpStatus.INTERNAL_SERVER_ERROR, CREATE_INSTANCE_FAILED);
        when(mockAcmFileGenerator.generateAcmInstancePropertiesFile(any()))
            .thenThrow(exception);
        // Ensure the mocked method returns a non-null AppInstances object
        when(mockAppInstancesMapper.generateAppInstanceEntity(any(UUID.class), any(App.class)))
            .thenReturn(appInstances);
        // Run the test and assert that it throws AppLcmException
        AssertionsForClassTypes.assertThatThrownBy(() -> appInstancesService.createAppInstance(createAppInstanceRequest))
            .isInstanceOf(AppLcmException.class);
    }


    @Test
    public void testDeleteAppInstance() throws URISyntaxException {
        final AppInstanceOperationResponse expectedResult = TestUtils.getAppInstanceDetails();

        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setMode(AppMode.ENABLED);

        final AppInstances appInstances = TestUtils.createAppInstance();
        appInstances.setStatus(AppInstanceStatus.UNDEPLOYED);
        appInstances.setId(UUID.fromString(EEFD_22233));
        appInstances.setApp(app);
        final Optional<AppInstances> appInstance1 = Optional.of(appInstances);
        when(mockAppInstancesRepository.findById(appInstances.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstances.getId(), AppInstanceUseCase.DELETE);

        // Configure AcmService.deleteAutomationCompositionInstance(...).
        final AcInstanceResponse acInstanceResponse = new AcInstanceResponse(appInstances.getCompositionInstanceId(),
                new ToscaIdentifier(AC_INSTANCE_PROPERTIES_FILE_FOR_AN_APP_LCM_APP_INSTANCE, VERSION_2_0_0));

        when(mockAcmService.deleteAutomationCompositionInstance(any(), any()))
                .thenReturn(acInstanceResponse);
        when(mockAppInstancesRepository.save(any(AppInstances.class))).thenReturn(AppInstances.builder().build());

        // Configure AppInstancesMapper.fromApp(...).
        final AppInstanceOperationResponse appInstance = TestUtils.getAppInstanceDetails();
        when(mockAppInstancesMapper.toAppInstanceOperationResponse(any(AppInstances.class))).thenReturn(appInstance);

        // Run the test
        final AppInstanceOperationResponse result = appInstancesService.deleteAppInstance(appInstances.getId().toString());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

    }

    @Test
    public void testDeployAppInstance() throws Exception {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest().type(AppInstanceManagementRequest.TypeEnum.DEPLOY);

        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.DEPLOY);
        doNothing().when(deployActionHandler).deployAppInstance(appInstancesUnderTest, deployUndeployAppInstanceRequest);

        final AppInstanceManagementResponse appInstance = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstance);
        // Run the test
        final AppInstanceManagementResponse result = appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest);

        // Verify the results
        assertThat(result.getAppInstance()).isNotNull();
        assertThat(result.getAdditionalData()).isNotNull();
    }

    @Test
    void testManageAppInstance_should_throw_exception_for_not_valid_ns() {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(getAdditionalData(getStructuredPropertiesWithNamespace("B@d_ns")));

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setId(UUID.randomUUID());

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.DEPLOY);

        // Verify the results
        assertThatThrownBy(() -> appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void testDeployAppInstance_with_properties() throws Exception {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(getAdditionalData(getStructuredPropertiesWithNamespace("test-ns")));

        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.DEPLOY);
        doNothing().when(deployActionHandler).deployAppInstance(appInstancesUnderTest, deployUndeployAppInstanceRequest);

        final AppInstanceManagementResponse appInstance = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstance);

        // Run the test
        final AppInstanceManagementResponse result = appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest);

        // Verify the results
        assertThat(result.getAppInstance()).isNotNull();
        assertThat(result.getAdditionalData()).isNotNull();
    }

    @Test
    void testDeployAppInstance_with_properties_including_empty_map() throws Exception {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.DEPLOY)
                .additionalData(getAdditionalData(new HashMap<>()));

        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.DEPLOY);
        doNothing().when(deployActionHandler).deployAppInstance(appInstancesUnderTest, deployUndeployAppInstanceRequest);

        final AppInstanceManagementResponse appInstance = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstance);

        // Run the test
        final AppInstanceManagementResponse result = appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest);

        // Verify the results
        assertThat(result.getAppInstance()).isNotNull();
        assertThat(result.getAdditionalData()).isNotNull();
    }

    @Test
    void testDeployAppInstance_with_properties_without_namespace() throws Exception {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest().type(AppInstanceManagementRequest.TypeEnum.DEPLOY);

        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.DEPLOY);
        doNothing().when(deployActionHandler).deployAppInstance(appInstancesUnderTest, deployUndeployAppInstanceRequest);

        final AppInstanceManagementResponse appInstance = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstance);

        // Run the test
        final AppInstanceManagementResponse result = appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest);

        // Verify the results
        assertThat(result.getAppInstance()).isNotNull();
        assertThat(result.getAdditionalData()).isNotNull();
    }

    @Test
    public void testUndeployAppInstance() throws Exception {
        // Setup
        final AppInstanceManagementRequest deployUndeployAppInstanceRequest = new AppInstanceManagementRequest().type(AppInstanceManagementRequest.TypeEnum.UNDEPLOY);

        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.UNDEPLOY);
        doNothing().when(undeployActionHandler).undeployAppInstance(appInstancesUnderTest);

        final AppInstanceManagementResponse appInstance = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstance);
        // Run the test
        final AppInstanceManagementResponse result = appInstancesService.manageAppInstance(
                appInstancesUnderTest.getId().toString(),
                deployUndeployAppInstanceRequest);

        // Verify the results
        assertThat(result.getAppInstance()).isNotNull();
        assertThat(result.getAdditionalData()).isNotNull();
    }

    @Test
    public void testUpdateAppInstance() {
        // Setup
        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setId(UUID.fromString("4f7ed323-2923-43d7-928e-ee79c34701aa"));
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setMode(AppMode.ENABLED);
        appUnderTest.setCompositionId(UUID.randomUUID());
        appUnderTest.setId(UUID.randomUUID());
        final AppComponentInstance appComponentInstanceUnderTest = TestUtils.getComponentInstance();
        appComponentInstanceUnderTest.setAppId(appUnderTest.getId());
        appComponentInstanceUnderTest.setAppInstance(appInstancesUnderTest);
        appInstancesUnderTest.setCompositionInstanceId(appComponentInstanceUnderTest.getCompositionElementInstanceId());
        appInstancesUnderTest.setApp(appUnderTest);
        final List<AppComponentInstance> appComponentInstances = new ArrayList<>();
        appComponentInstanceUnderTest.setAppComponent(new AppComponent(UUID.randomUUID(), "", "", "1.0.0", "MS", new ArrayList<>(), appComponentInstances, appUnderTest));
        appComponentInstances.add(appComponentInstanceUnderTest);
        appInstancesUnderTest.setAppComponentInstances(appComponentInstances);
        final HashMap<UUID,AutomationCompositionElement> compositionElementHashMap = new HashMap<>();
        final AutomationCompositionElement compositionElement = new AutomationCompositionElement(appComponentInstanceUnderTest.getCompositionElementInstanceId(), new ToscaIdentifier("test", "1.0.0"), UUID.randomUUID(), DeployState.UPDATING, LockState.UNLOCKED, StateChangeResult.NO_ERROR, "", new HashMap<>());
        compositionElementHashMap.put(appComponentInstanceUnderTest.getCompositionElementInstanceId(), compositionElement);

        UpdateAppInstanceRequest updateAppInstanceRequest = new UpdateAppInstanceRequest().componentInstances(List.of(TestUtils.createAsdComponentInstance(appUnderTest, AppInstanceStatus.UNDEPLOYED)));

        final Optional<AppInstances> appInstance1 = Optional.of(appInstancesUnderTest);
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(appInstance1);
        doNothing().when(mockStateTransition).validateAppInstanceState(appInstancesUnderTest.getId(), AppInstanceUseCase.UPDATE);
        doNothing().when(updateActionHandler).updateAppInstance(appInstancesUnderTest, updateAppInstanceRequest.getComponentInstances());
        when(mockAcmService.getAutomationCompositionInstance(appUnderTest.getCompositionId(), appInstancesUnderTest.getCompositionInstanceId()))
            .thenReturn(new AutomationCompositionInstance(appInstancesUnderTest.getId(), appInstancesUnderTest.getApp().getCompositionId(), DeployState.UPDATING, LockState.NONE, compositionElementHashMap,
                StateChangeResult.NO_ERROR));
        when(mockAppInstancesRepository.findById(appInstancesUnderTest.getId())).thenReturn(Optional.of(appInstancesUnderTest));
        when(mockAppComponentRepository.findById(appComponentInstanceUnderTest.getAppComponent().getId())).thenReturn(Optional.ofNullable(appComponentInstanceUnderTest.getAppComponent()));
        when(mockAppComponentInstanceRepository.findByAppInstanceId(appInstancesUnderTest.getId())).thenReturn(appInstancesUnderTest.getAppComponentInstances());
        when(mockAppInstancesMapper.toAppInstance(any(AppInstances.class), anyList(), any(ClientCredential.class))).thenReturn(new AppInstance());
        when(mockAppInstancesMapper.toAppInstanceUpdateResponse(any(AppInstance.class)))
            .thenReturn(DummyDataGenerator.getDummyAppInstanceUpdateOperationResponse());


        // Run the test
        final AppInstanceUpdateResponse appInstanceOperationResponse = appInstancesService.updateAppInstance(appInstancesUnderTest.getId().toString(),
                updateAppInstanceRequest);

        // Verify the results
        assertNotNull(appInstanceOperationResponse);
    }

    @Test
    public void upgradeAppInstance_shouldSucceed() throws Exception{
        UUID instanceId = UUID.randomUUID();
        UUID targetAppId = UUID.randomUUID();
        final AppInstances appInstancesUnderTest = getAppInstanceDetailsForDeploy();

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(List.of());
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(targetAppId.toString())
                .additionalData(additionalData);

        when(mockAppInstancesRepository.findById(instanceId)).thenReturn(Optional.of(appInstancesUnderTest));

        App targetAppDetails = new App();
        targetAppDetails.setMode(AppMode.ENABLED);
        targetAppDetails.setStatus(AppStatus.INITIALIZED);

        when(mockAppRepository.findById(targetAppId)).thenReturn(Optional.of(targetAppDetails));

        // Mocking the execution of upgradeActionHandler
        doNothing().when(upgradeActionHandler).executeAppInstanceUpgrade(
                appInstancesUnderTest, targetAppDetails, upgradeAppInstanceRequest);

        // Mocking successful appInstancesMapper conversion
        final AppInstanceManagementResponse appInstanceManagementResponse = TestUtils.getAppInstanceManagementDetails(AppInstanceStatus.DEPLOYING);
        when(mockAppInstancesMapper.toAppInstanceManagementResponse(any(), any(AppInstances.class))).thenReturn(appInstanceManagementResponse);
        // Act
        AppInstanceManagementResponse result = appInstancesService.manageAppInstance(instanceId.toString(), upgradeAppInstanceRequest);

        // Assert
        verify(mockStateTransition).validateAppInstanceState(instanceId, AppInstanceUseCase.UPGRADE);
        verify(upgradeActionHandler).executeAppInstanceUpgrade(
                appInstancesUnderTest, targetAppDetails, upgradeAppInstanceRequest);
        verify(mockAppInstancesMapper).toAppInstanceManagementResponse(any(), any());
    }

    @Test
    public void upgradeAppInstance_shouldHandleAppLcmException() {
        UUID instanceId = UUID.randomUUID();

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setId(UUID.fromString("4f7ed323-2923-43d7-928e-ee79c34701aa"));
        final App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest.setMode(AppMode.ENABLED);
        appUnderTest.setStatus(AppStatus.INITIALIZED);
        appUnderTest.setCompositionId(UUID.randomUUID());
        appInstancesUnderTest.setApp(appUnderTest);

        final AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
        additionalData.setComponentInstances(null);
        final AppInstanceManagementRequest upgradeAppInstanceRequest = new AppInstanceManagementRequest()
                .type(AppInstanceManagementRequest.TypeEnum.UPGRADE)
                .targetAppId(UUID.randomUUID().toString())
                .additionalData(additionalData);

        when(mockAppInstancesRepository.findById(instanceId)).thenReturn(Optional.of(appInstancesUnderTest));
        when(mockAppRepository.findById(any())).thenReturn(Optional.of(appUnderTest));
        doThrow(new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.UPGRADE_APP_INSTANCE_ERROR)).when(upgradeActionHandler).executeAppInstanceUpgrade(any(), any(), any());
        assertThrows(AppLcmException.class,
            () -> appInstancesService.manageAppInstance(instanceId.toString(), upgradeAppInstanceRequest));
    }

    private AppInstances getAppInstanceDetailsForDeploy(){
        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setId(UUID.fromString("4f7ed323-2923-43d7-928e-ee79c34701aa"));
        final App app = TestUtils.generateAppResponseForCreateInstance();
        app.setMode(AppMode.ENABLED);
        appInstancesUnderTest.setApp(app);
        final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();
        for (AppComponent appComponent : appInstancesUnderTest.getApp().getAppComponents()) {
            AppComponentInstance appComponentInstance = TestUtils.createAppComponentInstanceForTestCase(appComponent, appInstancesUnderTest);
            appComponentInstance.setAppInstance(appInstancesUnderTest);
            appComponentInstancesList.add(appComponentInstance);
        }
        appInstancesUnderTest.setAppComponentInstances(appComponentInstancesList);
        return appInstancesUnderTest;
    }

    @Test
    void testGetAppByInstanceId_success() {
        UUID instanceId = UUID.randomUUID();
        AppInstances appInstances = new AppInstances(instanceId, instanceId, AppInstanceStatus.DEPLOYED,
                App.builder().compositionId(instanceId).build(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        AutomationCompositionInstance compositionInstance = new AutomationCompositionInstance(instanceId, instanceId,
                DeployState.DEPLOYED, LockState.NONE, new HashMap<>(), StateChangeResult.NO_ERROR);

        when(mockAppInstancesRepository.findById(instanceId)).thenReturn(Optional.of(appInstances));
        when(mockAcmService.getAutomationCompositionInstance(instanceId, instanceId)).thenReturn(compositionInstance);
        when(mockAppInstancesMapper.toAppInstance(any(), any(), any())).thenReturn(new AppInstance());

        AppInstance result = appInstancesService.getAppInstanceById(instanceId.toString());

        assertThat(result).isNotNull();
    }

    @Test
    public void validateTimeout_DefaultExceeded_Fails() {
        ComponentInstances componentInstance = new ComponentInstances();
        Map<String, Object> properties = new HashMap<>();
        properties.put(AppLcmConstants.TIMEOUT, (int) ((appInstancesService.defaultAcmTimeout / 1000) + 1));
        componentInstance.setProperties(properties);

        assertThatThrownBy(() -> appInstancesService.validateTimeout(List.of(componentInstance)))
            .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void validateTimeout_StringTimeout_Fails() {
        ComponentInstances componentInstance = new ComponentInstances();
        Map<String, Object> properties = new HashMap<>();
        properties.put(AppLcmConstants.TIMEOUT, "timeout_value");
        componentInstance.setProperties(properties);

        assertThatThrownBy(() -> appInstancesService.validateTimeout(List.of(componentInstance)))
            .isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testValidateTimeout_ValidTimeout() {
        ComponentInstances componentInstance = new ComponentInstances();
        Map<String, Object> properties = new HashMap<>();
        properties.put(AppLcmConstants.TIMEOUT, (int) ((appInstancesService.defaultAcmTimeout / 1000) - 1));
        componentInstance.setProperties(properties);
        final App appUnderTest = TestUtils.generateAppEntity();

        assertDoesNotThrow(() -> appInstancesService.validateTimeout(List.of(TestUtils.createAsdComponentInstance(appUnderTest, AppInstanceStatus.UNDEPLOYED))));
    }

    private AppInstanceManagementRequestAdditionalData getAdditionalData(Map<String, Object> properties) {
        List<ComponentInstances> componentInstances = new ArrayList<>();
        ComponentInstances instance = new ComponentInstances().name("hello_world_app").properties(properties);
        componentInstances.add(instance);
        return new AppInstanceManagementRequestAdditionalData().componentInstances(componentInstances);
    }

    private Map<String, Object> getStructuredPropertiesWithNamespace(String namespace) {
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put(TIMEOUT_KEY, 5);
        componentPropertyMap.put("namespace", namespace);
        return componentPropertyMap;
    }

    private AppComponent getAppComponent(final App app, final String type) throws Exception {
        return app.getAppComponents().stream().filter(component -> component.getType().equals(type)).findFirst().orElseThrow(Exception::new);
    }

}
