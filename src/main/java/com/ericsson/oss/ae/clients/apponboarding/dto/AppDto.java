/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.ae.clients.apponboarding.dto;

import com.ericsson.oss.ae.clients.keycloak.dto.RoleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * App DTO modelling the app entity from App Onboarding. Used for App Onboarding's '/app' endpoint responses.
 * <p>
 * Each {@link AppDto} contains a list of {@link ArtifactDto}.
 */
public class AppDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("version")
    private String version;

    @JsonProperty("size")
    private String size;

    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("type")
    private String type;

    @JsonProperty("onboardedDate")
    private String onboardedDate;


    @JsonProperty("status")
    private String status;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("artifacts")
    private List<ArtifactDto> artifacts = new ArrayList<>();

    @JsonProperty("events")
    private List events = new ArrayList<>();

    @JsonProperty("permissions")
    private List<Permission> permissions = new ArrayList<>();

    @JsonProperty("roles")
    private List<RoleDto> roles = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getOnboardedDate() {
        return onboardedDate;
    }

    public void setOnboardedDate(final String onboardedDate) {
        this.onboardedDate = onboardedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public List<ArtifactDto> getArtifacts() {
        return new ArrayList<>(artifacts);
    }

    public void setArtifacts(final List<ArtifactDto> artifacts) {
        this.artifacts = new ArrayList<>(artifacts);
    }

    public List<ApplicationEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public void setEvents(final List events) {
        this.events = new ArrayList(events);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = new ArrayList<>(permissions);
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }
}