package com.verbitsky.service.backend.request;

import com.verbitsky.property.BackendProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.util.Map;

@Component
@EnableConfigurationProperties(BackendProperties.class)
public final class BackendRequestFactory {
    private final BackendProperties properties;

    public BackendRequestFactory(BackendProperties backendProperties) {
        this.properties = backendProperties;
    }

    public URI buildUri(RequestType requestType, Map<String, String> uriParams) {
        return resolveUri(requestType, uriParams);
    }

    private URI resolveUri(RequestType requestType, Map<String, String> params) {
        return new DefaultUriBuilderFactory().builder()
                .scheme(properties.scheme())
                .port(properties.port())
                .host(properties.host())
                .path(properties.endpointMap().get(requestType.getUriKey()))
                .build(params);
    }
}
