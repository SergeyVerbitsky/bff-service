package com.verbitsky.service.keycloak;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.ApiResponse;

import java.util.Map;

@SuppressWarnings("unused")
public interface KeycloakService {
    Mono<ApiResponse> processLogin(String userName, String password);

    Mono<ApiResponse> processUserRegistration(Map<String, String> regData);

    Mono<ApiResponse> processRefreshToken(String token);

    void init();
}
