package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakLoginResponse extends KeycloakAbstractResponse {
    @Serial
    private static final long serialVersionUID = 6450381531922719321L;
    private String accessToken;
    private String refreshToken;
}