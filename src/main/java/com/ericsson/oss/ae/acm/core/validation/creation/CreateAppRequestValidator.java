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

package com.ericsson.oss.ae.acm.core.validation.creation;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_APP_COMPONENT_TYPE_UNSUPPORTED_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_LOCATION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_NAME_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_TYPE_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_COMPONENT_VERSION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_VERSION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_NAME_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_TYPE_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_VERSION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_NAME_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_PROVIDER_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_TYPE_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_VERSION_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_NULL_APP_COMPONENT_ERR_MESSAGE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.CREATE_APP_REQUEST_NULL_ARTIFACT_ERR_MESSAGE;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.Artifact;
import com.ericsson.oss.ae.v3.api.model.Component;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;

@Slf4j
@NoArgsConstructor
public class CreateAppRequestValidator {

    public void validate(CreateAppRequest createAppRequest) throws AppLcmException {
        validateApp(createAppRequest);
        //Validate App Components and Artifacts
        if (isAppComponentPopulated(createAppRequest)) {
            for (Component component : createAppRequest.getComponents()) {
                validateAppComponent(component);
                if (isArtifactPopulated(component)) {
                    for (Artifact artifact : component.getArtifacts()) {
                        validateArtifact(artifact);
                    }
                } else {
                    throw generateAndLogError(CREATE_APP_REQUEST_NULL_ARTIFACT_ERR_MESSAGE);
                }
            }
        } else {
            throw generateAndLogError(CREATE_APP_REQUEST_NULL_APP_COMPONENT_ERR_MESSAGE);
        }
    }

    private void validateApp(final CreateAppRequest createAppRequest) {
        if (Optional.ofNullable(createAppRequest.getName()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_NAME_ERR_MESSAGE);
        }

        if (Optional.ofNullable(createAppRequest.getVersion()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_VERSION_ERR_MESSAGE);
        } else if (!validateVersion(Optional.ofNullable(createAppRequest.getVersion()).orElse(StringUtils.EMPTY))) {
            throw generateAndLogError(CREATE_APP_REQUEST_INVALID_APP_VERSION_ERR_MESSAGE);
        }

        if (Optional.ofNullable(createAppRequest.getType()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_TYPE_ERR_MESSAGE);
        }

        if (Optional.ofNullable(createAppRequest.getProvider()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_PROVIDER_ERR_MESSAGE);
        }
    }

    private void validateAppComponent(final Component component) {
        if (Optional.ofNullable(component.getName()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_COMPONENT_NAME_ERR_MESSAGE);
        }

        if (Optional.ofNullable(component.getType()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_COMPONENT_TYPE_ERR_MESSAGE);
        } else if (!validateValidAppComponentType(Optional.ofNullable(component.getType()).orElse(StringUtils.EMPTY))) {
            throw generateAndLogError(CREATE_APP_REQUEST_APP_COMPONENT_TYPE_UNSUPPORTED_ERR_MESSAGE);
        }

        if (Optional.ofNullable(component.getVersion()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_MISSING_APP_COMPONENT_VERSION_ERR_MESSAGE);
        } else if (!validateVersion(Optional.ofNullable(component.getVersion()).orElse(StringUtils.EMPTY))) {
            throw generateAndLogError(CREATE_APP_REQUEST_INVALID_APP_COMPONENT_VERSION_ERR_MESSAGE);
        }
    }

    private void validateArtifact(Artifact artifact) {
        if (Optional.ofNullable(artifact.getName()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_ARTIFACT_MISSING_NAME_ERR_MESSAGE);
        }

        if (Optional.ofNullable(artifact.getType()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_ARTIFACT_MISSING_TYPE_ERR_MESSAGE);
        }

        if (Optional.ofNullable(artifact.getLocation()).orElse(StringUtils.EMPTY).isEmpty()) {
            throw generateAndLogError(CREATE_APP_REQUEST_ARTIFACT_MISSING_LOCATION_ERR_MESSAGE);
        } else if (!validateArtifactLocation(Optional.ofNullable(artifact.getLocation()).orElse(StringUtils.EMPTY))) {
            throw generateAndLogError(CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE);
        }
    }

    private AppLcmException generateAndLogError(String lcmConstantsMessage) {
        final AppLcmException appLcmException = new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED,
            new String[] { lcmConstantsMessage });
        log.error(appLcmException.generateErrorMessage());
        return appLcmException;
    }

    private boolean isAppComponentPopulated(CreateAppRequest createAppRequest) {
        return !createAppRequest.getComponents().isEmpty();
    }

    private boolean isArtifactPopulated(Component appComponent) {
        return !appComponent.getArtifacts().isEmpty();
    }

    private boolean validateValidAppComponentType(String type) {
        return Arrays.stream(ValidAppComponentType.values()).anyMatch(e -> e.name().equalsIgnoreCase(type));
    }

    private boolean validateVersion(String version) {
        Pattern versionPattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
        Matcher matcher = versionPattern.matcher(version);
        return matcher.matches();
    }

    private boolean validateArtifactLocation(String location) {
        if (!location.contains(String.valueOf(AppLcmConstants.SLASH))) {
            return false;
        }

        String[] keyValue = location.split(String.valueOf(AppLcmConstants.SLASH));
        return keyValue.length == 3 && !keyValue[0].isEmpty() && !keyValue[1].isEmpty() && !keyValue[2].isEmpty();
    }
}
