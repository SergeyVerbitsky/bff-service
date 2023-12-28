package com.verbitsky.service.auth;

import reactor.core.publisher.Mono;

import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LoginResponse;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.model.RegisterResponse;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;

public interface AuthService {
    Mono<LoginResponse> processLoginUser(LoginRequest loginRequest);

    Mono<RegisterResponse> processUserRegistration(RegisterRequest registerRequest);

    void processUserLogout(LogoutRequest userId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId);
}
