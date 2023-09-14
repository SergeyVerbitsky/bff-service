package com.verbitsky.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import lombok.Getter;

import java.io.Serial;

public class AuthException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = -246425056301051639L;
    @Getter
    private final HttpStatus httpStatus;

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }

    public AuthException(String message, Exception exception, HttpStatus status) {
        super(message, exception);
        this.httpStatus = status;
    }
}
