package com.verbitsky.service.keycloak;

import reactor.core.publisher.Mono;

import com.verbitsky.service.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.service.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.service.keycloak.response.KeycloakUserInfoResponse;

public interface KeycloakService {
    Mono<KeycloakLoginResponse> processLogin(String userName, String password);

    Mono<KeycloakIntrospectResponse> introspectToken(String token);

    Mono<KeycloakUserInfoResponse> getUserInfoByToken(String token);

    Mono<KeycloakLogoutResponse> processLogout(String userSub);

    Mono<KeycloakLoginResponse> processRefreshToken(String token);
}
