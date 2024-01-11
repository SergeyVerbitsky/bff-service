package com.verbitsky.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LogoutRequest(
        @NotEmpty(message = "Request parameter1 is required and can't be empty ")
        @NotNull(message = "Request parameter1 can't be empty")
        String userId,
        @NotEmpty(message = "Request parameter2 is required and can't be empty")
        @NotNull(message = "Request parameter2 can't be empty")
        String sessionId,
        @NotEmpty(message = "Request parameter3 is required and can't be empty")
        @NotNull(message = "Request parameter3 can't be empty")
        String deviceId) {
}
