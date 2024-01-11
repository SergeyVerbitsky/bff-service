package com.verbitsky.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
        @NotEmpty(message = "Field 'login' is required and can't be empty.")
        String login,
        @NotEmpty(message = "Field 'password' is required and can't be empty.")
        String password,
        @NotEmpty(message = "Field 'deviceId' is required and can't be empty.")
        String deviceId) {
}
