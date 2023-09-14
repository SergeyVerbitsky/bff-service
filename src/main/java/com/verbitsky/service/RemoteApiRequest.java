package com.verbitsky.service;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.verbitsky.api.client.ApiRequest;

import java.net.URI;
import java.util.Map;

public class RemoteApiRequest implements ApiRequest {
    private final URI requestedUri;
    private final HttpHeaders requestHeaders;
    private final Map<String, String> requestFields;

    public RemoteApiRequest(@NonNull URI requestedUri, @NonNull HttpHeaders headers,
                            @NonNull Map<String, String> fields) {

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
    public Map<String, String> getRequestFields() {
        return requestFields;
    }

    public MultiValueMap<String, String> getRequestFieldsAsMultiValueMap() {
        LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        requestFields.forEach(result::add);
        return result;
    }
}
