package com.verbitsky.keycloak;

import com.verbitsky.keycloak.exception.InvalidKeycloakRequestException;
import com.verbitsky.keycloak.client.KeycloakAction;
import com.verbitsky.keycloak.client.KeycloakClient;
import com.verbitsky.keycloak.request.KeycloakRequest;
import com.verbitsky.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.keycloak.response.KeycloakUserInfoResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KeycloakServiceImpl implements KeycloakService {
    private final KeycloakClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;

    public KeycloakServiceImpl(KeycloakClient keycloakClient, KeycloakRequestFactory requestBuilder) {
        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
    }

    @Override
    public Mono<KeycloakLoginResponse> processLogin(String userName, String password) {
        KeycloakRequest request = requestFactory.buildLoginRequest(userName, password);
        return keycloakClient.post(request, KeycloakLoginResponse.class);
    }

    @Override
    public Mono<KeycloakIntrospectResponse> introspectToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        KeycloakRequest request = requestFactory.buildTokenIntrospectionRequest(token);
        return keycloakClient.post(request, KeycloakIntrospectResponse.class);
    }

    @Override
    public Mono<KeycloakUserInfoResponse> getUserInfoByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        KeycloakRequest request = requestFactory.buildUserInfoRequest(token);
        return keycloakClient.get(request, KeycloakUserInfoResponse.class);
    }

    @Override
    public Mono<KeycloakLogoutResponse> processLogout(String userSub) {
        KeycloakRequest request = requestFactory.buildLogoutRequest(userSub);
        return keycloakClient.post(request, KeycloakLogoutResponse.class);
    }

    @Override
    public Mono<KeycloakLoginResponse> processRefreshToken(String token) {
        KeycloakRequest request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient
                .post(request, KeycloakLoginResponse.class);
    }
}