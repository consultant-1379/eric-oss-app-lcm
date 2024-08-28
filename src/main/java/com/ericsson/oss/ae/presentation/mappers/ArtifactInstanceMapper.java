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

import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.utils.UrlGenerator;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ericsson.oss.ae.utils.mapper.MapperUtils.addArtifactInstanceDtoLinks;


/**
 * Maps ArtifactInstance to its Dto class.
 */
@Component
public class ArtifactInstanceMapper extends ConfigurableMapper {

    @Autowired
    private UrlGenerator urlGenerator;

    @Override
    protected void configure(final MapperFactory factory) {
        factory.classMap(ArtifactInstanceDto.class, ArtifactInstance.class).field("artifactInstanceId", "id")
                .field("artifactId", "appOnBoardingArtifactId").mapNulls(false).mapNullsInReverse(false).byDefault().customize(new CustomMapper<>() {
                    @Override
                    public void mapBtoA(final ArtifactInstance artifactInstance, final ArtifactInstanceDto artifactInstanceDto,
                                        final MappingContext context) {
                        super.mapBtoA(artifactInstance, artifactInstanceDto, context);
                        addArtifactInstanceDtoLinks(artifactInstanceDto, artifactInstance, urlGenerator);
                    }
                }).register();
    }
}