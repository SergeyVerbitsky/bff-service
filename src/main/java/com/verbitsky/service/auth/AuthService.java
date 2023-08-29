package com.verbitsky.service.auth;

import com.verbitsky.model.ApiLoginRequest;
import com.verbitsky.model.ApiLoginResponse;
import com.verbitsky.model.ApiRegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import reactor.core.publisher.Mono;

public interface AuthService {
    CustomUserDetails getUserById(String userId);

    Mono<ApiLoginResponse> processLoginUser(ApiLoginRequest loginRequest);

    void processUserRegistration(ApiRegisterRequest registerRequest);
    void processUserLogout(String userId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId);
}
