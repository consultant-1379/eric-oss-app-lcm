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

package com.ericsson.oss.ae.presentation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.api.ArtifactInstancesApi;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstancesDto;
import com.ericsson.oss.ae.presentation.services.artifactinstance.ArtifactInstanceService;

/**
 * Controller for ArtifactInstance.
 */
@Slf4j
@RestController
@RequestMapping("/app-lcm/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontEnd.corsValue:corsValue}")
public class ArtifactInstanceController implements ArtifactInstancesApi {

    private final ArtifactInstanceService artifactInstanceService;


    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<ArtifactInstanceDto> getArtifactInstanceByAppInstanceIdAndArtifactInstanceId(@PathVariable final Long appInstanceId,
                                                                                                       @PathVariable final Long artifactInstanceId) {
        log.info("Get Artifact Instance by Application Instance id {} and Artifact Instance id {} called", appInstanceId, artifactInstanceId);
        final ArtifactInstanceDto artifactInstance = artifactInstanceService.getArtifactInstance(appInstanceId, artifactInstanceId);
        log.info("Artifact Instance retrieved endpoint not implemented");
        return new ResponseEntity<>(artifactInstance, HttpStatus.OK);
    }

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<ArtifactInstancesDto> getArtifactInstancesByAppInstanceId(@PathVariable final Long appInstanceId) {
        log.info("Get All Artifact Instances by Application Instance id {} called", appInstanceId);
        final ArtifactInstancesDto artifactInstances = artifactInstanceService.getAllArtifactInstances(appInstanceId);
        log.info("All Artifact Instances retrieved");
        return new ResponseEntity<>(artifactInstances, HttpStatus.OK);
    }
}