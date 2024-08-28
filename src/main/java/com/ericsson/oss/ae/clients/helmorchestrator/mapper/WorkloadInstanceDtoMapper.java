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

package com.ericsson.oss.ae.clients.helmorchestrator.mapper;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.api.model.AppInstancePutRequestDto;
import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePutRequestDto;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;

/**
 * Mapper class used for mapping multiple instances together such as
 * <ul>
 * <li>AppInstancePostRequestDto {@link AppInstancePostRequestDto} to WorkloadInstancePostRequestDto {@link WorkloadInstancePostRequestDto}.
 * <li>AppInstancePostRequestDto {@link AppInstancePostRequestDto} to WorkloadInstancePutRequestDto {@link WorkloadInstancePutRequestDto}.
 * <li>WorkloadInstanceDto {@link WorkloadInstanceDto} to WorkloadInstance {@link WorkloadInstance}.
 * <li>WorkloadInstancePostRequestDto {@link WorkloadInstancePostRequestDto} to WorkloadInstance {@link WorkloadInstance}.
 * <li>WorkloadInstancePutRequestDto {@link WorkloadInstancePutRequestDto} to WorkloadInstance {@link WorkloadInstance}.
 * <li>WorkloadInstance {@link WorkloadInstance} to WorkloadInstanceDto {@link WorkloadInstanceDto}.
 * </ul>
 * .
 */
@Log4j2
@Component
public class WorkloadInstanceDtoMapper extends ConfigurableMapper {

    @Autowired
    EnvironmentHolder environmentHolder;

    @Override
    protected void configure(final MapperFactory factory) {
        setDefaultSettings(factory, AppInstancePostRequestDto.class, WorkloadInstancePostRequestDto.class);
        setDefaultSettings(factory, AppInstancePostRequestDto.class, WorkloadInstancePutRequestDto.class);
        setAppInstancePostRequestDtoSettings(factory);
        setDefaultSettings(factory, AppInstancePutRequestDto.class, WorkloadInstancePutRequestDto.class);
        setDefaultSettings(factory, WorkloadInstanceDto.class, WorkloadInstance.class);
        setDefaultSettings(factory, WorkloadInstancePostRequestDto.class, WorkloadInstance.class);
        setDefaultSettings(factory, WorkloadInstancePutRequestDto.class, WorkloadInstance.class);
        setDefaultSettings(factory, WorkloadInstance.class, WorkloadInstanceDto.class);
    }

    private <A, B> void setDefaultSettings(final MapperFactory factory, final Class<A> source, final Class<B> target) {
        factory.classMap(source, target).mapNulls(false).mapNullsInReverse(false).byDefault().register();
    }

    private void setAppInstancePostRequestDtoSettings(final MapperFactory factory) {
        factory.classMap(AppInstancePostRequestDto.class, WorkloadInstance.class).mapNulls(false).mapNullsInReverse(false).byDefault()
                .customize(new CustomMapper<>() {
                    @Override
                    public void mapAtoB(final AppInstancePostRequestDto appInstancePostRequestDto, final WorkloadInstance workloadInstance,
                                        final MappingContext context) {
                       log.info("Namespace extracted: {}", environmentHolder.getNamespaceEnv());
                       workloadInstance.setNamespace(environmentHolder.getNamespaceEnv());
                    }
                }).byDefault().register();

        factory.classMap(AppInstancePutRequestDto.class, WorkloadInstance.class).mapNulls(false).mapNullsInReverse(false).byDefault()
                .customize(new CustomMapper<>() {
                    @Override
                    public void mapAtoB(final AppInstancePutRequestDto appInstancePutRequestDto, final WorkloadInstance workloadInstance,
                                        final MappingContext context) {
                        log.info("Namespace extracted: {}", environmentHolder.getNamespaceEnv());
                        workloadInstance.setNamespace(environmentHolder.getNamespaceEnv());
                    }
                }).byDefault().register();
    }
}