package com.verbitsky.service.keycloak.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.verbitsky.api.client.request.RemoteApiRequest;
import com.verbitsky.property.KeycloakProperties;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.verbitsky.service.keycloak.request.KeycloakFields.GRANT_TYPE_PASSWORD;

@Component
public final class KeycloakRequestFactory {
    private final KeycloakProperties keycloakProperties;

    public KeycloakRequestFactory(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public RemoteApiRequest buildLoginRequest(String userName, String password) {
        return new RemoteApiRequest(URI.create(keycloakProperties.tokenUrl()),
                buildContentTypeHeaderFormUrlencoded(), buildLoginRequestFields(userName, password));
    }

    public RemoteApiRequest buildRefreshTokenRequest(String token) {
        return new RemoteApiRequest(URI.create(keycloakProperties.tokenUrl()),
                buildContentTypeHeaderFormUrlencoded(), buildRefreshTokenRequestFields(token));
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

    private Map<String, String> buildRequestSecretFields() {
        return Map.of(
                KeycloakFields.CLIENT_ID_FIELD, keycloakProperties.clientId(),
                KeycloakFields.CLIENT_SECRET_FIELD, keycloakProperties.clientSecret()
        );
    }

    private HttpHeaders buildContentTypeHeaderFormUrlencoded() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return headers;
    }
}
