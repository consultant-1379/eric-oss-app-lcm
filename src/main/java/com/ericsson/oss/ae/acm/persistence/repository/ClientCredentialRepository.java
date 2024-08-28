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

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;

/**
 * Interface for Credential Repository.
 */

@Repository
public interface ClientCredentialRepository extends JpaRepository<ClientCredential, Long> {

    ClientCredential findByAppInstanceId(final UUID appInstanceId);

}
