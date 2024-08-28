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

package com.ericsson.oss.ae.presentation.services.artifactinstance;

import com.ericsson.oss.ae.presentation.enums.Version;
import org.springframework.core.io.ByteArrayResource;

import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;

/**
 * Interface for Artifact Instance Service.
 */
public interface ArtifactInstanceService {

    ArtifactInstanceDto getArtifactInstance(Long appInstanceId, Long artifactInstanceId);

    ArtifactInstanceDto getArtifactInstance(Long appInstanceId, Long artifactInstanceId, Version version);

    ArtifactInstancesDto getAllArtifactInstances(Long appInstanceId);

    ArtifactInstancesDto getAllArtifactInstances(Long appInstanceId, Version version);

    void saveArtifactInstance(ArtifactInstance artifactInstance);

    void terminateArtifactInstance(ArtifactInstance artifactInstance);

    void deleteArtifactInstance(ArtifactInstance artifactInstance);

    void updateArtifactInstance(ArtifactInstance artifactInstance, ByteArrayResource artifactHelmFile,
            AppInstancePutRequestDto appInstancePutRequestDto);

}
