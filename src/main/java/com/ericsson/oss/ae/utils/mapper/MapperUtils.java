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

package com.ericsson.oss.ae.utils.mapper;

import com.ericsson.oss.ae.api.model.AppInstanceDto;
import com.ericsson.oss.ae.api.model.ArtifactInstanceDto;
import com.ericsson.oss.ae.api.model.Link;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.Instance;
import com.ericsson.oss.ae.utils.UrlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ericsson.oss.ae.constants.AppLcmConstants.*;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_APPS_URL;
import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_ARTIFACTS;

/**
 * Mapper utility class for mapping.
 */
@Slf4j
public final class MapperUtils {

    // Apache Commons BeanMap object maps a given Java Bean's attributes to a Map. However, it also maps the Bean object itself as a property keyed by
    // name 'class'. We do not need the bean itself mapped as a property, so we ignore it if it matches this constant.
    private static final String EXCESSIVE_MAP_ENTRY_CLASS = "class";

    private MapperUtils() {
    }

    /**
     * Helper method to map given REST Request Model to a MultiValueMap.
     *
     * @param dtoObject
     *            The DTO object to be mapped.
     * @return The ValueMap needed to make a call.
     */
    public static MultiValueMap<String, Object> mapRequestModelToMultiValueMap(final Object dtoObject) {
        log.debug("MapperUtils Map Request Model To Multi Value Map");
        final MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();

        for (final Map.Entry<Object, Object> attribute : new BeanMap(dtoObject).entrySet()) {
            if (attribute.getKey().toString().equals(EXCESSIVE_MAP_ENTRY_CLASS) || Objects.isNull(attribute.getValue())) {
                continue;
            }
            requestMap.add(attribute.getKey().toString(), createEntityWithMediaType(attribute.getValue()));
        }
        return requestMap;
    }

    private static HttpEntity<?> createEntityWithMediaType(final Object dtoObjectAttribute) {
        log.trace("MapperUtils Create Entity With Media Type");
        final HttpHeaders httpHeaders = new HttpHeaders();
        if (checkForType(dtoObjectAttribute, ByteArrayResource.class)) {
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        } else {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(dtoObjectAttribute, httpHeaders);
    }

    private static boolean checkForType(final Object candidate, final Class<?> type) {
        log.trace("MapperUtils Check for type");
        return type.isInstance(candidate);
    }

    /**
     * Helper method to add a list of {@link Link} to {@link AppInstanceDto}.
     *
     * @param appInstanceDto
     *            The {@link AppInstanceDto} object to add the links to.
     * @param appInstance
     *            The {@link AppInstance} object used to retrieve from it some data to create the links.
     * @param urlGenerator
     *            The {@link UrlGenerator} object to retrieve the route path used in the links.
     */
    public static void addAppInstanceDtoLinks(final AppInstanceDto appInstanceDto, final AppInstance appInstance, final UrlGenerator urlGenerator) {
        log.debug("MapperUtils Add App Instance Dto Links");
        appInstanceDto.links(buildLinks(appInstance, urlGenerator));
    }

    /**
     * Helper method to add a list of {@link Link} to {@link ArtifactInstanceDto}.
     *
     * @param artifactInstanceDto
     *            The {@link ArtifactInstanceDto} object to add the links to.
     * @param artifactInstance
     *            The {@link ArtifactInstance} object used to retrieve from it some data to create the links.
     * @param urlGenerator
     *            The {@link UrlGenerator} object to retrieve the route path used in the links.
     */
    public static void addArtifactInstanceDtoLinks(final ArtifactInstanceDto artifactInstanceDto, final ArtifactInstance artifactInstance,
                                                   final UrlGenerator urlGenerator) {
        log.debug("MapperUtils Add Artifact Instance Dto Links");
        artifactInstanceDto.links(buildLinks(artifactInstance, urlGenerator));
    }

    private static List<Link> buildLinks(final Instance instance, final UrlGenerator urlGenerator) {
        log.debug("MapperUtils Build Links");
        final List<Link> links = new ArrayList<>();

        final String appManagerHost = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString();

        String appOnBoardingHref = appManagerHost + urlGenerator.getAppManagerAppOnboardingRoutePath() + APP_ONBOARDING_APPS_URL + SLASH;
        final String appLcmBaseHref = appManagerHost + urlGenerator.getAppManagerAppLcmRoutePath() + APP_INSTANCES_URL + SLASH;

        if (instance instanceof AppInstance) {
            appOnBoardingHref += ((AppInstance) instance).getAppOnBoardingAppId();
            final String appInstanceSelfHref = appLcmBaseHref + ((AppInstance) instance).getId();
            links.add(createLink(SELF, appInstanceSelfHref));
            links.add(createLink(ARTIFACT_INSTANCES, appInstanceSelfHref + SLASH + ARTIFACT_INSTANCES));
        } else {
            appOnBoardingHref += ((ArtifactInstance) instance).getAppInstance().getAppOnBoardingAppId();
            final String appLcmAppInstanceHref = appLcmBaseHref + ((ArtifactInstance) instance).getAppInstance().getId();
            links.add(createLink(APP_INSTANCE, appLcmAppInstanceHref));
            final String appLcmAppInstancesHref = appLcmAppInstanceHref + SLASH + ARTIFACT_INSTANCES;
            links.add(createLink(ARTIFACT_INSTANCES, appLcmAppInstancesHref));
            final String artifactInstanceSelfHref = appLcmAppInstancesHref + SLASH + ((ArtifactInstance) instance).getId();
            links.add(createLink(SELF, artifactInstanceSelfHref));
        }
        log.debug("MapperUtils.buildLinks appOnBoardingHref: {}, appLcmBaseHref: {}", appOnBoardingHref, appLcmBaseHref);
        links.add(createLink(APP, appOnBoardingHref));
        links.add(createLink(ARTIFACTS, appOnBoardingHref + SLASH + APP_ONBOARDING_ARTIFACTS));

        return links;
    }

    private static Link createLink(final String rel, final String href) {
        log.debug("MapperUtils Create Link with REL: {} and HREF: {}", rel, href);
        final Link link = new Link();
        link.setRel(rel);
        link.setHref(href);

        return link;
    }
}
