package com.verbitsky.service.auth;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.verbitsky.keycloak.request.KeycloakFields.TOKEN;
import com.google.common.cache.Cache;
import com.verbitsky.converter.KeycloakResponseConverter;
import com.verbitsky.exception.InvalidKeycloakRequest;
import com.verbitsky.keycloak.client.KeycloakAction;
import com.verbitsky.keycloak.client.KeycloakClient;
import com.verbitsky.keycloak.request.KeycloakRequest;
import com.verbitsky.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.keycloak.response.KeycloakTokenResponse;
import com.verbitsky.keycloak.response.KeycloakUserInfoResponse;
import com.verbitsky.model.ApiLoginRequest;
import com.verbitsky.model.UserModel;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl {
    private final KeycloakClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;
    private final KeycloakResponseConverter<KeycloakIntrospectResponse, UserModel> introspectResponseConverter;
    private final Cache<String, UserModel> tokenCache;

    public AuthServiceImpl(KeycloakClient keycloakClient, KeycloakRequestFactory requestBuilder,
                           KeycloakResponseConverter<KeycloakIntrospectResponse, UserModel> converter,
                           @Qualifier("tokenCache") Cache<String, UserModel> tokenCache) {
        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
        this.introspectResponseConverter = converter;
        this.tokenCache = tokenCache;
    }

    public Mono<KeycloakTokenResponse> processLogin(ApiLoginRequest loginRequest) {
        // TODO: 17.05.2023 mb need to check if user already logged in
        KeycloakRequest request = requestFactory.buildLoginRequest(loginRequest);
        return keycloakClient
                .post(request, KeycloakTokenResponse.class)
                .doOnNext(addLoggedUserToCache());


    }

    public Mono<KeycloakIntrospectResponse> introspectToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequest(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        KeycloakRequest request = requestFactory.buildTokenIntrospectionRequest(token);
        return keycloakClient.post(request, KeycloakIntrospectResponse.class);
    }

    public Mono<KeycloakUserInfoResponse> getUserInfo(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequest(HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        KeycloakRequest request = requestFactory.buildUserInfoRequest(token);
        return keycloakClient.get(request, KeycloakUserInfoResponse.class);
    }

    public Mono<KeycloakLogoutResponse> processLogout(String userSub) {
        KeycloakRequest request = requestFactory.buildLogoutRequest(userSub);
        return keycloakClient.post(request, KeycloakLogoutResponse.class)
                .doOnNext(response ->
                    removeLoggedUserFromCache(userSub));
    }

    private void removeLoggedUserFromCache(String userId) {
        UserModel userModel = tokenCache.getIfPresent(userId);
        if (Objects.nonNull(userModel)) {
            tokenCache.invalidate(userId);
        }
    }

    private Consumer<? super KeycloakTokenResponse> addLoggedUserToCache() {
        return loginResponse ->
                introspectToken(loginResponse.getAccessToken()).subscribe(
                        introspectResponse -> {
                            UserModel userModel = introspectResponseConverter
                                    .convert(introspectResponse, Map.of(TOKEN, loginResponse.getAccessToken()));
                            userModel.setToken(loginResponse.getAccessToken());
                            tokenCache.put(introspectResponse.getUserName(), userModel);
                        });
    }
}
