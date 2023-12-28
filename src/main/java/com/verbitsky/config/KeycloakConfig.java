package com.verbitsky.config;


import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.verbitsky.property.KeycloakProperties;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
class KeycloakConfig {
    @Bean
    Keycloak keycloak(KeycloakProperties keycloakProperties) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.serverUrl())
                .realm(keycloakProperties.realm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakProperties.clientId())
                .clientSecret(keycloakProperties.clientSecret())
                .build();
    }
}
