package com.verbitsky.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BffLogoutRequest(
        @NotEmpty(message = "Field 'userId' is required and can't be empty.")
        String userId,
        @NotEmpty(message = "Field 'sessionId' is required and can't be empty.")
        String sessionId) {
}
