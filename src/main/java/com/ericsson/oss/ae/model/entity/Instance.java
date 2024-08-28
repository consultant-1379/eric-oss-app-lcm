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

package com.ericsson.oss.ae.model.entity;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.Data;

/**
 * Abstract entity class to represent an Instance.
 * <p>
 * This class is extended by AppInstance {@link AppInstance} and ArtifactInstance {@link ArtifactInstance} .
 */
@Data
@MappedSuperclass
public abstract class Instance {

    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus = HealthStatus.PENDING;

    private ZonedDateTime createdTimestamp;

    private ZonedDateTime updatedTimestamp;

    @PrePersist
    void createdAt() {
        createdTimestamp = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void updatedAt() {
        updatedTimestamp = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
