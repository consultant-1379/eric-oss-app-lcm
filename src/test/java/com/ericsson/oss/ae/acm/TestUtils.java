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

package com.ericsson.oss.ae.acm;

import static com.ericsson.oss.ae.acm.TestConstants.ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.ADMIN;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_HELLO_WORLD;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_NAME;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_TYPE;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.APP_PROVIDER;
import static com.ericsson.oss.ae.acm.TestConstants.APP_VERSION_1_1_1;
import static com.ericsson.oss.ae.acm.TestConstants.BUCKET_NAME;
import static com.ericsson.oss.ae.acm.TestConstants.COMPOSITION_ID;
import static com.ericsson.oss.ae.acm.TestConstants.DATA_MANAGEMENT_COMPONENT_HIGHER_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.DATA_MANAGEMENT_COMPONENT_NAME;
import static com.ericsson.oss.ae.acm.TestConstants.DATA_MANAGEMENT_COMPONENT_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.DOCKER;
import static com.ericsson.oss.ae.acm.TestConstants.DOCKER_TAR;
import static com.ericsson.oss.ae.acm.TestConstants.EEFD_0035;
import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22230;
import static com.ericsson.oss.ae.acm.TestConstants.EEFD_6C8CC;
import static com.ericsson.oss.ae.acm.TestConstants.ELEMENT_INSTANCE_DATA_MANAGEMENT;
import static com.ericsson.oss.ae.acm.TestConstants.FOO;
import static com.ericsson.oss.ae.acm.TestConstants.GLOBAL;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_APP;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_METRICS_GO_APP_COMPONENT_INSTANCE;
import static com.ericsson.oss.ae.acm.TestConstants.HELM;
import static com.ericsson.oss.ae.acm.TestConstants.IDS_FILE;
import static com.ericsson.oss.ae.acm.TestConstants.IMAGE;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_ID_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_CLIENT_SECRET_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.KEYCLOAK_SCOPE_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.LCM_PARTICIPANT;
import static com.ericsson.oss.ae.acm.TestConstants.NAMESPACE_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.NGINX;
import static com.ericsson.oss.ae.acm.TestConstants.OBJECT_STORE_ACCESS_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.OBJECT_STORE_SECRET_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.OPAQUE;
import static com.ericsson.oss.ae.acm.TestConstants.REPLICA_COUNT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.SCOPE;
import static com.ericsson.oss.ae.acm.TestConstants.TEST_ONBOARDING_JOB_ID;
import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.USER;
import static com.ericsson.oss.ae.acm.TestConstants.USER_DEFINED_HELM_PARAMETERS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCE_REPLICA_COUNT_KEY;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCE_REPLICA_COUNT_VALUE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTOMATION_COMPOSITION_ELEMENT;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CLIENT_SECRET;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.DATAMANAGEMENT;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.MICROSERVICE;
import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getDummyAppDetailsById;
import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getDummyAppInstanceDetails;
import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getDummyAppInstanceWithMultipleComponentsDetails;
import static com.ericsson.oss.ae.acm.common.dummy.DummyDataGenerator.getDummyAppsDetails;
import static com.ericsson.oss.ae.acm.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_ID_NAME;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KAFKA;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ericsson.oss.ae.acm.clients.acmr.common.AppComponentTypeComparator;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.ToscaIdentifier;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.AttributeScopeDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientScopeDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.acm.clients.keycloak.dto.TokenDto;
import com.ericsson.oss.ae.acm.clients.minio.ObjectStoreContainer;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.Artifact;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.Permission;
import com.ericsson.oss.ae.acm.persistence.entity.Role;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponseAppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.Component;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.CreateAppInstanceRequest;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.Href;
import com.ericsson.oss.ae.v3.api.model.ProblemDetails;

/**
 * Test utility class.
 */
public class TestUtils {

    public static final String GOODBYE_WORLD = "goodbyeWorld";
    public static final String MONGO_DB = "mongoDB";
    public static final String ADMIN1 = "admin";
    public static final String HTTP_APP_MANAGER_URL_APP = "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035";
    public static final String HTTP_APP_MANAGER_URL_APP_INSTANCE = "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230";

    public static final String KEYCLOAK_CLIENT_URL_KEY_VALUE = "clientUrl";
    public static final String AC_ELEMENT_DEFINITION_VERSION = "1.0.0";
    public static final String COMPOSITION_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT = "fac50cde-11a2-4915-a49c-709762714a5d";
    public static final String INSTANCE_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT = "fb576d1c-9a58-461c-b43f-697dd88dc064";

    public static CreateAppRequest generateCreateAppRequest() {
        // Initialize a CreateAppRequest object.
        final CreateAppRequest createAppRequest = new CreateAppRequest();

        // Set the name, version and type for the createAppRequest.
        createAppRequest.name(APP_ENTITY_NAME);
        createAppRequest.version(APP_VERSION_1_1_1);
        createAppRequest.type(APP_ENTITY_TYPE);
        createAppRequest.provider(APP_PROVIDER);

        // Create an appComponent for the createAppRequest test fixture.
        Component component = new Component();
        component.name(HELLO_WORLD_METRICS_GO_APP_COMPONENT_INSTANCE);
        component.version(APP_VERSION_1_1_1);
        component.type(MICROSERVICE);
        component.addArtifactsItem(
                new com.ericsson.oss.ae.v3.api.model.Artifact()
                        .name(APP_ENTITY_HELLO_WORLD)
                        .type(HELM)
                        .location(BUCKET_NAME + SLASH + EEFD_22230 + SLASH + APP_ENTITY_HELLO_WORLD)
        );
        component.addArtifactsItem(
                new com.ericsson.oss.ae.v3.api.model.Artifact()
                        .name(DOCKER_TAR)
                        .type(IMAGE)
                        .location(BUCKET_NAME + SLASH + EEFD_22230 + SLASH + DOCKER)
        );

        // Set the appComponents and appPermissionsItem of the createAppRequest.
        createAppRequest.addComponentsItem(component);
        createAppRequest.addPermissionsItem(new com.ericsson.oss.ae.v3.api.model.Permission().resource(NGINX).scope(FOO));
        createAppRequest.addPermissionsItem(new com.ericsson.oss.ae.v3.api.model.Permission().resource(MONGO_DB).scope(GOODBYE_WORLD));
        createAppRequest.addRolesItem(new com.ericsson.oss.ae.v3.api.model.Role().name(ADMIN1));

        return createAppRequest;
    }

    public static CreateAppRequest generateCreateAppRequestWithMultipleComponents() {

        final CreateAppRequest createAppRequest = generateCreateAppRequest();

        final Component component = new Component();
        component.name(DATA_MANAGEMENT_COMPONENT_NAME);
        component.version(DATA_MANAGEMENT_COMPONENT_VERSION);
        component.type(DATAMANAGEMENT);
        component.addArtifactsItem(
                new com.ericsson.oss.ae.v3.api.model.Artifact()
                        .name(IDS_FILE)
                        .type(OPAQUE)
                        .location(BUCKET_NAME + SLASH + EEFD_22230 + SLASH + IDS_FILE)
        );

        createAppRequest.addComponentsItem(component);

        return createAppRequest;
    }

    public static CreateAppInstanceRequest generateCreateAppInstanceRequest() {
        CreateAppInstanceRequest createAppInstanceRequest = new CreateAppInstanceRequest();
        createAppInstanceRequest.setAppId(EEFD_22230);
        return createAppInstanceRequest;
    }

    public static App generateAppEntity() {
        return generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
    }

    public static App generateAppEntity(final AppMode mode,final AppStatus status) {
        return generateAppEntityCommon(mode, status, APP_ENTITY_VERSION);
    }

    public static App generateAppEntityHigherComponentVersion(final AppMode mode,final AppStatus status,final String version) {
        return generateAppEntityCommon(mode, status, version);
    }

    private static App generateAppEntityCommon(final AppMode mode,final AppStatus status,final String version) {
        App appUnderTest = App.builder()
                .id(UUID.randomUUID())
                .mode(mode).status(status).type(APP_ENTITY_TYPE)
                .name(APP_ENTITY_NAME).version(APP_ENTITY_VERSION)
                .provider(APP_PROVIDER)
                .name(APP_ENTITY_NAME).version(version)
                .compositionId(COMPOSITION_ID).build();
        appUnderTest.setRAppId(generateRAppId(appUnderTest));
        Permission permission1 = Permission.builder().scope(GLOBAL).resource(KAFKA).app(appUnderTest).build();
        Permission permission2 = Permission.builder().scope(FOO).resource(NGINX).app(appUnderTest).build();

        appUnderTest.setPermissions(List.of(permission1, permission2));

        Role role1 = Role.builder().name(ADMIN).app(appUnderTest).build();
        Role role2 = Role.builder().name(USER).app(appUnderTest).build();

        appUnderTest.setRoles(List.of(role1, role2));

        AppComponent appComponent = AppComponent.builder().name(HELLO_WORLD_APP).type(MICROSERVICE).version(version).compositionElementName(AUTOMATION_COMPOSITION_ELEMENT).build();
        UUID bucketId = UUID.fromString(EEFD_0035);
        String bucketName = BUCKET_NAME;
        Artifact artifact1 = Artifact.builder().type(HELM).name(HELLO_WORLD).version(version).location(bucketName + SLASH + bucketId + SLASH + HELLO_WORLD)
                .appComponent(appComponent).build();
        Artifact artifact2 = Artifact.builder().type(IMAGE).name(DOCKER).version(version).location(bucketName + SLASH + bucketId + SLASH + DOCKER)
                .appComponent(appComponent).build();
        appComponent.setArtifacts(List.of(artifact1, artifact2));
        appComponent.setApp(appUnderTest);

        appUnderTest.setAppComponents(List.of(appComponent));

        return appUnderTest;
    }


        public static App generateAppEntityWithMicroserviceAndDataManagementComponents(final AppMode mode, final AppStatus status){
        final App appUnderTest = generateAppEntity(mode, status);

        final AppComponent appComponent = AppComponent.builder()
                .name(DATA_MANAGEMENT_COMPONENT_NAME)
                .type("DataManagement")
                .version(DATA_MANAGEMENT_COMPONENT_VERSION)
                .compositionElementName(AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT).build();
        final Artifact artifact = Artifact.builder().type(OPAQUE).name(IDS_FILE).location(BUCKET_NAME + SLASH + TEST_ONBOARDING_JOB_ID + SLASH + IDS_FILE)
                .appComponent(appComponent).build();

        appComponent.setArtifacts(List.of(artifact));
        final List<AppComponent> appComponents = new ArrayList<>(appUnderTest.getAppComponents());
        appComponents.add(appComponent);
        appUnderTest.setAppComponents(appComponents);

        return appUnderTest;
    }

    public static App generateAppEntityWithMicroserviceAndDataManagementComponentsHigherVersion(final AppMode mode, final AppStatus status){
        final App appUnderTest = generateAppEntityHigherComponentVersion(mode, status, DATA_MANAGEMENT_COMPONENT_HIGHER_VERSION);

        final AppComponent appComponent = AppComponent.builder()
                .name(DATA_MANAGEMENT_COMPONENT_NAME)
                .type("DataManagement")
                .version(DATA_MANAGEMENT_COMPONENT_HIGHER_VERSION)
                .compositionElementName(AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT).build();
        final Artifact artifact = Artifact.builder().type(OPAQUE).name(IDS_FILE).location(BUCKET_NAME + SLASH + TEST_ONBOARDING_JOB_ID + SLASH + IDS_FILE)
                .appComponent(appComponent).build();

        appComponent.setArtifacts(List.of(artifact));
        final List<AppComponent> appComponents = new ArrayList<>(appUnderTest.getAppComponents());
        appComponents.add(appComponent);
        appUnderTest.setAppComponents(appComponents);

        return appUnderTest;
    }

        public static App generateAppEntityWithMultipleComponents(AppMode mode, AppStatus status) {
        App appUnderTest = App.builder()
            .mode(mode).status(status).type(APP_ENTITY_TYPE)
            .name("eric-oss-hello-world-multiple-microservices-go-app").version(APP_ENTITY_VERSION)
            .provider(APP_PROVIDER)
            .compositionId(UUID.randomUUID()).build();
        appUnderTest.setRAppId(generateRAppId(appUnderTest));
        Permission permission1 = Permission.builder().scope(GLOBAL).resource(KAFKA).app(appUnderTest).build();
        Permission permission2 = Permission.builder().scope(FOO).resource(NGINX).app(appUnderTest).build();

        appUnderTest.setPermissions(List.of(permission1, permission2));

        Role role1 = Role.builder().name(ADMIN).app(appUnderTest).build();
        Role role2 = Role.builder().name(USER).app(appUnderTest).build();

        appUnderTest.setRoles(List.of(role1, role2));

        final int numberOfComponents = 2;
        final List<AppComponent> appComponentList = new ArrayList<>();
        for (int count=0; count < numberOfComponents; count++) {
            AppComponent appComponent = AppComponent.builder().name(HELLO_WORLD_APP).type(MICROSERVICE).version(APP_ENTITY_VERSION).compositionElementName(AUTOMATION_COMPOSITION_ELEMENT).build();
            String bucketId = UUID.randomUUID().toString();
            Artifact artifact1 = Artifact.builder().type(HELM).name(HELLO_WORLD).version(APP_ENTITY_VERSION).location(bucketId + "/" + HELLO_WORLD).appComponent(appComponent).build();
            Artifact artifact2 = Artifact.builder().type(IMAGE).name(DOCKER).version(APP_ENTITY_VERSION).location(bucketId + "/" + DOCKER).appComponent(appComponent).build();
            appComponent.setArtifacts(List.of(artifact1, artifact2));
            appComponent.setApp(appUnderTest);
            appComponentList.add(appComponent);
        }

        appUnderTest.setAppComponents(appComponentList);

        return appUnderTest;
    }
    public static App generateAppResponseForCreateInstance() {
        App app = generateAppEntity(AppMode.ENABLED, AppStatus.INITIALIZED);
        app.setId(UUID.fromString(EEFD_0035));
        return app;
    }

    public static App generateAppWithMultipleComponentsForCreateInstance() {
        App app = generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.ENABLED, AppStatus.INITIALIZED);
        app.setId(UUID.fromString(EEFD_0035));
        return app;
    }

    public static AppInstances generateAppInstanceEntityForDeployInstance(final App app) {

        final AppInstances appInstancesUnderTest = TestUtils.createAppInstance();
        appInstancesUnderTest.setApp(app);

        List<AppComponentInstance> appComponentInstances = new ArrayList<>();
        for (AppComponent appComponent : app.getAppComponents()){
            AppComponentInstance appComponentInstance = new AppComponentInstance();
            appComponentInstance.setAppId(app.getId());
            appComponentInstance.setAppInstance(appInstancesUnderTest);
            appComponentInstance.setCompositionElementInstanceId(UUID.randomUUID());
            appComponentInstance.setAppComponent(appComponent);
            appComponentInstances.add(appComponentInstance);
        }
        appInstancesUnderTest.setAppComponentInstances(appComponentInstances);
        return appInstancesUnderTest;
    }

    public static List<App> generateAppEntities(final int numberOfAppEntities, final AppMode mode) {
        final List<App> apps = new ArrayList<>();

        for (int i = 0; i < numberOfAppEntities; i++) {
            final App app = TestUtils.generateAppEntity();
            app.setCompositionId(UUID.randomUUID());
            app.setName(String.format("%s-%d", APP_ENTITY_HELLO_WORLD, i + 1));
            app.setMode(mode);
            app.setProvider(APP_PROVIDER);
            apps.add(app);
        }

        return apps;
    }

    public static AppInstances generateAppInstanceEntity() {
      return generateAppInstanceEntity(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED, AppInstanceStatus.UNDEPLOYED, false, false);
    }

    public static AppInstances generateAppInstanceEntity(final UUID compositionInstanceId, final AppInstanceStatus status, final boolean withMultipleAppComponents, final boolean withDataManagementComponent) {
        final App app;
        if (withMultipleAppComponents &! withDataManagementComponent) {
            app = generateAppEntityWithMultipleComponents(AppMode.DISABLED, AppStatus.CREATED);
        } else if(withMultipleAppComponents && withDataManagementComponent) {
            app = generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.DISABLED, AppStatus.CREATED);
        } else {
            app = generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);
        }
        app.setId(UUID.randomUUID());
        app.setCompositionId(UUID.randomUUID());
        app.setMode(AppMode.ENABLED);

        return AppInstances.builder()
                .id(UUID.randomUUID())
                .compositionInstanceId(UUID.randomUUID())
                .status(status)
                .app(app)
                .build();
    }

    public static List<AppInstances> generateAppInstanceEntities(final int numberOfAppInstanceEntities,
                                                                 final AppInstanceStatus status) {
        final List<AppInstances> appInstances = new ArrayList<>();
        final List<App> apps = TestUtils.generateAppEntities(numberOfAppInstanceEntities, AppMode.ENABLED);

        for (final App app : apps) {
            final AppInstances appInstance = TestUtils.generateAppInstanceEntity();
            appInstance.setApp(app);
            appInstance.setStatus(status);
            appInstances.add(appInstance);
        }

        return appInstances;
    }

    public static AppComponentInstance generateAppComponentInstanceEntity(final AppInstances appInstances, final UUID appId, final UUID appComponentInstanceId) {
        return AppComponentInstance.builder()
            .id(appComponentInstanceId)
            .appInstance(appInstances)
            .appId(appId)
            .compositionElementInstanceId(appComponentInstanceId)
            .build();
    }

    public static AppDetails createApp() {
        final Href linksHref = new Href().href("app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230");
        return new AppDetails()
                .id(EEFD_22230)
                .type(APP_ENTITY_TYPE)
                .provider(APP_PROVIDER)
                .name(APP_ENTITY_NAME)
                .version(APP_VERSION_1_1_1)
                .mode(AppMode.DISABLED)
                .status(AppStatus.CREATED)
                .createdAt("2023-04-06T00:04:16.711+00:00")
                .self(linksHref);
    }
    public static CompositionInstanceData generateValidCompositionInstanceData() {

        final App app = generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.DISABLED, AppStatus.CREATED);

        app.setCompositionId(UUID.fromString(COMPOSITION_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT));

        final AppInstances appInstances  =  AppInstances.builder()
            .id(UUID.fromString(INSTANCE_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT))
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.UNDEPLOYED)
            .app(app)
            .build();

        final ClientCredential clientCredential = ClientCredential.builder()
            .clientId(CLIENT_ID_NAME)
            .clientSecret(CLIENT_SECRET)
            .clientUrl(KEYCLOAK_CLIENT_URL_KEY_VALUE)
            .appInstance(appInstances)
            .build();

        final List<ClientCredential> clientCredentialList  = new ArrayList<>();
        clientCredentialList.add(clientCredential);
        appInstances.setClientCredentials(clientCredentialList);

        // Set App Component Instances
        final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();

        for (AppComponent appComponent : appInstances.getApp().getAppComponents()) {
            AppComponentInstance appComponentInstance = createAppComponentInstanceForTestCase(appComponent, appInstances);
            appComponentInstance.setAppInstance(appInstances);
            appComponentInstancesList.add(appComponentInstance);
        }
        appInstances.setAppComponentInstances(appComponentInstancesList);

        // Set the componentInstance data properties
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances);

        return compositionInstanceData;

    }

    public static CompositionInstanceData generateInvalidCompositionInstanceData() {

        final App app = generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);

        app.setCompositionId(UUID.fromString(COMPOSITION_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT));

        final AppInstances appInstances  =  AppInstances.builder()
            .id(UUID.randomUUID())
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.UNDEPLOYED)
            .app(app)
            .build();

        final ClientCredential clientCredential = ClientCredential.builder()
            .clientId(CLIENT_ID_NAME)
            .clientSecret(CLIENT_SECRET)
            .clientUrl(KEYCLOAK_CLIENT_URL_KEY_VALUE)
            .appInstance(appInstances)
            .build();

        final List<ClientCredential> clientCredentialList  = new ArrayList<>();
        clientCredentialList.add(clientCredential);
        appInstances.setClientCredentials(clientCredentialList);

        // Set App Component Instances
        final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();

        AppComponentInstance appComponentInstance = new AppComponentInstance();
        appComponentInstance.setAppInstance(null);
        appComponentInstancesList.add(appComponentInstance);

        appInstances.setAppComponentInstances(appComponentInstancesList);

        // Set the componentInstance data properties
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances,new ArrayList<>());

        return compositionInstanceData;

    }

    public static CompositionInstanceData generateValidCompositionInstanceDataWithComponentInstancesProperties() {

        final App app = generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);

        app.setCompositionId(UUID.fromString(COMPOSITION_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT));

        final AppInstances appInstances  =  AppInstances.builder()
            .id(UUID.fromString(INSTANCE_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT))
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.UNDEPLOYED)
            .app(app)
            .build();

        final ClientCredential clientCredential = ClientCredential.builder()
            .clientId(CLIENT_ID_NAME)
            .clientSecret(CLIENT_SECRET)
            .clientUrl(KEYCLOAK_CLIENT_URL_KEY_VALUE)
            .appInstance(appInstances)
            .build();

        final List<ClientCredential> clientCredentialList  = new ArrayList<>();
        clientCredentialList.add(clientCredential);
        appInstances.setClientCredentials(clientCredentialList);

        // Set App Component Instances
        final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();

        for (AppComponent appComponent : appInstances.getApp().getAppComponents()) {
            AppComponentInstance appComponentInstance = createAppComponentInstanceForTestCase(appComponent, appInstances);
            appComponentInstance.setAppInstance(appInstances);
            appComponentInstancesList.add(appComponentInstance);
        }
        appInstances.setAppComponentInstances(appComponentInstancesList);

        // Create a list to represent componentInstancesProperties
        List<ComponentInstances> componentInstancesProperties = new ArrayList<>();
        ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName(app.getAppComponents().get(0).getName());
        componentInstance.setDeployState(AppInstanceStatus.DEPLOYED.name());
        componentInstance.setVersion(app.getAppComponents().get(0).getVersion());
        componentInstance.setType(app.getAppComponents().get(0).getType());
        final Map<String, Object> userDefinedPropertyMap = new HashMap<>();
        userDefinedPropertyMap.put(REPLICA_COUNT_KEY, "1");
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put(TIMEOUT_KEY, 10);
        componentPropertyMap.put(NAMESPACE_KEY, "valid-ns");
        componentPropertyMap.put(USER_DEFINED_HELM_PARAMETERS, userDefinedPropertyMap);
        componentInstance.setProperties(componentPropertyMap);
        componentInstancesProperties.add(componentInstance);


        // Set the componentInstance data properties
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances, componentInstancesProperties);

        return compositionInstanceData;

    }

    public static CompositionInstanceData generateInvalidCompositionInstanceDataWithComponentInstancesProperties() {

        final App app = generateAppEntity(AppMode.DISABLED, AppStatus.CREATED);

        app.setCompositionId(UUID.fromString(COMPOSITION_ID_WHEN_COMPOSITION_INSTANCE_DATA_IS_PRESENT));

        final AppInstances appInstances  =  AppInstances.builder()
            .id(UUID.randomUUID())
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.UNDEPLOYED)
            .app(app)
            .build();

        final ClientCredential clientCredential = ClientCredential.builder()
            .clientId(CLIENT_ID_NAME)
            .clientSecret(CLIENT_SECRET)
            .clientUrl(KEYCLOAK_CLIENT_URL_KEY_VALUE)
            .appInstance(appInstances)
            .build();

        final List<ClientCredential> clientCredentialList  = new ArrayList<>();
        clientCredentialList.add(clientCredential);
        appInstances.setClientCredentials(clientCredentialList);

        // Set App Component Instances
        final List<AppComponentInstance> appComponentInstancesList = new ArrayList<>();

        AppComponentInstance appComponentInstance = new AppComponentInstance();
        appComponentInstance.setAppInstance(null);
        appComponentInstancesList.add(appComponentInstance);

        appInstances.setAppComponentInstances(appComponentInstancesList);

        List<ComponentInstances> componentInstancesProperties = new ArrayList<>();
        ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName(app.getAppComponents().get(0).getName());
        componentInstance.setDeployState(AppInstanceStatus.DEPLOYED.name());
        componentInstance.setVersion(app.getAppComponents().get(0).getVersion());
        componentInstance.setType(app.getAppComponents().get(0).getType());
        final Map<String, Object> userDefinedPropertyMap = new HashMap<>();
        userDefinedPropertyMap.put("replicaCountWrong", "2");
        userDefinedPropertyMap.put("pullPolicyWrong", "IfNotPresent");
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put(TIMEOUT_KEY, 10);
        componentPropertyMap.put(USER_DEFINED_HELM_PARAMETERS, userDefinedPropertyMap);
        componentInstance.setProperties(componentPropertyMap);
        componentInstancesProperties.add(componentInstance);

        // Set the componentInstance data properties
        CompositionInstanceData compositionInstanceData = new CompositionInstanceData(appInstances, componentInstancesProperties);

        return compositionInstanceData;

    }


    public static AppComponentInstance createAppComponentInstanceForTestCase(AppComponent appComponent, AppInstances appInstances) {
        AppComponentInstance appComponentInstance = new AppComponentInstance();
        appComponentInstance.setAppId(appInstances.getApp().getId());
        appComponentInstance.setAppComponent(appComponent);
        if (AppComponentTypeComparator.isDataManagementType(appComponent.getType())){
            appComponentInstance.setCompositionElementInstanceId(UUID.fromString(ELEMENT_INSTANCE_DATA_MANAGEMENT));
        } else {
            appComponentInstance.setCompositionElementInstanceId(UUID.fromString(EEFD_6C8CC));
        }
        appComponentInstance.setAppInstance(appInstances);
        return appComponentInstance;
    }

    public static AppInstance getAppInstance() {
        return getAppInstance(UUID.fromString(EEFD_22230), UUID.fromString(EEFD_22230),
                AppInstanceStatus.DEPLOYED);
    }

    public static AppInstance getDummyAppInstance() {
        return getDummyAppInstanceDetails();
    }

    public static AppInstance getDummyAppInstanceWithMultipleComponents() {
        return getDummyAppInstanceWithMultipleComponentsDetails();
    }

    public static AppInstance getAppInstance(UUID instanceId, UUID appId, AppInstanceStatus appInstanceStatus) {
        return new AppInstance()
            .id(instanceId.toString())
            .appId(appId.toString())
            .status(appInstanceStatus)
            .createdAt(String.valueOf(OffsetDateTime.now()));
    }

    public static AppInstance getAppInstance(final boolean timestamp, final boolean additionalParameters, final AppInstanceStatus status) {
        AppInstance appInstance = new AppInstance().
                id("7e151de6-18a9-4770-be4f-354b620f0035")
                .appId("26471a81-1de4-4ad9-9724-326eefd22230")
                .status(status);

        List<ComponentInstances> componentInstanceList = new ArrayList<>();
        final Map<String, Object> componentInstanceProperties = new HashMap<>();
        componentInstanceProperties.put(TIMEOUT_KEY, 5);
        if (additionalParameters) {
            final Map<String, Object> userDefinedHelmParameters = new HashMap<>();
            userDefinedHelmParameters.put(APP_INSTANCE_REPLICA_COUNT_KEY, APP_INSTANCE_REPLICA_COUNT_VALUE);
            componentInstanceProperties.put(USER_DEFINED_HELM_PARAMETERS, userDefinedHelmParameters);
        }
        ComponentInstances componentInstance = new ComponentInstances().type(MICROSERVICE)
            .deployState(TestConstants.COMPOSITION_INSTANCE_STATUS_DEPLOYED)
            .properties(componentInstanceProperties);
        componentInstanceList.add(componentInstance);
        appInstance.setComponentInstances(componentInstanceList);
        if (timestamp) {
            appInstance.createdAt(String.valueOf(OffsetDateTime.now()));
        }
        appInstance.setSelf(new Href().href(HTTP_APP_MANAGER_URL_APP_INSTANCE));
        appInstance.setApp(new Href().href(HTTP_APP_MANAGER_URL_APP));

        return appInstance;
    }

    public static List<AppDetails> getAppDetails() {
        return getDummyAppsDetails();
    }

    public static AppDetails getAppDetailsById() {
        return getDummyAppDetailsById();
    }

    public static AcCommissionResponse generateCreateAutomationCompositionResponse() {
        final ToscaIdentifier toscaIdentifier = new ToscaIdentifier(LCM_PARTICIPANT, APP_VERSION_1_1_1);
        return new AcCommissionResponse(COMPOSITION_ID, List.of(toscaIdentifier));
    }

    public static AppInstanceOperationResponse getAppInstanceDetails() throws URISyntaxException {
        final AppInstanceOperationResponse result = new AppInstanceOperationResponse();
        final AppInstanceOperationResponseAppInstance appInstance = new AppInstanceOperationResponseAppInstance();

        appInstance.setHref("https://localhost:8080/app-manager/lcm/app-lcm/v3/app-instances/4f7ed323-2923-43d7-928e-ee79c34701aa");
        appInstance.setStatus(AppInstanceStatus.UPDATING);
        return result;
    }

    public static AppInstanceManagementResponse getAppInstanceManagementDetails(AppInstanceStatus appInstanceStatus) throws URISyntaxException {
        final AppInstanceManagementResponse appInstanceManagementResponse = new AppInstanceManagementResponse();
        final AppInstanceOperationResponseAppInstance appInstance = new AppInstanceOperationResponseAppInstance();
        appInstance.setHref("app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035");
        appInstance.setStatus(appInstanceStatus);
        appInstanceManagementResponse.setAppInstance(appInstance);
        if(!appInstanceStatus.equals(AppInstanceStatus.UNDEPLOYING)) {
            AppInstanceManagementRequestAdditionalData additionalData = new AppInstanceManagementRequestAdditionalData();
            final ComponentInstances componentInstance = new ComponentInstances();
            componentInstance.setName("eric-oss-5gcnr");
            final Map<String, Object> properties = new HashMap<>();
            properties.put("timeout", 5);
            final Map<String, Object> userDefined = new HashMap<>();
            userDefined.put("replicaCount", 1);
            properties.put("userDefinedHelmParameters", userDefined);
            componentInstance.setProperties(properties);
            additionalData.setComponentInstances(List.of(componentInstance));
            appInstanceManagementResponse.setAdditionalData(additionalData);
        }
        appInstanceManagementResponse.setType(AppInstanceManagementResponse.TypeEnum.DEPLOY);
        return appInstanceManagementResponse;
    }

    public static ClientDto[] getClientDtos() {
        ClientDto clientDto = getClientDto();
        return new ClientDto[]{clientDto};
    }

    public static ClientDto getClientDto() {
        ClientDto clientDto1 = new ClientDto();
        clientDto1.setClientId(KEYCLOAK_CLIENT_ID_KEY);
        clientDto1.setName(SCOPE);
        clientDto1.setId("1");
        return clientDto1;
    }

    public static ClientRoleDTO[] getClientRolesDtos() {
        ClientRoleDTO clientRoleDTO1 = new ClientRoleDTO();
        clientRoleDTO1.setId("1");
        clientRoleDTO1.setName(USER);
        ClientRoleDTO clientRoleDTO2 = new ClientRoleDTO();
        clientRoleDTO2.setId("2");
        clientRoleDTO2.setName(ADMIN);
        return new ClientRoleDTO[]{clientRoleDTO1, clientRoleDTO2};
    }

    public static ServiceAccountDto getServiceAccount() {
        ServiceAccountDto serviceAccountDto = new ServiceAccountDto();
        serviceAccountDto.setId("1");
        serviceAccountDto.setUsername(ADMIN);
        return serviceAccountDto;
    }

    public static ClientCredential getClientCredential() {
        return ClientCredential.builder()
                .id(new Random().nextLong())
                .clientId(KEYCLOAK_CLIENT_ID_KEY)
                .clientSecret("clientSecret")
                .clientScope(SCOPE)
                .appInstance(AppInstances.builder().build())
                .build();
    }

    public static ClientScopeDto getClientScopeDto() {
        final ClientScopeDto clientScopeDto = new ClientScopeDto();
        final AttributeScopeDto attributes = new AttributeScopeDto();
        attributes.setScreen("screen");
        attributes.setScope(SCOPE);
        clientScopeDto.setAttributes(attributes);
        clientScopeDto.setName("name");
        clientScopeDto.setProtocol("protocol");
        return clientScopeDto;
    }

    public static AppInstances createAppInstance() {
        final AppInstances appInstances = AppInstances.builder()
                .id(UUID.randomUUID())
                .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
                .status(AppInstanceStatus.UNDEPLOYED)
                .build();

        final List<ClientCredential> clientCredentials = new ArrayList<>();
        ClientCredential clientCredential = ClientCredential.builder()
                .clientId(KEYCLOAK_CLIENT_ID_KEY)
                .clientSecret(KEYCLOAK_CLIENT_SECRET_KEY)
                .clientScope(KEYCLOAK_SCOPE_KEY)
                .clientUrl("http://localhost:8080/iam-url")
                .appInstance(appInstances)
                .build();
        clientCredentials.add(clientCredential);
        appInstances.setClientCredentials(clientCredentials);
        return appInstances;
    }

    public static AppComponentInstance getComponentInstance(){
        return AppComponentInstance.builder()
                .id(UUID.randomUUID())
                .compositionElementInstanceId(UUID.randomUUID())
                .build();
    }

    /**
     * Init object store container.
     *
     * @return the object store container
     */
    public static ObjectStoreContainer initObjectStoreContainer() {
        final ObjectStoreContainer objectStoreContainer = new ObjectStoreContainer(OBJECT_STORE_ACCESS_KEY, OBJECT_STORE_SECRET_KEY);
        objectStoreContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(objectStoreContainer::stop));

        // The ObjectStoreContainer's mapped port is not fixed and changes
        // each time a new minio docker container is started. For integration
        // tests we need to set the mapped port in the minioClient, and it is set as a
        // system property to override the fixed port value 9000 configured in the
        // properties yaml file
        final int mappedPort = objectStoreContainer.getPort();
        System.setProperty("object-store.port", String.valueOf(mappedPort));

        return objectStoreContainer;
    }

    public static Map<String, Object> jsonFileToMap(String path) throws IOException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getClasspathResourceAsString(path), new TypeReference<Map<String, Object>>() {});
    }

    public ProblemDetails generateErrorResponse(final String errorMessage, final String title, final String type,
                                                 final int status) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setTitle(title);
        problemDetails.setStatus(status);
        problemDetails.setDetail(errorMessage);
        return problemDetails;
    }

    public static TokenDto getTokenDto(){
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken("accessToken");
        return tokenDto;
    }

    public static ComponentInstances createAsdComponentInstance(final App appEntity, final AppInstanceStatus appInstanceStatus){
        final ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName(appEntity.getAppComponents().get(0).getName());
        componentInstance.setDeployState(appInstanceStatus.name());
        componentInstance.setVersion(appEntity.getAppComponents().get(0).getVersion());
        componentInstance.setType(appEntity.getAppComponents().get(0).getType());
        final Map<String, Object> userDefinedPropertyMap = new HashMap<>();
        userDefinedPropertyMap.put(REPLICA_COUNT_KEY, "1");
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put(TIMEOUT_KEY, 10);
        componentPropertyMap.put(USER_DEFINED_HELM_PARAMETERS, userDefinedPropertyMap);
        componentInstance.setProperties(componentPropertyMap);
        return componentInstance;
    }

    public static ComponentInstances createDataManagementComponentInstanceWithInvalidPropertiesForUpdate(final App appEntity, final AppInstanceStatus appInstanceStatus)
        throws Exception {
        final ComponentInstances componentInstance = new ComponentInstances();
        final List<AppComponent> appComponents = appEntity.getAppComponents();
        final Optional<AppComponent> appComponentOptional = appComponents.stream().filter(component -> AppComponentTypeComparator.isDataManagementType(component.getType())).findFirst();

        final AppComponent appComponent;
        if (appComponentOptional.isPresent()) {
            appComponent = appComponentOptional.get();
        } else {
            throw new Exception("Test setup incorrectly");
        }
        componentInstance.setName(appComponent.getName());
        componentInstance.setDeployState(appInstanceStatus.name());
        componentInstance.setVersion(appComponent.getVersion());
        componentInstance.setType(appComponent.getType());
        final Map<String, Object> componentPropertyMap = new HashMap<>();
        componentPropertyMap.put("not_an_updatable_property", "some value");
        componentInstance.setProperties(componentPropertyMap);
        return componentInstance;
    }

    public static List<ComponentInstances> createMultipleComponentInstanceList(final App appEntity, final AppInstanceStatus appInstanceStatus){
        List<ComponentInstances> componentInstances = new ArrayList<>();
        for (AppComponent appComponent : appEntity.getAppComponents()){
            ComponentInstances componentInstance = new ComponentInstances();
            componentInstance.setName(appComponent.getName());
            componentInstance.setDeployState(appInstanceStatus.name());
            componentInstance.setVersion(appComponent.getVersion());
            componentInstance.setType(appComponent.getType());
            componentInstances.add(componentInstance);
        }

        return componentInstances;
    }

    private static String generateRAppId(App appUnderTest) {
        return getNonNullString(appUnderTest.getType()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(appUnderTest.getProvider()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(appUnderTest.getName()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(appUnderTest.getVersion()).toLowerCase(Locale.ROOT).replace(".", "-");
    }

    private static String getNonNullString(String value) {
        return value == null ? "" : value;
    }
}