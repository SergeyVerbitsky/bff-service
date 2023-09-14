package com.verbitsky.service.keycloak;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.service.RemoteApiRequest;
import com.verbitsky.service.keycloak.client.KeycloakAction;
import com.verbitsky.service.keycloak.exception.InvalidKeycloakRequestException;
import com.verbitsky.service.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.service.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.service.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.service.keycloak.response.KeycloakUserInfoResponse;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
class KeycloakServiceImpl implements KeycloakService {
    private final RemoteServiceClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;

    KeycloakServiceImpl(RemoteServiceClient keycloakClient, KeycloakRequestFactory requestBuilder) {
        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
    }

    @Override
    public Mono<KeycloakLoginResponse> processLogin(String userName, String password) {
        RemoteApiRequest request = requestFactory.buildLoginRequest(userName, password);
        return keycloakClient.post(
                request,
                KeycloakLoginResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnLoginSuccess(),
                getOnError());
    }

    @Override
    public Mono<KeycloakIntrospectResponse> introspectToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(
                    HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        RemoteApiRequest request = requestFactory.buildTokenIntrospectionRequest(token);
        return keycloakClient.post(
                request,
                KeycloakIntrospectResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnIntrospectionSuccess(),
                getOnError());
    }

    @Override
    public Mono<KeycloakUserInfoResponse> getUserInfoByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(
                    HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        RemoteApiRequest request = requestFactory.buildUserInfoRequest(token);
        return keycloakClient.get(
                request,
                KeycloakUserInfoResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnUserInfoSuccess(),
                getOnError());
    }

    @Override
    public Mono<KeycloakLogoutResponse> processLogout(String userSub) {
        RemoteApiRequest request = requestFactory.buildLogoutRequest(userSub);
        return keycloakClient.post(
                request,
                KeycloakLogoutResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnLogoutSuccess(),
                getOnError());
    }

    @Override
    public Mono<KeycloakLoginResponse> processRefreshToken(String token) {
        RemoteApiRequest request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient.post(
                request,
                KeycloakLoginResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnLoginSuccess(),
                getOnError());
    }

    private Predicate<HttpStatusCode> getStatusPredicate() {
        //todo impl correct predicate
        return httpStatusCode -> false;
    }

    private Function<ClientResponse, Mono<? extends Throwable>> getStatusExceptionFunc() {
        //todo impl correct func
        return response -> Mono.empty();
    }

    private Consumer<KeycloakLoginResponse> getOnLoginSuccess() {
        //todo implement
        return whatever -> {
        };
    }

    private Consumer<KeycloakIntrospectResponse> getOnIntrospectionSuccess() {
        //todo implement
        return whatever -> {
        };
    }

    private Consumer<KeycloakLogoutResponse> getOnLogoutSuccess() {
        //todo implement
        return whatever -> {
        };
    }

    private Consumer<KeycloakUserInfoResponse> getOnUserInfoSuccess() {
        //todo implement
        return whatever -> {
        };
    }

    private Consumer<? super Throwable> getOnError() {
        //todo implement
        return whatever -> {
        };
    }
}