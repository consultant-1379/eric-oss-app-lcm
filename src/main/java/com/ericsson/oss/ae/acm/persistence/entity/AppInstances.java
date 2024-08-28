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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Entity class used for an AppInstances.
 */

@Entity
@Table(name = "app_instance", schema = AppLcmConstants.DB_ACM_SCHEMA)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AppInstances extends TimestampGenerator {

    @Id
    @Column(unique = true, nullable = false, length = AppLcmConstants.UUID_FIELD_LENGTH)
    private UUID id;

    @Column(name = "composition_instance_id")
    private UUID compositionInstanceId;

    @Enumerated(EnumType.STRING)
    private AppInstanceStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id")
    private App app;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_app_id")
    private App targetApp;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "appInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientCredential> clientCredentials;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "appInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppInstanceEvent> appInstanceEvents;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "appInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppComponentInstance> appComponentInstances;
}
