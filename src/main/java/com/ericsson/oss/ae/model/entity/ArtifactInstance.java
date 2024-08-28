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

import jakarta.persistence.*;

import lombok.*;

/**
 * Entity class used for an ArtifactInstance.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "artifact_instance")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtifactInstance extends Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_instance_id")
    private AppInstance appInstance;

    private Long appOnBoardingArtifactId;

    private String statusMessage;

    private String workloadInstanceId;

    private String operationId;
}
