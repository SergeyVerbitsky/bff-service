package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KeycloakUserInfoResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 2565488421343003119L;
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
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean enabled;
}
