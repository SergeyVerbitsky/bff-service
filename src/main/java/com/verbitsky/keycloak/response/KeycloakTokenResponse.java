package com.verbitsky.keycloak.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KeycloakTokenResponse extends KeycloakAbstractResponse {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private String refreshExpiresIn;
    private String tokenType;
}