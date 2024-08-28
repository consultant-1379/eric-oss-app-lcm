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

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.v2.api.model.AppInstanceV2Dto;
import com.ericsson.oss.ae.v2.api.model.AppInstancesV2Dto;
import com.ericsson.oss.ae.v2.api.model.Link;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APP_INSTANCES_V2_URL;

@Component
public class AppInstanceV1toV2Mapper {

    public AppInstancesV2Dto mapV1ToV2(AppInstancesDto appInstancesDto) {
        AppInstancesV2Dto appInstancesV2Dto = new AppInstancesV2Dto();
        appInstancesV2Dto.setAppInstances(appInstancesDto.getAppInstances().stream().map(appInstanceDto -> {
            AppInstanceV2Dto appInstanceV2Dto = new AppInstanceV2Dto();
            appInstanceV2Dto = setAppInstanceData(appInstanceDto, appInstanceV2Dto);
            return appInstanceV2Dto;
        }).collect(Collectors.toList()));
        return appInstancesV2Dto;
    }

    public AppInstanceV2Dto mapV1ToV2(AppInstanceDto appInstanceDto, Long appInstanceId) {

        AppInstanceV2Dto appInstanceV2Dto = new AppInstanceV2Dto();

        if (appInstanceDto != null) {
            appInstanceV2Dto = setAppInstanceData(appInstanceDto, appInstanceV2Dto);
            return appInstanceV2Dto;
        }

        appInstanceV2Dto.setAppLcmErrorCode(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorCode());
        appInstanceV2Dto.setAppLcmErrorMessage(String.valueOf(AppLcmError.SPECIFIED_APP_INSTANCE_NOT_FOUND.getErrorMessage()));
        appInstanceV2Dto.setDetail(String.format("App Instance by appInstanceId = " + "%s was not found", appInstanceId));
        appInstanceV2Dto.setUrl(APP_INSTANCES_V2_URL);

        return appInstanceV2Dto;
    }

    private AppInstanceV2Dto setAppInstanceData(AppInstanceDto appInstanceDto, AppInstanceV2Dto appInstanceV2Dto){

        appInstanceV2Dto.setAppOnBoardingAppId(appInstanceDto.getAppOnBoardingAppId());
        appInstanceV2Dto.setAdditionalParameters(appInstanceDto.getAdditionalParameters());
        appInstanceV2Dto.setCreatedTimestamp(appInstanceDto.getCreatedTimestamp());
        appInstanceV2Dto.setHealthStatus(appInstanceDto.getHealthStatus());
        appInstanceV2Dto.setId(appInstanceDto.getId());
        appInstanceV2Dto.setLinks(appInstanceDto.getLinks().stream().map((com.ericsson.oss.ae.api.model.Link link) -> {
            Link linkV2 = new Link();
            linkV2.setHref(link.getHref());
            linkV2.setRel(link.getRel());
            return linkV2;
        }).collect(Collectors.toList()));
        appInstanceV2Dto.setTargetStatus(appInstanceDto.getTargetStatus());

        return appInstanceV2Dto;
    }

}
