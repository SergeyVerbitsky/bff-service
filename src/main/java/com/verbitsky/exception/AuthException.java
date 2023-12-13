package com.verbitsky.exception;

import org.springframework.http.HttpStatus;

import com.verbitsky.api.exception.ServiceException;

import java.io.Serial;

public class AuthException extends ServiceException {
    @Serial
    private static final long serialVersionUID = -246425056301051639L;


    public AuthException(HttpStatus httpStatus, String errorMessage, String cause) {
        super(errorMessage, cause, httpStatus);

    }

    public AuthException(HttpStatus httpStatus, String errorMessage) {
        super(httpStatus, errorMessage);
    }
}
