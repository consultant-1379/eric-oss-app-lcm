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

package com.ericsson.oss.ae.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ae.model.entity.ArtifactInstance;

/**
 * Interface for Artifact Instance Repository.
 */
@Repository
public interface ArtifactInstanceRepository extends JpaRepository<ArtifactInstance, Long> {

    Optional<ArtifactInstance> findByAppInstanceIdAndId(Long appInstanceId, Long artifactInstanceId);

    Optional<List<ArtifactInstance>> findAllArtifactInstancesByAppInstanceId(Long appInstanceId);

}
