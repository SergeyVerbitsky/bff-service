package com.verbitsky.model;

public record BffRegisterRequest(
        String userName,
        String password,
        String email) {
}
