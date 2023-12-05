package com.verbitsky.controller;

import org.springframework.http.HttpStatusCode;

public record ErrorMessage(
        HttpStatusCode httpStatus,
        int errorCode,
        String message) {
}
