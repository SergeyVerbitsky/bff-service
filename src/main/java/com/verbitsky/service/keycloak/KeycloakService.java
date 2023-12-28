package com.verbitsky.service.keycloak;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.model.RegisterRequest;

@SuppressWarnings("unused")
public interface KeycloakService {
    Mono<ApiResponse> processLogin(String userName, String password);

    Mono<ApiResponse> processLogout(String userId);

    Mono<ApiResponse> processUserRegistration(RegisterRequest regData);

    Mono<ApiResponse> processRefreshToken(String token);
}
