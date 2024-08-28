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

package com.ericsson.oss.ae.acm.clients.keycloak.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Keycloak service accounts data object class.
 */
@Getter
@Setter
public class ServiceAccountDto {
    private String id;
    private String username;
}
