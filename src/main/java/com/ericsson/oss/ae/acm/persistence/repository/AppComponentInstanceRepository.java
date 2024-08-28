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

import com.ericsson.oss.ae.acm.persistence.entity.AppComponentInstance;

/**
 * Interface for App Component Instance Repository.
 */
public interface AppComponentInstanceRepository extends JpaRepository<AppComponentInstance, UUID> {
    List<AppComponentInstance> findByAppInstanceId(final UUID appInstanceId);
}
