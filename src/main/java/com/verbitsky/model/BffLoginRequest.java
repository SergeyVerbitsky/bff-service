package com.verbitsky.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BffLoginRequest(
        @NotEmpty(message = "Field 'userName' is required and can't be empty.")
        String userName,
        @NotEmpty(message = "Field 'password' is required and can't be empty.")
        String password) {
}
