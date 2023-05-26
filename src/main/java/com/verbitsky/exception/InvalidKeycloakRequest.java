package com.verbitsky.exception;

import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpStatusCode;

import java.io.Serial;

import com.verbitsky.keycloak.client.KeycloakAction;

@Getter
@Setter
public class InvalidKeycloakRequest extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7175021289840209830L;
    private final HttpStatusCode errorCode;
    private final KeycloakAction action;
    private final String errorMessage;

    public InvalidKeycloakRequest(HttpStatusCode code, KeycloakAction action, String errorMessage) {
        this.errorCode = code;
        this.errorMessage = errorMessage;
        this.action = action;
    }
}
