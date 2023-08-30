package com.verbitsky.service.keycloak;

import com.verbitsky.service.RemoteApiRequest;
import com.verbitsky.service.RemoteServiceClient;
import com.verbitsky.service.keycloak.client.KeycloakAction;
import com.verbitsky.service.keycloak.exception.InvalidKeycloakRequestException;
import com.verbitsky.service.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.service.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.service.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.service.keycloak.response.KeycloakUserInfoResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class KeycloakServiceImpl implements KeycloakService {
    private final RemoteServiceClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;

    KeycloakServiceImpl(@Qualifier("keycloakClient") RemoteServiceClient keycloakClient, KeycloakRequestFactory requestBuilder) {
        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
    }

    @Override
    public Mono<KeycloakLoginResponse> processLogin(String userName, String password) {
        RemoteApiRequest request = requestFactory.buildLoginRequest(userName, password);
        return keycloakClient.post(request, KeycloakLoginResponse.class);
    }

    @Override
    public Mono<KeycloakIntrospectResponse> introspectToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        RemoteApiRequest request = requestFactory.buildTokenIntrospectionRequest(token);
        return keycloakClient.post(request, KeycloakIntrospectResponse.class);
    }

    @Override
    public Mono<KeycloakUserInfoResponse> getUserInfoByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        RemoteApiRequest request = requestFactory.buildUserInfoRequest(token);
        return keycloakClient.get(request, KeycloakUserInfoResponse.class);
    }

    @Override
    public Mono<KeycloakLogoutResponse> processLogout(String userSub) {
        RemoteApiRequest request = requestFactory.buildLogoutRequest(userSub);
        return keycloakClient.post(request, KeycloakLogoutResponse.class);
    }

    @Override
    public Mono<KeycloakLoginResponse> processRefreshToken(String token) {
        RemoteApiRequest request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient
                .post(request, KeycloakLoginResponse.class);
    }
}