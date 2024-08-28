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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.Artifact;
import com.ericsson.oss.ae.acm.persistence.entity.Permission;
import com.ericsson.oss.ae.acm.persistence.entity.Role;
import com.ericsson.oss.ae.constants.KeycloakConstants;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;
import com.ericsson.oss.ae.v3.api.model.Href;

/**
 * AppDetailsMapper for mapping data objects with App entity.
 */
@Component
@AllArgsConstructor
@Slf4j
public class AppDetailsMapper {

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final LcmUrlGenerator lcmUrlGenerator;

    /**
     * Maps app entity with AppDetails data object for create App.
     *
     * @param app
     *     - app entity details
     * @return AppDetails
     */
    public AppDetails createAppResponseFromApp(final App app) {
        log.info("Generate create appDetails response for the saved app");
        final AppDetails appDetails = new AppDetails();
        appDetails.setId(app.getId().toString());
        appDetails.setMode(app.getMode());
        appDetails.setStatus(app.getStatus());
        appDetails.setName(app.getName());
        appDetails.setType(app.getType());
        appDetails.setProvider(app.getProvider());
        appDetails.setVersion(app.getVersion());
        appDetails.createdAt(String.valueOf(app.getCreatedAt()));
        return appDetails;
    }

    /**
     * Maps app entity with AppDetails data object for get App details.
     *
     * @param app
     *     - app entity details
     * @return AppDetails
     */
    public AppDetails fromApp(final App app) {
        log.info("Generate appDetails for the saved app with ID: {}", app.getId());
        AppDetails appDetails = modelMapper.map(app, AppDetails.class);
        removeArtifactLocationFromAppDetails(appDetails);
        appDetails.setId(app.getId().toString());
        appDetails.setSelf(new Href().href(lcmUrlGenerator.getAppsUrlById(app.getId())));
        return appDetails;
    }

    /**
     * Maps AppDetails data object with app entity.
     *
     * @param createAppRequest
     *     - createAppRequest details for mapping to entity
     * @return App
     */
    public App toApp(final CreateAppRequest createAppRequest) {
        log.info("Generating app entity with the app request details");
        final App app = App.builder()
            .name(createAppRequest.getName()).version(createAppRequest.getVersion()).type(createAppRequest.getType())
            .mode(AppMode.DISABLED).status(AppStatus.CREATED).provider(createAppRequest.getProvider())
            .rAppId(createRAppId(createAppRequest)).build();
        final List<AppComponent> appComponents = new ArrayList<>();
        createAppRequest.getComponents().stream().forEach(component -> {
            AppComponent appComponent = modelMapper.map(component, AppComponent.class);
            setCompositionElementNameByAppComponent(appComponent);
            appComponent.setApp(app);
            final List<Artifact> artifacts = component.getArtifacts().stream().map(artifact -> {
                final Artifact appArtifact = modelMapper.map(artifact, Artifact.class);
                appArtifact.setAppComponent(appComponent);
                return appArtifact;
            }).collect(Collectors.toList());
            appComponent.setArtifacts(artifacts);
            appComponents.add(appComponent);
        });

        List<Permission> permissions = this.getDefaultKafkaPermissions(app);
        if (createAppRequest.getPermissions() != null) {
            createAppRequest.getPermissions().stream()
                .filter(permission -> !permission.getResource().equalsIgnoreCase(KeycloakConstants.KAFKA))
                .map(permission -> modelMapper.map(permission, Permission.class))
                .peek(permissionEnt -> permissionEnt.setApp(app))
                .forEach(permissions::add);
        }

        List<Role> roles = null;
        if (createAppRequest.getRoles() != null) {
            roles = createAppRequest.getRoles().stream()
                .map(role -> {
                final Role appRole = modelMapper.map(role, Role.class);
                appRole.setApp(app);
                return appRole;
            }).collect(Collectors.toList());
        }

        app.setAppComponents(appComponents);
        app.setPermissions(permissions);
        app.setRoles(roles);
        log.debug("Generated app entity : {}", app.toString());
        return app;
    }

    /**
     * Maps app entity with AppDetails data object.
     *
     * @param entityList
     *     - list of app entity details
     * @return AppDetails
     */
    public List<AppDetails> toAppDetailsList(final List<App> entityList) {
        log.info("Generating List of app details from App entity list");
        List<AppDetails> appDetailsList = new ArrayList<>();

        for (App app : entityList) {
            AppDetails tempAppDetails;

            tempAppDetails = modelMapper.map(app, AppDetails.class);
            tempAppDetails.setId(app.getId().toString());
            tempAppDetails.setSelf(new Href().href(lcmUrlGenerator.getAppsUrlById(app.getId())));
            appDetailsList.add(tempAppDetails);
        }
        return appDetailsList;
    }

    private void removeArtifactLocationFromAppDetails(final AppDetails appDetails) {
        appDetails.getComponents().stream().forEach(component -> component.getArtifacts().stream().forEach(artifact -> artifact.setLocation(null)));
    }

    private void setCompositionElementNameByAppComponent(final AppComponent appComponent) {
        if (appComponent.getType().equalsIgnoreCase(ValidAppComponentType.MICROSERVICE.name()) || appComponent.getType()
            .equalsIgnoreCase(ValidAppComponentType.ASD.name())) {
            appComponent.setCompositionElementName(AUTOMATION_COMPOSITION_ELEMENT);
        } else if (appComponent.getType().equalsIgnoreCase(ValidAppComponentType.DATAMANAGEMENT.name())) {
            appComponent.setCompositionElementName(AUTOMATION_COMPOSITION_ELEMENT_DATAMANAGEMENT);
        }
    }

    private List<Permission> getDefaultKafkaPermissions(final App app) {
        final List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.builder().resource(KeycloakConstants.KAFKA).scope(KeycloakConstants.SCOPE_GLOBAL).app(app).build());
        return permissions;
    }

    private String createRAppId(CreateAppRequest createAppRequest) {
        return getNonNullString(createAppRequest.getType()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(createAppRequest.getProvider()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(createAppRequest.getName()).toLowerCase(Locale.ROOT) + "-" +
               getNonNullString(createAppRequest.getVersion()).toLowerCase(Locale.ROOT).replace(".", "-");
    }

    private String getNonNullString(String value) {
        return value == null ? "" : value;
    }

}
