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

package com.ericsson.oss.ae.presentation.services;

import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * The type App instance jpa specification.
 */
@Component
public class AppInstanceJpaSpecification {

    /**
     * Get app instance request specification.
     *
     * @param request the request
     * @return the specification
     */
    public Specification<AppInstance> getAppInstanceRequest(AppInstanceFilter request){

        return (appInstance, criteriaQuery, criteriaBuilder) -> {
            List<Predicate>predicates = new ArrayList<>();
            if(!CollectionUtils.isEmpty(request.getInstanceIds())){
                predicates.add(criteriaBuilder.in(appInstance.get("id")).value(request.getInstanceIds()));
            }
            if(!CollectionUtils.isEmpty(request.getAppOnBoardingAppId())){
                predicates.add(criteriaBuilder.in(appInstance.get("appOnBoardingAppId")).value(request.getAppOnBoardingAppId()));
            }
            if(!CollectionUtils.isEmpty(request.getHealthStatus())){
                predicates.add(criteriaBuilder.in(appInstance.get("healthStatus")).value(request.getHealthStatus()));
            }
            if(!CollectionUtils.isEmpty(request.getTargetStatus())){
                predicates.add(criteriaBuilder.in(appInstance.get("targetStatus")).value(request.getTargetStatus()));
            }
            if(!CollectionUtils.isEmpty(request.getCreateTimeStamp())){
                predicates.add(criteriaBuilder.in(appInstance.get("createdTimestamp")).value(request.getCreateTimeStamp()));
            }
            if(!CollectionUtils.isEmpty(request.getUpdateTimeStamp())){
                predicates.add(criteriaBuilder.in(appInstance.get("updatedTimestamp")).value(request.getUpdateTimeStamp()));
            }
            if(!CollectionUtils.isEmpty(request.getAdditionalParameters())){
                predicates.add(criteriaBuilder.in(appInstance.get("additionalParameters")).value(request.getAdditionalParameters()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
