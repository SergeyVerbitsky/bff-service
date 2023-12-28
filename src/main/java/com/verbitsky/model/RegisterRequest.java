package com.verbitsky.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @NotEmpty(message = "Field 'userName' is required and can't be empty.")
        String userName,
        String firstName,
        String lastName,
        @Email(message = "Wrong email field value.")
        @NotEmpty(message = "Field 'email' is required and can't be empty.")
        String email,
        @NotEmpty(message = "Field 'password' is required and can't be empty.")
        String password) {
}