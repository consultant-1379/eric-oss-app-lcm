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

package com.ericsson.oss.ae.api.contracts;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.oss.ae.acm.presentation.controller.AppInstancesController;
import com.ericsson.oss.ae.acm.presentation.web.GlobalControllerExceptionHandler;
import com.ericsson.oss.ae.acm.core.services.AppInstancesService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

public abstract class AcmAppInstancesBase extends AcmV3Base {

    @InjectMocks
    public AppInstancesController controller;

    @Mock
    public AppInstancesService service;

    @InjectMocks
    public GlobalControllerExceptionHandler appLcmExceptionHandler;

    @BeforeEach
    public void setup() {
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(controller, service)
            .setControllerAdvice(appLcmExceptionHandler);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
        validate();
    }
}
