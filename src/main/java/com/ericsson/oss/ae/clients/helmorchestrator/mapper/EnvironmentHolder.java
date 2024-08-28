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

package com.ericsson.oss.ae.clients.helmorchestrator.mapper;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class EnvironmentHolder {

    @Value("${NAMESPACE:}")
    private String namespaceEnv;

    @Value("${KEYCLOAK_ADMIN_USER:admin}")
    private String iamAdminUser;

    @Value("${KEYCLOAK_ADMIN_P:test}")
    private String iamAdminP;
}
