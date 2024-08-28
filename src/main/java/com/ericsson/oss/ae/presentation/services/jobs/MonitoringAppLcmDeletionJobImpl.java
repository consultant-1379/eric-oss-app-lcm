/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClient;
import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.services.AppInstanceJpaSpecification;
import com.ericsson.oss.ae.presentation.services.appinstance.AppInstanceService;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_DELETE_ERROR;

/**
 * The type Monitoring app lcm deletion job.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MonitoringAppLcmDeletionJobImpl implements MonitoringJob {

    @Autowired
    private AppInstanceService appInstanceService;
    @Autowired
    private AppInstanceRepository appInstancerepository;
    @Autowired
    private AppOnboardingClient appOnboardingClient;
    /**
     * Method used to run the Monitoring job which will delete instances {@link AppInstance} in valid for delete health status {@link HealthStatus}
     */
    @Override
    @Scheduled(initialDelayString = "${deletionJob.initialDelay.in.milliseconds}",
        fixedRateString = "${deletionJob.fixedRate.in.milliseconds}")
    public void execute() {
        log.debug("App Lcm Monitoring Deletion job started");
        List<AppInstance> deletingInstances = getDeletingInstances();
        if(!deletingInstances.isEmpty()){
            log.debug("{} instances found to delete", deletingInstances.size());
            List<Long> appIdList = getAppIdList(deletingInstances);
            log.debug("{} apps intended to delete", appIdList.size());
            for(Long appId : appIdList){
                //filter out and create a list of instances for the same app id fetched from all DB instances
                List<AppInstance>appInstanceListForAppId = deletingInstances.stream()
                    .filter(instance -> appId.equals(instance.getAppOnBoardingAppId()))
                    .collect(Collectors.toList());
                //Delete app instances/artifacts and workloads from Helm and App-Lcm DB for app id
                log.debug("Delete {} app instances and their artifacts for App Id:{}", appInstanceListForAppId.size(), appId);
                if(appInstanceService.deleteAppInstancesResources(appId, appInstanceListForAppId)){
                    log.info("Delete App Id {} and its instances and artifacts from app-lcm has succeeded", appId);
                    //Delete app package - call on-boarding to clean their resources.
                    deleteOnBoardingPackage(appId);
                }else{
                    log.error("Delete App Id {} and its instances and artifacts from app-lcm has failed", appId);
                }
            }
        }
        log.debug("App Lcm Monitoring Deletion job finished");
    }

    private void deleteOnBoardingPackage(Long appId) {
        log.debug("Delete app onBoarding package");
        try{
            final ResponseEntity<Object> deletePackageResponse = appOnboardingClient.deletePackage(appId);
            //Just log if the onBoarding resources has been deleted
            log.info("Delete of app onBoarding package with app id {} returned {} http status code", appId, deletePackageResponse.getStatusCode());
        }catch (final ResourceAccessException exception){
            log.error(AppLcmError.APP_ON_BOARDING_SERVICE_UNAVAILABLE.getErrorMessage(), exception);
        }catch (final RestClientException exception) {
            log.error(APP_ON_BOARDING_DELETE_ERROR.getErrorMessage(), exception);
        }
    }

    /**
     * Get separate list with only app id's
     * @param deletingInstances
     * @return
     */
    private List<Long> getAppIdList(List<AppInstance> deletingInstances) {
        //sort list in app id order
        log.debug("Create list of Apps from list of instances");
        Comparator<AppInstance> comparatorById = Comparator.comparing(AppInstance::getAppOnBoardingAppId);
        Collections.sort(deletingInstances, comparatorById);
        List<Long>appIdList = new ArrayList<>();
        Long lastAppId = null;
        for(AppInstance instance : deletingInstances){
            if(!instance.getAppOnBoardingAppId().equals(lastAppId)){
                appIdList.add(instance.getAppOnBoardingAppId());
            }
            lastAppId = instance.getAppOnBoardingAppId();
        }
        return appIdList;
    }

    /**
     * Gets deleting instances.
     *
     * @return the deleting instances
     */
    protected List<AppInstance> getDeletingInstances() {
        log.debug("Get instances intended to delete from DB");
        AppInstanceJpaSpecification specification = new AppInstanceJpaSpecification();
        AppInstanceFilter filter = new AppInstanceFilter();
        filter.setHealthStatus(Arrays.asList(HealthStatus.DELETING, HealthStatus.FAILED));
        filter.setTargetStatus(Arrays.asList(TargetStatus.APP_DELETED));
        return appInstancerepository.findAll(specification.getAppInstanceRequest(filter));
    }
}
