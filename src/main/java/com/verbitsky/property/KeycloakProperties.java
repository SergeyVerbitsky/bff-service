package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("services.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String clientId,
        String clientSecret,
        String tokenUrl) {
}
