package com.verbitsky.service.auth;

import reactor.core.publisher.Mono;

import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.model.BffLogoutRequest;
import com.verbitsky.model.BffRegisterRequest;
import com.verbitsky.model.BffRegisterResponse;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;

public interface AuthService {
    Mono<BffLoginResponse> processLoginUser(BffLoginRequest loginRequest);

    Mono<BffRegisterResponse> processUserRegistration(BffRegisterRequest registerRequest);

    void processUserLogout(BffLogoutRequest userId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId);
}
