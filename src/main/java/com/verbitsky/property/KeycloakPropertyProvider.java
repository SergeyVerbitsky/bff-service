package com.verbitsky.property;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class KeycloakPropertyProvider {

    private final OAuth2ClientProperties.Provider keycloakProvider;
    private final ClientRegistration keycloakClient;
    private static final String KEYCLOAK_CLIENT = "keycloak";

    public KeycloakPropertyProvider(OAuth2ClientProperties properties, ClientRegistrationRepository clientRepository) {
        CaseInsensitiveMap<String, OAuth2ClientProperties.Provider> providers =
                new CaseInsensitiveMap<>(properties.getProvider());
        keycloakProvider = providers.get(KEYCLOAK_CLIENT);
        keycloakClient = clientRepository.findByRegistrationId(KEYCLOAK_CLIENT);
    }

    public String provideTokenUri() {
        return keycloakProvider.getTokenUri();
    }

    public String provideIntrospectionUri() {
        return "https://mykeycloak:9443/realms/dog_app_realm/protocol/openid-connect/token/introspect";
    }

    public String provideUserInfoUri() {
        return "https://mykeycloak:9443/realms/dog_app_realm/protocol/openid-connect/userinfo";
    }

    public String provideUserLogoutUri() {
        return "https://mykeycloak:9443/admin/realms/dog_app_realm/users/%s/logout";
    }

    public String provideClientId() {
        return keycloakClient.getClientId();
    }

    public String provideClientSecret() {
        return keycloakClient.getClientSecret();
    }
}
