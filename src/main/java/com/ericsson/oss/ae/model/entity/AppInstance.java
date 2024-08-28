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

import java.util.List;

import jakarta.persistence.*;

import lombok.*;

/**
 * Entity class used to represent an AppInstance.
 */
@Data
@Entity
@Table(name = "app_instance")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInstance extends Instance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appOnBoardingAppId;

    @Enumerated(EnumType.STRING)
    private TargetStatus targetStatus = TargetStatus.INSTANTIATED;

    @OneToMany(mappedBy = "appInstance", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.ALL })
    @ToString.Exclude
    private List<ArtifactInstance> artifactInstances;

    private String additionalParameters;

}