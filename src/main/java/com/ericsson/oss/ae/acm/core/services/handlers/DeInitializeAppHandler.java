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
import com.ericsson.oss.ae.acm.clients.acmr.dto.StateChangeResult;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.common.exception.RestRequestFailedException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.presentation.mapper.AppDetailsMapper;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@Slf4j
@Service
public class DeInitializeAppHandler {
    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppDetailsMapper appDetailsMapper;

    @Autowired
    private AcmService acmService;

    public AppDetails deInitializeApp(final App app) {

        final AutomationCompositionDefinition automationCompositionDefinition = acmService.getAutomationCompositionType(app.getCompositionId());
        final AcTypeState compositionState = automationCompositionDefinition.getState();
        final StateChangeResult currentStateChangeResult = automationCompositionDefinition.getStateChangeResult();
        if (compositionState.equals(AcTypeState.COMMISSIONED) && currentStateChangeResult.equals(StateChangeResult.NO_ERROR)) {
            log.warn("Automation Composition type is in state: COMISSIONED with State change result: NO_ERROR for Composition with id: {}. Setting App state to CREATED", automationCompositionDefinition.getCompositionId());
            app.setStatus(AppStatus.CREATED);
        } else {
            try {
                log.info("Automation Composition type is in state: {} in ACM-R with State change result: {} for Composition with id: {}. Sending DE-PRIME request to ACM-R and setting App state to DEINITIALIZING.", compositionState, currentStateChangeResult, automationCompositionDefinition.getCompositionId());
                acmService.dePrimeAutomationCompositionType(app.getCompositionId());
                app.setStatus(AppStatus.DEINITIALIZING);
            } catch (RestRequestFailedException ex){
                log.error("De-Prime Request failed in ACM-R for composition-id {}. Details: ACM-R response status: {}, Detail: {}", automationCompositionDefinition.getCompositionId(), ex.getHttpStatus(), ex.getErrorDetails(), ex);
                throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DEINITIALIZE_APP_DEPRIME_COMPOSITION_TYPE_ERROR, new String[]{ex.getErrorDetails()});
            }
        }
        appRepository.save(app);
        return appDetailsMapper.fromApp(app);
    }
}
