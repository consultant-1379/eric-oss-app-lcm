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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Keycloak client scope data object class.
 */
@Getter
@Setter
public class ClientScopeDto {

    @JsonProperty("attributes")
    private AttributeScopeDto attributes;

    @JsonProperty("name")
    private String name;

    @JsonProperty("protocol")
    private String protocol;

}
