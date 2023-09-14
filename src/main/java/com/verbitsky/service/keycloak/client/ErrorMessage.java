package com.verbitsky.service.keycloak.client;

import org.springframework.http.HttpStatusCode;

public record ErrorMessage(
        HttpStatusCode httpStatus,
        int errorCode,
        String message) {
}
