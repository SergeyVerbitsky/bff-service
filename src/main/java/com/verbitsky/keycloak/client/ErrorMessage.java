package com.verbitsky.keycloak.client;

import org.springframework.http.HttpStatusCode;

public record ErrorMessage (
        HttpStatusCode statusCode,
        String message) {
}
