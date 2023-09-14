package com.verbitsky.service.backend.request;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.verbitsky.property.BackendProperties;
import com.verbitsky.service.RemoteApiRequest;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@Component
@EnableConfigurationProperties(BackendProperties.class)
public final class BackendRequestBuilder {
    private final BackendProperties properties;
    private HttpHeaders requestHeaders;

    public BackendRequestBuilder(BackendProperties backendProperties) {
        this.properties = backendProperties;
        initHeaderValue();
    }

    public RemoteApiRequest buildRequest(RequestType requestType, Map<String, String> uriParams) {
        return buildRequest(requestType, uriParams, Collections.emptyMap());
    }

    public RemoteApiRequest buildRequest(RequestType requestType, Map<String, String> uriParams,
                                         Map<String, String> bodyFields) {

        return new RemoteApiRequest(resolveUri(requestType, uriParams), this.requestHeaders, bodyFields);
    }

    private URI resolveUri(RequestType requestType, Map<String, String> params) {
        return new DefaultUriBuilderFactory().builder()
                .scheme(properties.scheme())
                .port(properties.port())
                .host(properties.host())
                .path(properties.endpointMap().get(requestType.getUriKey()))
                .build(params);
    }

    private void initHeaderValue() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        this.requestHeaders = headers;
    }
}
