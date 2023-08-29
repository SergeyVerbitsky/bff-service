package com.verbitsky.keycloak.request;

import com.verbitsky.keycloak.client.KeycloakAction;
import com.verbitsky.property.KeycloakPropertyProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.verbitsky.keycloak.request.KeycloakFields.*;

@Component
public class KeycloakRequestFactory {
    private final KeycloakPropertyProvider keycloakPropertyProvider;

    public KeycloakRequestFactory(KeycloakPropertyProvider propertyProvider) {
        this.keycloakPropertyProvider = propertyProvider;
    }

    public KeycloakRequest buildLoginRequest(String userName, String password) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.LOGIN);
        request.setEndpointUrl(keycloakPropertyProvider.provideTokenUri());
        request.setHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildLoginRequestFields(userName, password));

        return request;
    }

    public KeycloakRequest buildTokenIntrospectionRequest(String token) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.TOKEN_INTROSPECTION);
        request.setEndpointUrl(keycloakPropertyProvider.provideIntrospectionUri());
        request.setHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildTokenIntrospectionRequestFields(token));

        return request;
    }

    public KeycloakRequest buildRefreshTokenRequest(String token) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.TOKEN_REFRESH);
        request.setEndpointUrl(keycloakPropertyProvider.provideTokenUri());
        request.setHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildRefreshTokenRequestFields(token));

        return request;
    }

    public KeycloakRequest buildUserInfoRequest(String token) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.USER_INFO);
        request.setEndpointUrl(keycloakPropertyProvider.provideUserInfoUri());
        request.setHeaders(buildUserInfoRequestHeaders(token));

        return request;
    }

    public KeycloakRequest buildLogoutRequest(String userId) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.LOGOUT);
        request.setEndpointUrl(String.format(keycloakPropertyProvider.provideUserLogoutUri(), userId));
        request.setRequestFields(buildRequestSecretFields());
        request.setHeaders(buildContentTypeHeaderFormUrlencoded());

        return request;
    }

    private LinkedMultiValueMap<String, String> buildRefreshTokenRequestFields(String refreshToken) {
        LinkedMultiValueMap<String, String> fields = new LinkedMultiValueMap<>();
        fields.addAll(buildRequestSecretFields());
        fields.add(GRANT_TYPE_KEY, REFRESH_TOKEN);
        fields.add(REFRESH_TOKEN, refreshToken);

        return fields;
    }

    private MultiValueMap<String, String> buildLoginRequestFields(String userName, String password) {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        fieldsMap.add(USER_NAME, userName);
        fieldsMap.add(PASSWORD, password);
        fieldsMap.addAll(buildRequestSecretFields());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildTokenIntrospectionRequestFields(String token) {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(TOKEN, token);
        fieldsMap.addAll(buildRequestSecretFields());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildRequestSecretFields() {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(CLIENT_ID_FIELD, keycloakPropertyProvider.provideClientId());
        fieldsMap.add(CLIENT_SECRET_FIELD, keycloakPropertyProvider.provideClientSecret());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildContentTypeHeaderFormUrlencoded() {
        LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(1);
        headerMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headerMap;
    }

    private MultiValueMap<String, String> buildUserInfoRequestHeaders(String token) {
        LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(2);
        headerMap.add(HttpHeaders.AUTHORIZATION, BEARER_VALUE.concat(token));
        headerMap.addAll(buildContentTypeHeaderFormUrlencoded());
        return headerMap;
    }
}
