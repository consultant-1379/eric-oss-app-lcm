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

package com.ericsson.oss.ae.acm.core.validation.statemachine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;

/**
 * Class to validate status and mode of App and App instances
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateTransitionValidator {

    @Autowired
    private final AppRepository appRepository;

    @Autowired
    private final AppInstancesRepository appInstancesRepository;

    /**
     * Validate App mode and status
     *
     * @param appId
     *         -  App Id to get app entity
     * @param useCase
     *         -  Use case
     */
    public void validateAppState(final UUID appId, final AppUseCase useCase) {
        log.debug("Validate App state of given Id: {} for use case {}", appId, useCase.name());
        final Optional<App> appDetailsEntity = appRepository.findById(appId);
        if (appDetailsEntity.isEmpty()) {
            log.error("App entity for given Id: {}, was not found in the database", appId);
            throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_NOT_FOUND_ERROR);
        }

        //validates the App mode
        final List<String> validModes = getValidModeStrings(useCase);
        validateMode(validModes, appDetailsEntity.get().getMode().name(), useCase.name());

        //validates the App status
        final List<String> validStatuses = getValidStatusStrings(useCase);
        validateStatus(validStatuses, appDetailsEntity.get().getStatus().name(), useCase.name());

    }

    /**
     * Validate app Instance status and app mode
     *
     * @param entityId
     *         -  App Id in case of Create. Otherwise, App Instance Id
     * @param useCase
     *         -  Use case
     */
    public void validateAppInstanceState(final UUID entityId, final AppInstanceUseCase useCase) {
        log.debug("Validate App instance state of given Id: {} for use case {}", entityId, useCase.name());
        if (useCase == AppInstanceUseCase.CREATE) {
            final Optional<App> appDetailsEntity = appRepository.findById(entityId);
            if (appDetailsEntity.isEmpty()) {
                log.error("App entity for given Id: {}, was not found in the database", entityId);
                throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_NOT_FOUND_ERROR);
            }

            final App app = appDetailsEntity.get();
            final List<String> validModes = getValidModeStrings(useCase);
            validateMode(validModes, app.getMode().name(), useCase.name());
        } else {
            final Optional<AppInstances> appInstanceDetailsEntity = appInstancesRepository.findById(entityId);
            if (!appInstanceDetailsEntity.isPresent()) {
                log.error("App Instance entity for given ID: {}, was not found in the DB", entityId);
                throw new AppLcmException(HttpStatus.NOT_FOUND, AppLcmError.APP_INSTANCE_ENTITY_NOT_FOUND);
            }

            final AppInstances appInstance = appInstanceDetailsEntity.get();
            final List<String> validStatuses = getValidStatusStrings(useCase);
            validateStatus(validStatuses, appInstance.getStatus().name(), useCase.name());
        }
    }

    private void validateMode(final List<String> validModes, final String mode, final String useCaseName) {
        if (!validModes.isEmpty() && !validModes.contains(mode)) {
            throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.LCM_APP_MODE_VALIDATION_ERROR,
                    new String[]{mode, useCaseName, validModes.stream().collect(Collectors.joining(" or "))});
        }
    }

    private void validateStatus(final List<String> validStatuses, final String status, final String useCaseName) {
        if (!validStatuses.isEmpty() && !validStatuses.contains(status)) {
            throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.LCM_STATUS_VALIDATION_ERROR,
                    new String[]{status, useCaseName, validStatuses.stream().collect(Collectors.joining(" or "))});
        }
    }

    private List<String> getValidModeStrings(final UseCase useCase) {
        final List<AppMode> validModes = UseCaseStateMapper.getValidModesByUseCase(useCase);
        return validModes.stream().map(Enum::name).collect(Collectors.toList());
    }

    private List<String> getValidStatusStrings(final UseCase useCase) {
        final List<EntityStatus> validStatuses = UseCaseStateMapper.getValidStatusesByUseCase(useCase);
        return validStatuses.stream().map(EntityStatus::toString).collect(Collectors.toList());
    }
}
