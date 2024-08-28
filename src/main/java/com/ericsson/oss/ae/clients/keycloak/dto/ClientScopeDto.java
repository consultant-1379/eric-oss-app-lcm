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


package com.ericsson.oss.ae.clients.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ClientScopeDto {

    @JsonProperty("attributes")
    private AttributeScopeDto attributes;

    @JsonProperty("name")
    private String name;

    @JsonProperty("protocol")
    private String protocol;

    public AttributeScopeDto getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeScopeDto attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }



}
