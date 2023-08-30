package com.verbitsky.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;


@Getter
@Setter
public class RemoteApiRequest implements ApiRequest {
    private URI requestedUri;
    private MultiValueMap<String, String> requestHeaders;
    private MultiValueMap<String, String> requestFields;

    public RemoteApiRequest(URI requestedUri) {
        this.requestedUri = requestedUri;
        this.requestFields = new LinkedMultiValueMap<>();
        this.requestHeaders = new LinkedMultiValueMap<>();
    }
}
