package com.verbitsky.model;

public record BffRegisterRequest(
        String userName,
        String firstName,
        String lastName,
        String email,
        String password) {
}