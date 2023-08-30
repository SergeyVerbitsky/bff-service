package com.verbitsky.exception;

import java.io.Serial;

public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -9032471152769596811L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
