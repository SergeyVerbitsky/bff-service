package com.verbitsky.service.keycloak.request;

import com.verbitsky.property.KeycloakPropertyProvider;
import com.verbitsky.service.RemoteApiRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public final class KeycloakRequestFactory {
    private final KeycloakPropertyProvider keycloakPropertyProvider;

    public KeycloakRequestFactory(KeycloakPropertyProvider propertyProvider) {
        this.keycloakPropertyProvider = propertyProvider;
    }

    public RemoteApiRequest buildLoginRequest(String userName, String password) {
        RemoteApiRequest request = new RemoteApiRequest(keycloakPropertyProvider.provideTokenUri());
        request.setRequestHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildLoginRequestFields(userName, password));

        return request;
    }

    public RemoteApiRequest buildTokenIntrospectionRequest(String token) {
        RemoteApiRequest request = new RemoteApiRequest(keycloakPropertyProvider.provideIntrospectionUri());
        request.setRequestHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildTokenIntrospectionRequestFields(token));

        return request;
    }

    public RemoteApiRequest buildRefreshTokenRequest(String token) {
        RemoteApiRequest request = new RemoteApiRequest(keycloakPropertyProvider.provideTokenUri());
        request.setRequestHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildRefreshTokenRequestFields(token));

        return request;
    }

    public RemoteApiRequest buildUserInfoRequest(String token) {
        RemoteApiRequest request = new RemoteApiRequest(keycloakPropertyProvider.provideUserInfoUri());
        request.setRequestHeaders(buildUserInfoRequestHeaders(token));

        return request;
    }

    public RemoteApiRequest buildLogoutRequest(String userId) {
        RemoteApiRequest request = new RemoteApiRequest(keycloakPropertyProvider.provideUserLogoutUri(userId));
        request.setRequestFields(buildRequestSecretFields());
        request.setRequestHeaders(buildContentTypeHeaderFormUrlencoded());

        return request;
    }

    private LinkedMultiValueMap<String, String> buildRefreshTokenRequestFields(String refreshToken) {
        LinkedMultiValueMap<String, String> fields = new LinkedMultiValueMap<>();
        fields.addAll(buildRequestSecretFields());
        fields.add(KeycloakFields.GRANT_TYPE_KEY, KeycloakFields.REFRESH_TOKEN);
        fields.add(KeycloakFields.REFRESH_TOKEN, refreshToken);

        return fields;
    }

    private MultiValueMap<String, String> buildLoginRequestFields(String userName, String password) {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(KeycloakFields.GRANT_TYPE_KEY, KeycloakFields.GRANT_TYPE_PASSWORD);
        fieldsMap.add(KeycloakFields.USER_NAME, userName);
        fieldsMap.add(KeycloakFields.PASSWORD, password);
        fieldsMap.addAll(buildRequestSecretFields());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildTokenIntrospectionRequestFields(String token) {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(KeycloakFields.TOKEN, token);
        fieldsMap.addAll(buildRequestSecretFields());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildRequestSecretFields() {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(KeycloakFields.CLIENT_ID_FIELD, keycloakPropertyProvider.provideClientId());
        fieldsMap.add(KeycloakFields.CLIENT_SECRET_FIELD, keycloakPropertyProvider.provideClientSecret());

        return fieldsMap;
    }

    private MultiValueMap<String, String> buildContentTypeHeaderFormUrlencoded() {
        LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(1);
        headerMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headerMap;
    }

    private MultiValueMap<String, String> buildUserInfoRequestHeaders(String token) {
        LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(2);
        headerMap.add(HttpHeaders.AUTHORIZATION, KeycloakFields.BEARER_VALUE.concat(token));
        headerMap.addAll(buildContentTypeHeaderFormUrlencoded());
        return headerMap;
    }
}
