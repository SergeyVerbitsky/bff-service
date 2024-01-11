package com.verbitsky.service.keycloak.request;

@SuppressWarnings("unused")
public final class KeycloakFields {
    public static final String BEARER_VALUE = "Bearer ";
    public static final String CLIENT_ID_FIELD = "client_id";
    public static final String CLIENT_SECRET_FIELD = "client_secret";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String CREDENTIALS = "credentials";
    public static final String EMAIL = "email";
    public static final String USER_LOGIN = "preferred_username";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public static final String KEYCLOAK_USER_ID = "sub";
    public static final String USER_SESSION_ID = "sid";
    public static final String ENABLE_USER = "enabled";
    public static final String TOKEN = "token";
    public static final String REFRESH_TOKEN = "refresh_token";

    private KeycloakFields() {
    }
}
