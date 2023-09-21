package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("services.backend-service")
public record BackendProperties(
        String port,
        String host,
        String scheme,
        Map<String, String> endpointMap) {
}
