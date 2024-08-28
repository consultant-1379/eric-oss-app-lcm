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

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.clients.helmorchestrator.model.Operation;
import com.ericsson.oss.management.lcm.api.model.OperationDto;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;

/**
 * Mapper class used for mapping instances of Operation {@link Operation} and OperationDto {@link OperationDto}.
 */
@Component
public class OperationDtoMapper extends ConfigurableMapper {
    @Override
    protected void configure(final MapperFactory factory) {
        final ConverterFactory converterFactory = factory.getConverterFactory();

        converterFactory.registerConverter(new CustomConverter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(final String s, final Type<? extends LocalDateTime> type, final MappingContext mappingContext) {
                return LocalDateTime.parse(s);
            }
        });
        factory.classMap(OperationDto.class, Operation.class).byDefault().register();
    }
}
