package com.verbitsky.service.keycloak.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class InvalidKeycloakRequestException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7175021289840209830L;
    private final HttpStatusCode errorCode;
    private final String errorMessage;

    public InvalidKeycloakRequestException(HttpStatusCode code, String errorMessage) {
        super(errorMessage);
        this.errorCode = code;
        this.errorMessage = errorMessage;
    }
}
