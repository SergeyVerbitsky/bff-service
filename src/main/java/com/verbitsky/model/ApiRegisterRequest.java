package com.verbitsky.model;

public record ApiRegisterRequest(
        String userName,
        String password,
        String email) {
}
