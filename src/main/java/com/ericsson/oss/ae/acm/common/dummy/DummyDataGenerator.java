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

package com.ericsson.oss.ae.acm.common.dummy;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.MICROSERVICE;
import static reactor.netty.Metrics.ERROR;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementRequestAdditionalData;
import com.ericsson.oss.ae.v3.api.model.AppInstanceManagementResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceOperationResponseAppInstance;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponse;
import com.ericsson.oss.ae.v3.api.model.AppInstanceUpdateResponseAppInstance;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponse;
import com.ericsson.oss.ae.v3.api.model.AppOperationResponseApp;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.Artifact;
import com.ericsson.oss.ae.v3.api.model.Component;
import com.ericsson.oss.ae.v3.api.model.ComponentInstances;
import com.ericsson.oss.ae.v3.api.model.Credentials;
import com.ericsson.oss.ae.v3.api.model.Event;
import com.ericsson.oss.ae.v3.api.model.Href;
import com.ericsson.oss.ae.v3.api.model.Permission;
import com.ericsson.oss.ae.v3.api.model.Role;

public class DummyDataGenerator {
    public static List<AppDetails> getDummyAppsDetails() {
        return List.of(getDummyAppDetailsById());
    }

    private DummyDataGenerator(){}

    public static AppDetails getDummyAppDetailsById() {
        List<Component> appComponentList = new ArrayList<>();
        List<Permission> appPermissionList = new ArrayList<>();
        List<Role> appRoleList = new ArrayList<>();
        List<Event> appEventList = new ArrayList<>();
        List<Artifact> artifactList = new ArrayList<>();

        Artifact artifact1 = new Artifact().name("eric-oss-hello-world-go-app").type("HELM")
                .location("26471a81-1de4-4ad9-9724-326eefd22230/eric-oss-hello-world-go-app");

        Artifact artifact2 = new Artifact().name("docker.tar").type("IMAGE").location("26471a81-1de4-4ad9-9724-326eefd22230/docker");

        final Href linksHref = new Href().href("app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230");

        artifactList.add(artifact1);
        artifactList.add(artifact2);

        Component component = new Component().type(MICROSERVICE).name("eric-oss-hello-metrics-go-app").version("1.1.1")
                .artifacts(artifactList);

        appComponentList.add(component);

        Permission permission1 = new Permission().resource("nginx").scope("foo");

        Permission permission2 = new Permission().resource("mongoDB").scope("goodbyeWorld");

        appPermissionList.add(permission1);
        appPermissionList.add(permission2);

        Role role = new Role().name("admin");

        appRoleList.add(role);

        Event appEvent = new Event()
                .type(ERROR).title("Failed to enable app")
                .detail("PRIME request in ACM failed due to an authorization error")
                .createdAt(OffsetDateTime.parse("2023-04-12T18:06:57.886Z").toString());

        appEventList.add(appEvent);

        return new AppDetails().id("26471a81-1de4-4ad9-9724-326eefd22230").type("rApp")
                .name("eric-oss-hello-world-multiple-microservices-go-app").version("1.1.1").mode(AppMode.DISABLED).status(AppStatus.CREATED)
                .createdAt("2023-04-06T00:04:16.711Z").components(appComponentList).permissions(appPermissionList)
                .roles(appRoleList).events(appEventList).self(linksHref);
    }

    public static AppDetails createDummyApp() {
        Href href = new Href().href("app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230");

        return new AppDetails()
            .id("26471a81-1de4-4ad9-9724-326eefd22230")
            .type("rApp")
            .name("eric-oss-hello-world-multiple-microservices-go-app")
            .version("1.1.1")
            .mode(AppMode.DISABLED)
            .status(AppStatus.CREATED)
            .createdAt("2023-04-06T00:04:16.711+00:00")
            .self(href);
    }

    public static AppOperationResponse getAppOperationResponse(final UUID uuid, final AppMode appMode){
        AppOperationResponseApp appOperationResponseApp = new AppOperationResponseApp();
        appOperationResponseApp
                .id(uuid.toString())
                .href("app-lifecycle-management/v3/apps/" + uuid);

        return new AppOperationResponse()
                .mode(appMode)
                .app(appOperationResponseApp);
    }

    public static AppInstanceOperationResponse getDummyAppInstanceOperationResponse(final UUID appInstanceId) {
        return new AppInstanceOperationResponse()
                .appInstance(new AppInstanceOperationResponseAppInstance()
                        .status(AppInstanceStatus.DELETING)
                        .href("app-lifecycle-management/v3/app-instances/"+appInstanceId));

    }



    public static AppInstance getDummyCreateAppInstanceResponse(final UUID appInstanceId) {
        final Href hrefSelf = new Href().href("app-lifecycle-management/v3/app-instances/" + appInstanceId);
        final Href hrefApp = new Href().href("app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230");

        final Credentials credentials = new Credentials().clientId("rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825");

        Map<String, Object> userDefinedHelmParameters = new HashMap<>();
        userDefinedHelmParameters.put("replicaCount", "2");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", "5");
        properties.put("userDefinedHelmParameters", userDefinedHelmParameters);

        final List<ComponentInstances> componentInstancesList = new ArrayList<>();
        componentInstancesList.add(new ComponentInstances()
                .name("eric-oss-5gcnr")
                .version("1.2.3")
                .type("Microservice")
                .deployState("UNDEPLOYED")
                .properties(properties)
        );

        return new AppInstance()
                .id(appInstanceId.toString())
                .appId( "26471a81-1de4-4ad9-9724-326eefd22230")
                .status(AppInstanceStatus.UNDEPLOYED)
                .createdAt("2023-04-06T00:04:16.711+00:00")
                .updatedAt("2023-04-06T00:05:16.711+00:00")
                .credentials(credentials)
                .componentInstances(componentInstancesList)
                .events(new ArrayList<>())
                .self(hrefSelf)
                .app(hrefApp);
    }

    public static AppInstanceUpdateResponse getDummyAppInstanceUpdateOperationResponse() {
        AppInstanceUpdateResponse appInstanceUpdateOperationResponse = new AppInstanceUpdateResponse();
        AppInstanceUpdateResponseAppInstance appInstance = new AppInstanceUpdateResponseAppInstance();
        appInstance.setHref("app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035");
        appInstance.id("7e151de6-18a9-4770-be4f-354b620f0035");
        appInstanceUpdateOperationResponse.setAppInstance(appInstance);
        final ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName("eric-oss-5gcnr");
        componentInstance.setVersion("1.2.3");
        componentInstance.setDeployState("DEPLOYED");
        componentInstance.setType("Microservice");
        final Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 15);
        properties.put("namespace","default");
        final Map<String, Object> userDefined = new HashMap<>();
        userDefined.put("replicaCount", 5);
        properties.put("userDefinedHelmParameters", userDefined);
        componentInstance.setProperties(properties);
        final List<ComponentInstances> componentInstances = new ArrayList<>();
        componentInstances.add(componentInstance);
        appInstanceUpdateOperationResponse.setComponentInstances(componentInstances);
        return appInstanceUpdateOperationResponse;
    }

    public static AppInstanceManagementResponse getDummyAppInstanceManagementResponse(final AppInstanceStatus appInstanceStatus, AppInstanceManagementResponse.TypeEnum typeEnum){
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
        appInstanceManagementResponse.setType(typeEnum);
        return appInstanceManagementResponse;
    }

    public static AppInstanceManagementResponse getDummyAppInstanceUpgrade() {
        final AppInstanceManagementResponse appInstanceManagementResponse = getDummyAppInstanceManagementResponse(AppInstanceStatus.UPGRADING, AppInstanceManagementResponse.TypeEnum.UPGRADE);
        appInstanceManagementResponse.setTargetAppId("36471a81-1de4-4ad9-9724-426eefd44430");
        return appInstanceManagementResponse;
    }

    public static AppInstance getDummyAppInstanceDetails() {
        final AppInstance appInstance = new AppInstance();
        appInstance.setId("7e151de6-18a9-4770-be4f-354b620f0035");
        appInstance.setAppId("26471a81-1de4-4ad9-9724-326eefd22230");
        appInstance.setStatus(AppInstanceStatus.UNDEPLOYED);
        appInstance.setCreatedAt("2024-02-21 21:12:19.37507");
        appInstance.setUpdatedAt("2024-02-21 21:12:19.559007");
        final Credentials credentials = new Credentials();
        credentials.setClientId("rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825");
        appInstance.setCredentials(credentials);
        List<ComponentInstances> componentInstancesList = new ArrayList<>();
        final ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName("eric-oss-5gcnr");
        componentInstance.setVersion("1.0.77");
        componentInstance.setType("Microservice");
        componentInstance.deployState("UNDEPLOYED");
        final Map<String, Object> properties = new HashMap<>();
        properties.put("timeout", 5);
        properties.put("namespace", "hart098-eric-eic-0");
        componentInstance.setProperties(properties);
        componentInstancesList.add(componentInstance);
        appInstance.setComponentInstances(componentInstancesList);
        appInstance.setEvents(new ArrayList<>());
        appInstance.setSelf(new Href());
        appInstance.setApp(new Href());
        appInstance.getSelf().setHref("app-lifecycle-management/v3/app-instances/93030152-17bd-4d88-b8ef-7c143511b519");
        appInstance.getApp().setHref("app-lifecycle-management/v3/apps/3146ccdc-0323-4f34-8f3e-13b858c1c582");
        return appInstance;
    }

    public static AppInstance getDummyAppInstanceWithMultipleComponentsDetails() {
        final AppInstance appInstance = getDummyAppInstanceDetails();
        final ComponentInstances componentInstance = new ComponentInstances();
        componentInstance.setName("data-management");
        componentInstance.setVersion("1.0.0");
        componentInstance.setType("DataManagement");
        componentInstance.deployState("UNDEPLOYED");
        final Map<String, Object> properties = new HashMap<>();
        properties.put("iamClientId", "rappid_3146ccdc-0323-4f34-8f3e-13b858c1c582_1708549936654_8c12ffcc-64c3-4070-b9b5-a115ded1f825");
        properties.put("artifacts", List.of(Map.of("name", "input-data-specification.json")));
        componentInstance.setProperties(properties);
        appInstance.getComponentInstances().add(componentInstance);
        return appInstance;
    }
}