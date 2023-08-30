package com.verbitsky.exception;

import java.io.Serial;

public class RemoteServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8533892594771258521L;

    public RemoteServiceException(String message) {
        super(message);
    }

    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
