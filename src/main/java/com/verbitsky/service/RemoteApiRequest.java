package com.verbitsky.service;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.verbitsky.api.client.ApiRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RemoteApiRequest implements ApiRequest {
    private final URI requestedUri;
    private final HttpHeaders requestHeaders;
    private final Map<String, ? super String> requestFields;

    public RemoteApiRequest(@NonNull URI requestedUri, @NonNull HttpHeaders headers,
                            @NonNull Map<String, ? super String> fields) {

        this.requestedUri = requestedUri;
        this.requestFields = fields;
        this.requestHeaders = headers;
    }

    @Override
    public URI getRequestedUri() {
        return requestedUri;
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Map<String, ? super String> getRequestFields() {
        return requestFields;
    }

    public MultiValueMap<String, String> getRequestFieldsAsMultiValueMap() {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        requestFields.forEach((key, value) -> result.put(key, List.of(value.toString())));

        return result;
    }
}
