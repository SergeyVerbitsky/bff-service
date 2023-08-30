package com.verbitsky.service.keycloak.exception;

import com.verbitsky.service.keycloak.client.KeycloakAction;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

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
        this.errorCode = code;
        this.errorMessage = errorMessage;
        this.action = action;
    }
}
