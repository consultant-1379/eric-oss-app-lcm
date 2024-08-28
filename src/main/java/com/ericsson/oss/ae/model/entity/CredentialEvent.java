/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import lombok.*;

import jakarta.persistence.*;

/**
 * Entity class used to represent an AppInstance.
 */
@Data
@Entity
@Table(name = "credential_event")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialEvent extends Instance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="client_id", unique=true)
    private String clientId;

    @Column(name="app_on_boarding_app_id", unique=true)
    private Long appOnBoardingAppId;

    @Column(name="app_instance_id", unique=true)
    private Long appInstanceId;

    @Column(name="client_secret")
    private String clientSecret;

    @Column(name="client_scope")
    private String clientScope;

    @Column(name="deletion_status")
    private String deletionStatus;
}