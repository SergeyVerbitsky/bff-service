package com.verbitsky.service.keycloak;


import reactor.core.publisher.Mono;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.api.client.response.CommonApiResponse;
import com.verbitsky.api.client.response.NoContentApiResponse;
import com.verbitsky.exception.AuthException;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.property.KeycloakProperties;
import com.verbitsky.service.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.service.keycloak.response.ExternalApiError;
import com.verbitsky.service.keycloak.response.KeycloakTokenResponse;

import javax.ws.rs.core.Response;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Lazy(value = false)
class KeycloakServiceImpl implements KeycloakService {
    private static final boolean EXTERNAL_SERVICE_FLAG = true;
    private final RemoteServiceClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;
    private final Keycloak keycloakAdmin;
    private final KeycloakProperties keycloakProperties;
    private RealmResource realm;

    KeycloakServiceImpl(RemoteServiceClient keycloakClient, KeycloakRequestFactory requestBuilder,
                        Keycloak keycloakAdmin, KeycloakProperties keycloakProperties) {

        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
        this.keycloakAdmin = keycloakAdmin;
        this.keycloakProperties = keycloakProperties;

        initService();
    }

    @Override
    public Mono<ApiResponse> processLogin(String userName, String password) {
        var request = requestFactory.buildLoginRequest(userName, password);
        return keycloakClient.post(
                        request, KeycloakTokenResponse.class, ExternalApiError.class, EXTERNAL_SERVICE_FLAG)
                .doOnNext(apiResponse -> log.info("received response from keycloak: {}", apiResponse));
    }

    @Override
    public Mono<ApiResponse> processLogout(String userId, String sessionId) {
        UserResource userResource = realm.users().get(userId);
        userResource.getUserSessions().stream()
                .filter(session -> Objects.equals(session.getId(), sessionId))
                .findAny()
                .ifPresentOrElse(userSessionRepresentation -> userResource.logout(),
                        () -> {
                            throw new AuthException(HttpStatus.NOT_FOUND,
                                    "SessionEntity with id:%s and keycloak_id:%s not found".formatted(sessionId, userId));
                        });

        return Mono.just(new NoContentApiResponse(HttpStatus.NO_CONTENT));
    }

    @Override
    public Mono<ApiResponse> processUserRegistration(RegisterRequest request) {
        UserRepresentation user = createUserRepresentation(request);
        CredentialRepresentation credential = createCredentialRepresentation(request);

        user.setCredentials(List.of(credential));

        UsersResource users = this.realm.users();
        Response response = users.create(user);

        return convertToApiResponse(response);
    }

    @Override
    public Mono<ApiResponse> exchangeRefreshToken(String token) {
        var request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient
                .post(request, KeycloakTokenResponse.class, ExternalApiError.class, EXTERNAL_SERVICE_FLAG);
    }

    @SuppressWarnings("SameParameterValue")
    private Mono<ApiResponse> convertToCommonApiResponse(Response response, Class<? extends Serializable> bodyType) {
        boolean responseHasEntity = response.hasEntity();
        if (responseHasEntity) {
            Serializable entity = response.readEntity(bodyType);
            return Mono.just(CommonApiResponse.of(entity, HttpStatus.valueOf(response.getStatus())));
        }

        return Mono.just(new NoContentApiResponse(HttpStatus.valueOf(response.getStatus())));
    }

    private Mono<ApiResponse> convertToCommonApiError(Response response) {
        ExternalApiError externalApiError = response.readEntity(ExternalApiError.class);
        CommonApiResponse apiResponse = CommonApiResponse.of(
                externalApiError, HttpStatus.valueOf(response.getStatus()));

        return Mono.just(apiResponse);
    }

    private static CredentialRepresentation createCredentialRepresentation(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        return credential;
    }

    private static UserRepresentation createUserRepresentation(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.userName());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setEnabled(true);
        return user;
    }

    private void initService() {
        realm = keycloakAdmin.realm(keycloakProperties.realm());
    }

    private Mono<ApiResponse> convertToApiResponse(Response response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatus());
        if (httpStatus.isError()) {
            return convertToCommonApiError(response);
        } else {
            return convertToCommonApiResponse(response, NoContentApiResponse.class);
        }
    }
}