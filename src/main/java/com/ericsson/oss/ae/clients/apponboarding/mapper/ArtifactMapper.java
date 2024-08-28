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

package com.ericsson.oss.ae.clients.apponboarding.mapper;

import org.springframework.stereotype.Component;

import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.ae.clients.apponboarding.model.Artifact;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;

/**
 * Mapper class used for mapping an Artifact Dto {@link ArtifactDto} to an Artifact {@link Artifact}.
 */
@Component
public class ArtifactMapper extends ConfigurableMapper {
    @Override
    protected void configure(final MapperFactory factory) {
        factory.classMap(ArtifactDto.class, Artifact.class).mapNulls(false).mapNullsInReverse(false).byDefault().register();
    }
}
