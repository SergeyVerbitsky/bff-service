package com.verbitsky.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

//todo rename class
public class AuthException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = -246425056301051639L;
    @Getter
    private final HttpStatus httpStatus;

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }

    public AuthException(String message) {
        super(message);
        httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
    }
}
