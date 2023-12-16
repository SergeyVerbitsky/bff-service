package com.verbitsky.service.keycloak.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.verbitsky.property.KeycloakPropertyProvider;
import com.verbitsky.service.RemoteApiRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.verbitsky.service.keycloak.request.KeycloakFields.CREDENTIALS;
import static com.verbitsky.service.keycloak.request.KeycloakFields.GRANT_TYPE_PASSWORD;

@Component
public final class KeycloakRequestFactory {
    private static final boolean BLOCKED_PASSWORD = false;
    private final KeycloakPropertyProvider keycloakPropertyProvider;

    public KeycloakRequestFactory(KeycloakPropertyProvider propertyProvider) {
        this.keycloakPropertyProvider = propertyProvider;
    }

    public RemoteApiRequest buildLoginRequest(String userName, String password) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideTokenUri(),
                buildContentTypeHeaderFormUrlencoded(), buildLoginRequestFields(userName, password));
    }

    public RemoteApiRequest buildUserRegistrationRequest(Map<String, String> regData, String adminToken) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideUserRegistrationUri(),
                buildUserRegistrationRequestHeaders(adminToken), buildUserRegistrationRequestFields(regData));
    }

    public RemoteApiRequest buildTokenIntrospectionRequest(String token) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideIntrospectionUri(),
                buildContentTypeHeaderFormUrlencoded(), buildTokenIntrospectionRequestFields(token));
    }

    public RemoteApiRequest buildRefreshTokenRequest(String token) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideTokenUri(),
                buildContentTypeHeaderFormUrlencoded(), buildRefreshTokenRequestFields(token));
    }

    @SuppressWarnings("unused")
    public RemoteApiRequest buildUserInfoRequest(String token) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideUserInfoUri(),
                buildUserInfoRequestHeaders(token), new HashMap<>());
    }

    public RemoteApiRequest buildLogoutRequest(String userId) {
        return new RemoteApiRequest(keycloakPropertyProvider.provideUserLogoutUri(userId),
                buildContentTypeHeaderFormUrlencoded(), buildRequestSecretFields());
    }

    private Map<String, String> buildRefreshTokenRequestFields(String refreshToken) {
        var result = new HashMap<>(Map.of(
                KeycloakFields.GRANT_TYPE_KEY, KeycloakFields.REFRESH_TOKEN,
                KeycloakFields.REFRESH_TOKEN, refreshToken
        ));
        result.putAll(buildRequestSecretFields());

        return result;
    }

    private Map<String, String> buildLoginRequestFields(String userName, String password) {
        var result = new HashMap<>(Map.of(
                KeycloakFields.GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD,
                KeycloakFields.USER_NAME, userName,
                KeycloakFields.PASSWORD, password
        ));
        result.putAll(buildRequestSecretFields());

        return result;
    }

    private Map<String, String> buildTokenIntrospectionRequestFields(String token) {
        var result = new HashMap<>(Map.of(
                KeycloakFields.TOKEN, token
        ));
        result.putAll(buildRequestSecretFields());

        return result;
    }

    private Map<String, String> buildRequestSecretFields() {
        return Map.of(
                KeycloakFields.CLIENT_ID_FIELD, keycloakPropertyProvider.provideClientId(),
                KeycloakFields.CLIENT_SECRET_FIELD, keycloakPropertyProvider.provideClientSecret()
        );
    }

    private HttpHeaders buildContentTypeHeaderFormUrlencoded() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return headers;
    }

    private HttpHeaders buildUserInfoRequestHeaders(String token) {
        HttpHeaders headers = buildContentTypeHeaderFormUrlencoded();
        headers.add(HttpHeaders.AUTHORIZATION, KeycloakFields.BEARER_VALUE.concat(token));

        return headers;
    }

    private Map<String, Object> buildUserRegistrationRequestFields(Map<String, String> regData) {
        String password = regData.get(KeycloakFields.PASSWORD);
        Credentials credentials = new Credentials(GRANT_TYPE_PASSWORD, password,
                keycloakPropertyProvider.provideUserPassHashIteration(), BLOCKED_PASSWORD);
        Map<String, Object> requestFields = new HashMap<>(regData);
        requestFields.put(CREDENTIALS, List.of(credentials));

        return requestFields;
    }

    private HttpHeaders buildUserRegistrationRequestHeaders(String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, KeycloakFields.BEARER_VALUE.concat(adminToken));

        return headers;
    }

    private record Credentials(
            String type,
            String value,
            int hashIterations,
            boolean temporary) {
    }
}
