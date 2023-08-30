package com.verbitsky.service.backend.request;

import lombok.Getter;

import static com.verbitsky.service.backend.RequestEndpointKeys.USER_SESSION_URI_KEY;

public enum RequestType {
    GET_USER_SESSION(USER_SESSION_URI_KEY);
    @Getter
    private final String uriKey;

    RequestType(String uriKey) {
        this.uriKey = uriKey;

    }
}
