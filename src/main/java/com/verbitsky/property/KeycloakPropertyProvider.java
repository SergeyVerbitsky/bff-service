package com.verbitsky.property;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakPropertyProvider {
    private static final String INTROSPECTION_URI_KEY = "tokenIntrospection";
    private static final String KEYCLOAK_CLIENT = "keycloak";
    private static final String USER_INFO_URI_KEY = "userInfo";
    private static final String USER_LOGOUT_URI_KEY = "userLogout";
    private static final String USER_REGISTER_URI_KEY = "userRegister";
    private final KeycloakProperties keycloakProperties;
    private final OAuth2ClientProperties.Provider keycloakProvider;
    private final ClientRegistration keycloakClient;


    public KeycloakPropertyProvider(OAuth2ClientProperties properties, KeycloakProperties keycloakProperties,
                                    ClientRegistrationRepository clientRepository) {

        CaseInsensitiveMap<String, OAuth2ClientProperties.Provider> providers =
                new CaseInsensitiveMap<>(properties.getProvider());
        keycloakProvider = providers.get(KEYCLOAK_CLIENT);
        keycloakClient = clientRepository.findByRegistrationId(KEYCLOAK_CLIENT);
        this.keycloakProperties = keycloakProperties;
    }

    public URI provideTokenUri() {
        return URI.create(keycloakProvider.getTokenUri());
    }

    public URI provideIntrospectionUri() {
        return URI.create(keycloakProperties.endpointMap().get(INTROSPECTION_URI_KEY));
    }

    public URI provideUserInfoUri() {
        return URI.create(keycloakProperties.endpointMap().get(USER_INFO_URI_KEY));
    }

    public URI provideUserLogoutUri(String userId) {
        return URI.create(String.format(keycloakProperties.endpointMap().get(USER_LOGOUT_URI_KEY), userId));
    }

    public URI provideUserRegistrationUri() {
        return URI.create(keycloakProperties.endpointMap().get(USER_REGISTER_URI_KEY));
    }

    public String provideClientId() {
        return keycloakClient.getClientId();
    }

    public String provideClientSecret() {
        return keycloakClient.getClientSecret();
    }

    public String provideAdminUserName() {
        return keycloakProperties.adminUsername();
    }

    public String provideAdminUserPassword() {
        return keycloakProperties.adminPassword();
    }

    public int provideUserPassHashIteration() {
        return keycloakProperties.userPassHashIteration();
    }
}
