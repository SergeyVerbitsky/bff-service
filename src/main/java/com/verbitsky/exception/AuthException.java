package com.verbitsky.exception;

import org.springframework.http.HttpStatusCode;

import com.verbitsky.api.exception.ServiceException;

import java.io.Serial;

public class AuthException extends ServiceException {
    @Serial
    private static final long serialVersionUID = -246425056301051639L;

    public AuthException(HttpStatusCode httpStatusCode, String errorMessage) {
        super(httpStatusCode, errorMessage);
    }

    public AuthException(HttpStatusCode httpStatusCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause, httpStatusCode);
    }
}
