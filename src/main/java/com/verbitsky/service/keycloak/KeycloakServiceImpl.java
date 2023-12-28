package com.verbitsky.service.keycloak;


import reactor.core.publisher.Mono;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
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
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.property.KeycloakProperties;
import com.verbitsky.service.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.service.keycloak.response.ExternalApiError;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;

import javax.ws.rs.core.Response;

import java.io.Serializable;
import java.util.List;

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
                request, KeycloakLoginResponse.class, ExternalApiError.class, EXTERNAL_SERVICE_FLAG);
    }

    @Override
    public Mono<ApiResponse> processLogout(String userId) {
        realm.users().get(userId).logout();
        return Mono.just(new NoContentApiResponse());
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
    public Mono<ApiResponse> processRefreshToken(String token) {
        var request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient
                .post(request, KeycloakLoginResponse.class, ExternalApiError.class, EXTERNAL_SERVICE_FLAG);
    }

    @SuppressWarnings("SameParameterValue")
    private Mono<ApiResponse> convertToCommonApiResponse(Response response, Class<? extends Serializable> bodyType) {
        boolean responseHasEntity = response.hasEntity();
        if (responseHasEntity) {
            Serializable entity = response.readEntity(bodyType);
            return Mono.just(CommonApiResponse.of(entity, HttpStatus.valueOf(response.getStatus())));
        }

        return Mono.just(new NoContentApiResponse());
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