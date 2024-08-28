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

package com.ericsson.oss.ae.acm.core.services.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcTypeState;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AutomationCompositionDefinition;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.presentation.mapper.AppDetailsMapper;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Handler class that contains the logic to execute the 'Initialize App' use case.
 */
@Slf4j
@Service
public class InitializeAppHandler {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppDetailsMapper appDetailsMapper;

    @Autowired
    private AcmService acmService;

    public void initializeApp(final App app) {

        AutomationCompositionDefinition automationCompositionDefinition = null;

        // Check if App is already PRIMED in acm
        try {
            automationCompositionDefinition = acmService.getAutomationCompositionType(app.getCompositionId());
        } catch (final RestRequestFailedException ex) {
            final String errorMessage = ex.getErrorDetails();
            log.error("Request failure when calling ACM-R to Get the AC Type with appId {}. Reason: {}", app.getId(), errorMessage, ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.GET_AUTOMATION_COMPOSITION_TYPE_ERROR);

        }
        final AcTypeState compositionState = automationCompositionDefinition.getState();

        if (compositionState.equals(AcTypeState.COMMISSIONED)) {
            try {
                log.info("Composition type is in state COMMISSIONED in ACM-R. Setting App state to INITIALIZING and sending PRIME request to ACM-R.");
                // Send PRIME Request to ACM
                acmService.primeAutomationCompositionType(app.getCompositionId());
                app.setStatus(AppStatus.INITIALIZING);
                appRepository.save(app);
            } catch (final RestRequestFailedException ex) {
                log.error("PRIME Request failed in ACM for app-id {}. Details: ACM response status {}, Detail: {}", app.getId(), ex.getHttpStatus(),
                    ex.getErrorDetails(), ex);
                throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.INITIALIZE_APP_PRIME_COMPOSITION_TYPE_ERROR);
            }
        } else if (compositionState.equals(AcTypeState.PRIMING) || compositionState.equals(AcTypeState.PRIMED)) {
            /**
             * To support INITIALIZE_ERROR retriggers
             */
            log.info(
                "Composition type is in state PRIMING in ACM-R. Setting App state to INITIALIZING. Timeout was triggered in the previous prime.");
            //Do not send PRIME request to a PRIMING app.
            app.setStatus(AppStatus.INITIALIZING);
            appRepository.save(app);
        } else {
            // Invalid state, e.g. DEPRIMING
            log.error("Composition type not in valid state to Prime");
            throw new AppLcmException(HttpStatus.BAD_REQUEST, AppLcmError.INITIALIZE_APP_COMPOSITION_TYPE_INVALID_STATE_ERROR);
        }

    }

}
