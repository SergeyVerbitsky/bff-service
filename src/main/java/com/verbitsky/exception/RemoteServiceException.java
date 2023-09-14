package com.verbitsky.exception;

import org.springframework.http.HttpStatusCode;

import java.io.Serial;

public class RemoteServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8533892594771258521L;
    private final HttpStatusCode statusCode;

    public RemoteServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getHttpStatus() {
        return statusCode;
    }
}
