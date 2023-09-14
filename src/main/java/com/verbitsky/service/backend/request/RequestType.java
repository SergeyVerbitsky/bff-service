package com.verbitsky.service.backend.request;

import lombok.Getter;

import com.verbitsky.service.backend.RequestEndpointKeys;

public enum RequestType {
    USER_SESSION_OP(RequestEndpointKeys.USER_SESSION);

    @Getter
    private final String uriKey;

    RequestType(String uriKey) {
        this.uriKey = uriKey;

    }
}
