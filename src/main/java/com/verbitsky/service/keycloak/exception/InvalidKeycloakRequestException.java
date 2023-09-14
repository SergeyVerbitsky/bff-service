package com.verbitsky.service.keycloak.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.Setter;

import com.verbitsky.service.keycloak.client.KeycloakAction;

import java.io.Serial;

@Getter
@Setter
public class InvalidKeycloakRequestException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7175021289840209830L;
    private final HttpStatusCode errorCode;
    private final KeycloakAction action;
    private final String errorMessage;

    public InvalidKeycloakRequestException(HttpStatusCode code, KeycloakAction action, String errorMessage) {
        super(errorMessage);
        this.errorCode = code;
        this.errorMessage = errorMessage;
        this.action = action;
    }

    public InvalidKeycloakRequestException() {
        super();
        errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
        action = KeycloakAction.UNKNOWN;
        errorMessage = "unknown error";
    }

    public InvalidKeycloakRequestException(String message) {
        super(message);
        errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
        action = KeycloakAction.UNKNOWN;
        errorMessage = message;
    }
}
