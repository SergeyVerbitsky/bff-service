package com.verbitsky.service.auth;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;

public interface AuthService {
    Mono<ApiResponse> processLoginUser(String login, String password, String deviceId);

    Mono<ApiResponse> processUserRegistration(RegisterRequest registerRequest);

    void processUserLogout(String userId, String sessionId, String deviceId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId, String deviceId);
}
