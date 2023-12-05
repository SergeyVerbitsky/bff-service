package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("services.keycloak")
record KeycloakProperties(
        String adminUsername,
        String adminPassword,
        int userPassHashIteration,
        Map<String, String> endpointMap) {
}
