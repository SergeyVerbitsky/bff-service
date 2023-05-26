package com.verbitsky.model;

public record ApiLoginRequest(
        String userName,
        String password) {
}
