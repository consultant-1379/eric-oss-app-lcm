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

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import com.ericsson.oss.ae.presentation.controllers.AppInstanceController;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteAppInstancePositiveBase {

    @InjectMocks
    private AppInstanceController controller;

    @Mock
    private AppInstanceService service;

    @Mock
    private AppInstanceRepository repository;

    @BeforeEach
    public void setup() {
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(controller, service, repository);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

}
