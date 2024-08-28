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

package com.ericsson.oss.ae.acm.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

/**
 * Interface for App Instance Repository.
 */

@Repository
public interface AppInstancesRepository extends JpaRepository<AppInstances, UUID> {
    List<AppInstances> findAllByStatus(AppInstanceStatus status);
    List<AppInstances> findAllByAppId(UUID appId);
}
