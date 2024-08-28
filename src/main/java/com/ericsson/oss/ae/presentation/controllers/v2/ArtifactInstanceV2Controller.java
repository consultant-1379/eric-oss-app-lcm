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

package com.ericsson.oss.ae.presentation.controllers.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.mappers.ArtifactInstanceV1toV2Mapper;
import com.ericsson.oss.ae.presentation.services.artifactinstance.ArtifactInstanceService;
import com.ericsson.oss.ae.v2.api.ArtifactInstancesV2Api;
import com.ericsson.oss.ae.v2.api.model.ArtifactInstanceV2Dto;
import com.ericsson.oss.ae.v2.api.model.ArtifactInstancesV2Dto;

@Slf4j
@RestController
@RequestMapping("/app-lcm/v2")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontEnd.corsValue:corsValue}")
public class ArtifactInstanceV2Controller implements ArtifactInstancesV2Api {

    @Autowired
    private final ArtifactInstanceService artifactInstanceService;

    @Autowired
    private final ArtifactInstanceV1toV2Mapper artifactInstanceV1toV2Mapper;

    private static final Logger logger = LoggerFactory.getLogger(ArtifactInstanceV2Controller.class);

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<ArtifactInstanceV2Dto> getArtifactInstanceByAppInstanceIdAndArtifactInstanceId(@PathVariable final Long appInstanceId,
                                                                                                         @PathVariable final Long artifactInstanceId) {
        logger.info("Get V2 Artifact Instance by Application Instance id {} and Artifact Instance id {} called", appInstanceId, artifactInstanceId);
        final ArtifactInstanceDto artifactInstanceOrigDto = artifactInstanceService.getArtifactInstance(appInstanceId, artifactInstanceId, Version.V2);
        logger.info("V2 Artifact Instance retrieved endpoint not implemented");
        return new ResponseEntity<>(artifactInstanceV1toV2Mapper.mapV1ToV2(artifactInstanceOrigDto, appInstanceId, artifactInstanceId), HttpStatus.OK);
    }

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<ArtifactInstancesV2Dto> getArtifactInstancesByAppInstanceId(@PathVariable final Long appInstanceId) {
        logger.info("Get V2 All Artifact Instances by Application Instance id {} called", appInstanceId);
        final ArtifactInstancesDto artifactInstancesOrigDto = artifactInstanceService.getAllArtifactInstances(appInstanceId, Version.V2);
        logger.info("All V2 Artifact Instances retrieved");
        return new ResponseEntity<>(artifactInstanceV1toV2Mapper.mapV1ToV2(artifactInstancesOrigDto), HttpStatus.OK);
    }

}
