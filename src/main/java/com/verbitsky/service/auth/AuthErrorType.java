package com.verbitsky.service.auth;

enum AuthErrorType {
    TOKEN_REFRESH("Refresh token processing error: %s"),
    LOGIN("User login processing error: %s"),
    LOGOUT("User logout processing error: %s"),
    REGISTRATION("User registration processing error: %s"),
    USER_AUTHENTICATION("User authentication error: %s");

    private final String errorMessage;

    AuthErrorType(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
