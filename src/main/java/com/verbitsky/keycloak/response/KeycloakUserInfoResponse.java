package com.verbitsky.keycloak.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KeycloakUserInfoResponse extends KeycloakAbstractResponse {
    @JsonProperty("sub")
    private String userId;
    @JsonProperty("preferred_username")
    private String login;
    @JsonProperty("given_name")
    private String firstName;
    @JsonProperty("family_name")
    private String lastName;
    private String email;
    @JsonProperty("email_verified")
    private boolean emailVerified;
}
