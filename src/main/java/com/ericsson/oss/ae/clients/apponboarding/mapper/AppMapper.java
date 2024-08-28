/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.clients.apponboarding.mapper;

import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.model.App;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

/**
 * Mapper class used for mapping an App Dto {@link AppDto} to an App {@link com.ericsson.oss.ae.clients.apponboarding.model.App}.
 */
@Component
public class AppMapper extends ConfigurableMapper {
    @Override
    protected void configure(final MapperFactory factory) {
        factory.classMap(AppDto.class, App.class).mapNulls(false).mapNullsInReverse(false).byDefault().register();
    }
}
