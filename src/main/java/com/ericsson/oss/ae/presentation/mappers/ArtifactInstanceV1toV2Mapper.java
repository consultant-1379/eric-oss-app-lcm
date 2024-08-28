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

package com.ericsson.oss.ae.presentation.mappers;

import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.utils.UrlGenerator;
import com.ericsson.oss.ae.v2.api.model.ArtifactInstanceV2Dto;
import com.ericsson.oss.ae.v2.api.model.ArtifactInstancesV2Dto;
import com.ericsson.oss.ae.v2.api.model.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ArtifactInstanceV1toV2Mapper {

    @Autowired
    private UrlGenerator urlGenerator;

    public ArtifactInstanceV2Dto mapV1ToV2(ArtifactInstanceDto artifactInstanceDto, Long appInstanceId, Long artifactInstanceId) {

        ArtifactInstanceV2Dto artifactInstanceV2Dto = new ArtifactInstanceV2Dto();

        if (artifactInstanceDto != null) {
            artifactInstanceV2Dto = setArtifactInstanceData(artifactInstanceDto, artifactInstanceV2Dto);
            return artifactInstanceV2Dto;
        }

        artifactInstanceV2Dto.setAppLcmErrorCode(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND.getErrorCode());
        artifactInstanceV2Dto.setAppLcmErrorMessage(String.valueOf(AppLcmError.ARTIFACT_INSTANCE_NOT_FOUND.getErrorMessage()));
        artifactInstanceV2Dto.setDetail(String.format("Artifact Instance with artifactInstanceId = %s belonging to appInstanceId = %s was not found", artifactInstanceId,
                appInstanceId));
        artifactInstanceV2Dto.setUrl(urlGenerator.generateArtifactInstanceByAppIdAndArtifactIdV2Url(appInstanceId, artifactInstanceId));

        return artifactInstanceV2Dto;
    }

    public ArtifactInstancesV2Dto mapV1ToV2(ArtifactInstancesDto artifactInstancesDto) {

        ArtifactInstancesV2Dto artifactInstancesV2Dto = new ArtifactInstancesV2Dto();

        artifactInstancesV2Dto.setArtifactInstances(artifactInstancesDto.getArtifactInstances().stream().map(artifactInstanceDto -> {
            ArtifactInstanceV2Dto artifactInstanceV2Dto = new ArtifactInstanceV2Dto();
            artifactInstanceV2Dto = setArtifactInstanceData(artifactInstanceDto, artifactInstanceV2Dto);
            return artifactInstanceV2Dto;
        }).collect(Collectors.toList()));
        return artifactInstancesV2Dto;
    }

    private ArtifactInstanceV2Dto setArtifactInstanceData(ArtifactInstanceDto artifactInstanceDto, ArtifactInstanceV2Dto artifactInstanceV2Dto){

        artifactInstanceV2Dto.setArtifactInstanceId(artifactInstanceDto.getArtifactInstanceId());
        artifactInstanceV2Dto.setArtifactId(artifactInstanceDto.getArtifactId());
        artifactInstanceV2Dto.setHealthStatus(artifactInstanceDto.getHealthStatus());
        artifactInstanceV2Dto.setCreatedTimestamp(artifactInstanceDto.getCreatedTimestamp());
        artifactInstanceV2Dto.setStatusMessage(artifactInstanceDto.getStatusMessage());
        artifactInstanceV2Dto.setLinks(artifactInstanceDto.getLinks().stream().map((com.ericsson.oss.ae.api.model.Link link) -> {
            Link linkV2 = new Link();
            linkV2.setHref(link.getHref());
            linkV2.setRel(link.getRel());
            return linkV2;
        }).collect(Collectors.toList()));

        return artifactInstanceV2Dto;
    }

}
