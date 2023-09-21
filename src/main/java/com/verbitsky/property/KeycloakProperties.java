package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("services.keycloak.app-admin-user")
record KeycloakProperties(
        String userName,
        String password) {
}
