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

package com.ericsson.oss.ae.presentation.mappers;

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.utils.UrlGenerator;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ericsson.oss.ae.utils.mapper.MapperUtils.addAppInstanceDtoLinks;

/**
 * Maps AppInstance to its Dto class.
 */
@Component
public class AppInstanceMapper extends ConfigurableMapper {

    @Autowired
    private UrlGenerator urlGenerator;

    @Override
    protected void configure(final MapperFactory factory) {
        factory.classMap(AppInstanceDto.class, AppInstance.class).mapNulls(false).mapNullsInReverse(false).byDefault()
                .customize(new CustomMapper<>() {
                    @Override
                    public void mapBtoA(final AppInstance appInstance, final AppInstanceDto appInstanceDto, final MappingContext context) {
                        super.mapBtoA(appInstance, appInstanceDto, context);
                        addAppInstanceDtoLinks(appInstanceDto, appInstance, urlGenerator);
                    }
                }).register();
    }
}