package com.verbitsky.service.auth;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;

public interface AuthService {
    Mono<ApiResponse> processLoginUser(LoginRequest loginRequest);

    Mono<ApiResponse> processUserRegistration(RegisterRequest registerRequest);

    void processUserLogout(LogoutRequest userId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId);
}
