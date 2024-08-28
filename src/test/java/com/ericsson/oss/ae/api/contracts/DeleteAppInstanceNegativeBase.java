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

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.context.WebApplicationContext;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest(classes = AppLcmApplication.class)
public class DeleteAppInstanceNegativeBase {

    @MockBean
    private AppInstanceRepository repository;

    @Autowired
    private WebApplicationContext applicationContext;

    private static final Long NOT_FOUND_ID = 2L;
    private static final Long NOT_INSTANTIATED_ID = 2L;

    @BeforeEach
    public void setup() {

        when(repository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());
        RestAssuredMockMvc.webAppContextSetup(applicationContext);

    }


}
