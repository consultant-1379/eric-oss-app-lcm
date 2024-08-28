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
import com.google.gson.annotations.SerializedName;

public class TokenDto {

    @SerializedName("access_token")
    @JsonProperty("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    @JsonProperty("expires_in")
    private String expiresIn;

    @SerializedName("refresh_expires_in")
    @JsonProperty("refresh_expires_in")
    private Integer refreshExpiresIn;

    @SerializedName("refresh_token")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @SerializedName("token_type")
    @JsonProperty("token_type")
    private String tokenType;

    @SerializedName("not-before-policy")
    @JsonProperty("not-before-policy")
    private Integer notBeforePolicy;

    @SerializedName("session_state")
    @JsonProperty("session_state")
    private String sessionState;

    @SerializedName("scope")
    @JsonProperty("scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
