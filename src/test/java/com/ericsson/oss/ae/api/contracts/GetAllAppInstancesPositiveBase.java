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

package com.ericsson.oss.ae.api.contracts;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.AppInstancesDto;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.controllers.AppInstanceController;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@ExtendWith(MockitoExtension.class)
public class GetAllAppInstancesPositiveBase {

    @InjectMocks
    private AppInstanceController controller;

    @Mock
    private AppInstanceService service;

    @Mock
    private AppInstanceRepository repository;

    @BeforeEach
    public void setup() {
        given(service.getAllAppInstances(null)).willReturn(getAppInstances());
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(controller, service, repository);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

    private AppInstancesDto getAppInstances() {
        AppInstanceDto result = new AppInstanceDto();
        result.setAppOnBoardingAppId(234L);
        result.setId(1L);
        result.setHealthStatus(HealthStatus.FAILED.name());
        result.setTargetStatus(TargetStatus.INSTANTIATED.name());
        result.setCreatedTimestamp("2021-08-19 19:10:25-07");

        AppInstanceDto resultTwo = new AppInstanceDto();
        resultTwo.setAppOnBoardingAppId(235L);
        resultTwo.setId(2L);
        resultTwo.setHealthStatus(HealthStatus.PENDING.name());
        resultTwo.setTargetStatus(TargetStatus.TERMINATED.name());
        resultTwo.setCreatedTimestamp("2021-08-19 19:10:25-07");

        final AppInstancesDto appInstancesDto = new AppInstancesDto();
        appInstancesDto.addAppInstancesItem(result);
        appInstancesDto.addAppInstancesItem(resultTwo);

        return appInstancesDto;
    }
}
