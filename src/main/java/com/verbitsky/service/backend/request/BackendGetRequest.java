package com.verbitsky.service.backend.request;

public record BackendGetRequest(String requestedUri, Class<?> responseType) {
    public static final String USER_ID_FIELD = "userId";
}

