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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * Artifact DTO modelling the artifact entity from App Onboarding. Used for App Onboarding appArtifact endpoint responses.
 * <p>
 * Artifacts belong to an App {@link AppDto}.
 */
public class ArtifactDto {
    private static final String NEW_LINE = "\n";

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("version")
    private String version;

    /**
     * Gets or Sets status.
     */
    public enum StatusEnum {

        PENDING("PENDING"),

        FAILED("FAILED"),

        COMPLETED("COMPLETED");

        private final String value;

        StatusEnum(final String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(final String value) {
            for (final StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    @JsonProperty("status")
    private StatusEnum status;

    @JsonProperty("location")
    private String location;

    @JsonProperty("errorReponse")
    private String errorReponse;

    public ArtifactDto id(final Long id) {
        this.id = id;
        return this;
    }

    /**
     * Get id.
     *
     * @return id
     */
    @ApiModelProperty(example = "12", value = "")

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ArtifactDto name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name.
     *
     * @return name
     */
    @ApiModelProperty(example = "app-lcm", value = "")

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArtifactDto type(final String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type.
     *
     * @return type
     */
    @ApiModelProperty(example = "model", value = "")

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public ArtifactDto version(final String version) {
        this.version = version;
        return this;
    }

    /**
     * Get version.
     *
     * @return version
     */
    @ApiModelProperty(example = "6.1.0+66", value = "")

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public ArtifactDto status(final StatusEnum status) {
        this.status = status;
        return this;
    }

    /**
     * Get status.
     *
     * @return status
     */
    @ApiModelProperty(example = "Success", value = "")

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(final StatusEnum status) {
        this.status = status;
    }

    public ArtifactDto location(final String location) {
        this.location = location;
        return this;
    }

    /**
     * Get location.
     *
     * @return location
     */
    @ApiModelProperty(example = "localhost", value = "")

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public ArtifactDto errorReponse(final String errorReponse) {
        this.errorReponse = errorReponse;
        return this;
    }

    /**
     * Get errorReponse.
     *
     * @return errorReponse
     */
    @ApiModelProperty(example = "No artifacts present", value = "")

    public String getErrorReponse() {
        return errorReponse;
    }

    public void setErrorReponse(final String errorReponse) {
        this.errorReponse = errorReponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ArtifactDto artifacts = (ArtifactDto) o;
        return Objects.equals(id, artifacts.id) && Objects.equals(name, artifacts.name) && Objects.equals(type, artifacts.type)
                && Objects.equals(version, artifacts.version) && Objects.equals(status, artifacts.status)
                && Objects.equals(location, artifacts.location) && Objects.equals(errorReponse, artifacts.errorReponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, version, status, location, errorReponse);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class ArtifactDto {\n");

        sb.append("    id: ").append(toIndentedString(id)).append(NEW_LINE);
        sb.append("    name: ").append(toIndentedString(name)).append(NEW_LINE);
        sb.append("    type: ").append(toIndentedString(type)).append(NEW_LINE);
        sb.append("    version: ").append(toIndentedString(version)).append(NEW_LINE);
        sb.append("    status: ").append(toIndentedString(status)).append(NEW_LINE);
        sb.append("    location: ").append(toIndentedString(location)).append(NEW_LINE);
        sb.append("    errorReponse: ").append(toIndentedString(errorReponse)).append(NEW_LINE);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(final Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace(NEW_LINE, "\n    ");
    }
}