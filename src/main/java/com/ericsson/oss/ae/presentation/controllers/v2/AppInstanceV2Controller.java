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

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.presentation.enums.Version;
import com.ericsson.oss.ae.presentation.mappers.AppInstanceV1toV2Mapper;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.v2.api.AppInstancesV2Api;
import com.ericsson.oss.ae.v2.api.model.AppInstanceV2Dto;
import com.ericsson.oss.ae.v2.api.model.AppInstancesV2Dto;

@Slf4j
@RestController
@RequestMapping("/app-lcm/v2")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontEnd.corsValue:corsValue}")
public class AppInstanceV2Controller implements AppInstancesV2Api {

    @Autowired
    private final AppInstanceService appInstanceService;

    @Autowired
    private final AppInstanceV1toV2Mapper appInstanceV1toV2Mapper;

    private static final Logger logger = LoggerFactory.getLogger(AppInstanceV2Controller.class);

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstancesV2Dto> getAllAppInstances(final Long id) {
        logger.debug("Get V2 all Application Instances called");
        final AppInstancesDto allAppInstancesOrigDto = appInstanceService.getAllAppInstances(id, Version.V2);
        logger.debug("All V2 Application Instances retrieved");
        return new ResponseEntity<>(appInstanceV1toV2Mapper.mapV1ToV2(allAppInstancesOrigDto), HttpStatus.OK);
    }

    /**
     * Invokes a GET request to {@link AppInstanceService}.
     *
     * @param appInstanceId
     *            Long Object containing the ID of the specified AppInstance
     * @return Returns a ResponseEntity containing the Http Status & appInstance Retrieved from {@link AppInstanceRepository}
     */

    /**
     * @deprecated This function is now deprecated.
     */
    @Deprecated
    @Override
    public ResponseEntity<AppInstanceV2Dto> getAppInstanceById(final @PathVariable Long appInstanceId) {
        logger.info("Received a Get request to get V2 Application Instance with id = {}", appInstanceId);
        final AppInstanceDto appInstanceOrigDto = appInstanceService.getAppInstance(appInstanceId, Version.V2);
        logger.info("Successfully retrieved V2 Application Instance");
        return new ResponseEntity<>(appInstanceV1toV2Mapper.mapV1ToV2(appInstanceOrigDto, appInstanceId), HttpStatus.OK);
    }

}
