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

package com.ericsson.oss.ae.acm.persistence.entity;

import java.sql.Timestamp;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract entity class to represent timestamp details.
 * <p>
 * This class is extended by App {@link App}, Credential {@link ClientCredential} and AppInstances {@link AppInstances} .
 */

@Getter
@Setter
@MappedSuperclass
public abstract class TimestampGenerator {

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    void createdAt() {
        Timestamp dateTime = Timestamp.from(Instant.now());
        createdAt = dateTime;
        updatedAt = dateTime;
    }

    @PreUpdate
    void updatedAt() {
        updatedAt = Timestamp.from(Instant.now());
    }
}
