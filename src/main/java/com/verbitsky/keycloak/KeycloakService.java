package com.verbitsky.keycloak;

import com.verbitsky.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.keycloak.response.KeycloakUserInfoResponse;
import reactor.core.publisher.Mono;

public interface KeycloakService {
    Mono<KeycloakLoginResponse> processLogin(String userName, String password);

    Mono<KeycloakIntrospectResponse> introspectToken(String token);

    Mono<KeycloakUserInfoResponse> getUserInfoByToken(String token);

    Mono<KeycloakLogoutResponse> processLogout(String userSub);

    Mono<KeycloakLoginResponse> processRefreshToken(String token);
}
