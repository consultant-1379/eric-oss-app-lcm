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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTOMATION_COMPOSITION_ELEMENT;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.acm.TestConstants;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.Permission;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AppDetailsMapperTest {

    @Autowired
    private ModelMapper mockModelMapper;

    @Autowired
    private LcmUrlGenerator lcmUrlGenerator;

    private AppDetailsMapper appMapperUnderTest;

    @BeforeEach
    void setUp() {
        appMapperUnderTest = new AppDetailsMapper(mockModelMapper, lcmUrlGenerator);
    }

    @Test
    public void testCreateAppResponseFromAppEntity() {
        final AppDetails result = appMapperUnderTest.createAppResponseFromApp(TestUtils.generateAppEntity());
        assertThat(result.getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(result.getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    public void testMapFromAppEntity() {
        final AppDetails result = appMapperUnderTest.fromApp(TestUtils.generateAppEntity());
        assertThat(result.getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(result.getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getComponents().size()).isEqualTo(1);
        assertThat(result.getPermissions().size()).isEqualTo(2);
        assertThat(result.getRoles().size()).isEqualTo(2);
    }

    @Test
    void testMapAppEntityDetails() {
        final App result = appMapperUnderTest.toApp(TestUtils.generateCreateAppRequest());
        assertThat(result.getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(result.getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(1);
        assertThat(result.getPermissions().size()).isEqualTo(3);
        assertThat(result.getRoles().size()).isEqualTo(1);
    }

    @Test
    void testMapAppEntityDetailsWithMicroserviceAndDataManagementComponents() {
        final App result = appMapperUnderTest.toApp(TestUtils.generateCreateAppRequestWithMultipleComponents());
        assertThat(result.getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(result.getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(2);
        assertThat(result.getAppComponents().get(0).getCompositionElementName()).isEqualTo(AUTOMATION_COMPOSITION_ELEMENT);
        assertThat(result.getAppComponents().get(1).getCompositionElementName()).isEqualTo(AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT);
        assertThat(result.getPermissions().size()).isEqualTo(3);
        assertThat(result.getRoles().size()).isEqualTo(1);
    }

    @Test
    public void testToAppDetailsList(){
        List<App> listOfMockedApps= new ArrayList<>();

        listOfMockedApps.add(TestUtils.generateAppEntity());
        listOfMockedApps.add(TestUtils.generateAppEntity());
        listOfMockedApps.add(TestUtils.generateAppEntity());
        listOfMockedApps.add(TestUtils.generateAppEntity());
        listOfMockedApps.add(TestUtils.generateAppEntity());

        final List<AppDetails> result = appMapperUnderTest.toAppDetailsList(listOfMockedApps);
    }

    @Test
    public void testToAppDetailsList_withDefaultPermissionRequested(){
        final CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();
        createAppRequest.addPermissionsItem(
            new com.ericsson.oss.ae.v3.api.model.Permission().resource(KeycloakConstants.KAFKA).scope(TestConstants.FOO));
        final App result = appMapperUnderTest.toApp(createAppRequest);
        final List<Permission> permissions = result.getPermissions();

        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(1);
        assertThat(permissions.size()).isEqualTo(3);
        final boolean containsDefaultPerm = permissions.stream().anyMatch(
            permission -> Objects.equals(permission.getResource(), KeycloakConstants.KAFKA) && Objects.equals(permission.getScope(),
                KeycloakConstants.SCOPE_GLOBAL));
        assertThat(containsDefaultPerm).isTrue();
    }

    @Test
    public void testToAppDetailsList_withNoDefaultPermissionRequested(){
        final App result = appMapperUnderTest.toApp(TestUtils.generateCreateAppRequest());
        final List<Permission> permissions = result.getPermissions();

        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(1);
        assertThat(permissions.size()).isEqualTo(3);
        final boolean containsDefaultPerm = permissions.stream().anyMatch(
            permission -> Objects.equals(permission.getResource(), KeycloakConstants.KAFKA) && Objects.equals(permission.getScope(),
                KeycloakConstants.SCOPE_GLOBAL));
        assertThat(containsDefaultPerm).isTrue();
    }

    @Test
    public void testToAppDetailsList_withNoPermissions(){
        final CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();
        createAppRequest.setPermissions(null);
        final App result = appMapperUnderTest.toApp(createAppRequest);
        final List<Permission> permissions = result.getPermissions();

        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(1);
        assertThat(permissions.size()).isEqualTo(1);
        assertThat(permissions.get(0).getResource()).isEqualTo(KeycloakConstants.KAFKA);
        assertThat(permissions.get(0).getScope()).isEqualTo(KeycloakConstants.SCOPE_GLOBAL);
    }

    @Test
    public void testToAppDetailsList_withNoRoles(){
        final CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();
        createAppRequest.setRoles(null);
        final App result = appMapperUnderTest.toApp(createAppRequest);

        assertThat(result.getName()).isEqualTo("eric-oss-hello-world-multiple-microservices-go-app");
        assertThat(result.getAppComponents().size()).isEqualTo(1);
        assertThat(result.getRoles()).isNull();
    }
}
