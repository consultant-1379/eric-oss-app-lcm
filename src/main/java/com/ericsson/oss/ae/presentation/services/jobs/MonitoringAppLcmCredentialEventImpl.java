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

package com.ericsson.oss.ae.presentation.services.jobs;

import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.CredentialEvent;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.CredentialEventRepository;
import com.ericsson.oss.ae.utils.UrlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ericsson.oss.ae.constants.KeycloakConstants.KEYCLOAK_REALM_MASTER;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT_SCOPE;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonitoringAppLcmCredentialEventImpl implements MonitoringJob{

    @Autowired
    private CredentialEventRepository credentialEventRepository;

    @Autowired
    private KeycloakClient keycloakClient;

    @Autowired
    private UrlGenerator urlGenerator;

    @Autowired
    private AppInstanceRepository appInstancerepository;

    @Override
    @Scheduled(initialDelayString = "${monitoringJob.initialDelay.in.milliseconds}",
            fixedRateString = "${monitoringJob.fixedRate.in.milliseconds}")
    public void execute() {
        log.debug("Started job to clean pending deletion record on credential event table");
        List<CredentialEvent> credentialEventRepositoryList = credentialEventRepository.findAll();

        List<CredentialEvent> credentialEventListToDelete = credentialEventRepositoryList.stream()
            .filter(e -> "PENDING_DELETION".equals(e.getDeletionStatus())).collect(Collectors.toList());

        for (CredentialEvent ce : credentialEventListToDelete) {
            if (ce != null && ce.getClientId() != null && isAppTerminated(ce.getAppInstanceId())) {
                log.debug("Delete Keycloak resources for Client ID:{}", ce.getClientId());
                Arrays.stream(Objects.requireNonNull(Optional.ofNullable(keycloakClient.getClients())
                                                         .orElseThrow(() -> {
                                                             log.error("MonitoringAppLcmCredentialEventImpl message: {}, Credential Event Client ID:{}",FAILURE_TO_RETRIEVE_ID_FROM_CLIENT.getErrorMessage(), ce.getClientId());
                                                             return new ResourceNotFoundException(FAILURE_TO_RETRIEVE_ID_FROM_CLIENT,
                                                                                                  "Could not extract clients from keycloak'", urlGenerator.generateClientKeycloakUrl(KEYCLOAK_REALM_MASTER));
                                                         }).getBody()))
                    .filter(c -> c.getClientId().equals(ce.getClientId()))
                    .findAny()
                    .ifPresent(clientDto -> keycloakClient.deleteClient(clientDto.getId()));
                log.info("Delete CredentialEvent Id:{} for App Id:{}", ce.getClientId(), ce.getAppOnBoardingAppId());
                credentialEventRepository.delete(ce);
            }
        }
        log.debug("Finished job to clean pending deletion record on credential event table");
    }

    private boolean isAppTerminated(Long appInstanceId) {
        Optional<AppInstance> appInstanceOpt = appInstancerepository.findById(appInstanceId);
        if (appInstanceOpt.isEmpty()) {
            return true;
        }
        log.debug("Check if app instance id: {} is terminated", appInstanceId);
        List<AppInstance> aux = appInstancerepository.findAll();
        return aux.stream()
                .filter(appInstance -> Objects.equals(appInstanceId, appInstance.getId()))
                .anyMatch(appInstance -> appInstance.getHealthStatus().equals(HealthStatus.TERMINATED));
    }
}
