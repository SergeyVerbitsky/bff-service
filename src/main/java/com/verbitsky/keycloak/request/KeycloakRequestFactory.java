package com.verbitsky.keycloak.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.verbitsky.keycloak.request.KeycloakFields.BEARER_VALUE;
import static com.verbitsky.keycloak.request.KeycloakFields.CLIENT_ID_FIELD;
import static com.verbitsky.keycloak.request.KeycloakFields.CLIENT_SECRET_FIELD;
import static com.verbitsky.keycloak.request.KeycloakFields.GRANT_TYPE_KEY;
import static com.verbitsky.keycloak.request.KeycloakFields.GRANT_TYPE_PASSWORD;
import static com.verbitsky.keycloak.request.KeycloakFields.PASSWORD;
import static com.verbitsky.keycloak.request.KeycloakFields.TOKEN;
import static com.verbitsky.keycloak.request.KeycloakFields.USER_NAME;
import com.verbitsky.keycloak.client.KeycloakAction;
import com.verbitsky.model.ApiLoginRequest;
import com.verbitsky.property.KeycloakPropertyProvider;

@Component
public class KeycloakRequestFactory {
    private final KeycloakPropertyProvider keycloakPropertyProvider;

    public KeycloakRequestFactory(KeycloakPropertyProvider propertyProvider) {
        this.keycloakPropertyProvider = propertyProvider;
    }

    public KeycloakRequest buildLoginRequest(ApiLoginRequest loginRequest) {
        KeycloakRequest request = new KeycloakRequest();
        request.setAction(KeycloakAction.LOGIN);
        request.setEndpointUrl(keycloakPropertyProvider.provideTokenUri());
        request.setHeaders(buildContentTypeHeaderFormUrlencoded());
        request.setRequestFields(buildLoginRequestFields(loginRequest));

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

    private MultiValueMap<String, String> buildLoginRequestFields(ApiLoginRequest loginRequest) {
        LinkedMultiValueMap<String, String> fieldsMap = new LinkedMultiValueMap<>();
        fieldsMap.add(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        fieldsMap.add(USER_NAME, loginRequest.userName());
        fieldsMap.add(PASSWORD, loginRequest.password());
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
        headerMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headerMap.add(HttpHeaders.AUTHORIZATION, BEARER_VALUE.concat(token));
        return headerMap;
    }
}
