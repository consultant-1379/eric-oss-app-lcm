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

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;

/**
 * Entity class used for an AppInstances.
 */

@Entity
@Table(name = "app_component_instance", schema = AppLcmConstants.DB_ACM_SCHEMA)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AppComponentInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, length = AppLcmConstants.UUID_FIELD_LENGTH)
    private UUID id;

    @Column(name = "app_id")
    private UUID appId;

    @Column(name = "composition_element_instance_id")
    private UUID compositionElementInstanceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_instance_id")
    private AppInstances appInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_component_id")
    private AppComponent appComponent;
}
