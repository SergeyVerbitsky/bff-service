package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("http-client")
public record WebClientProperties(
        int connectionTimeout,
        int responseTimeout,
        int readTimeout,
        int writeTimeout) {
}
