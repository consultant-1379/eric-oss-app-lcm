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

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Entity class used for an App.
 */

@Entity
@Table(name = "app", schema = AppLcmConstants.DB_ACM_SCHEMA)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class App extends TimestampGenerator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, length = AppLcmConstants.UUID_FIELD_LENGTH)
    private UUID id;

    @NotNull
    @Column(name = "composition_id")
    private UUID compositionId;

    private String name;

    private String version;

    private String type;

    @Column(name = "rapp_id")
    private String rAppId;

    private String provider;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppMode mode = AppMode.DISABLED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppStatus status = AppStatus.CREATED;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AppInstances> appInstances;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Permission> permissions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Role> roles;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AppComponent> appComponents;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AppEvent> appEvents;
}
