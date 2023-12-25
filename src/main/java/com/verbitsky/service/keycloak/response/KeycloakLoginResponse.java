package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KeycloakLoginResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 6450381531922719321L;
    private String accessToken;
    private String refreshToken;
}