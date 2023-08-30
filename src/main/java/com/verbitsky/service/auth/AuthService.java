package com.verbitsky.service.auth;

import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.model.BffRegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import reactor.core.publisher.Mono;

public interface AuthService {
    CustomUserDetails getUserById(String userId);

    Mono<BffLoginResponse> processLoginUser(BffLoginRequest loginRequest);

    void processUserRegistration(BffRegisterRequest registerRequest);

    void processUserLogout(String userId);

    CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId);
}
