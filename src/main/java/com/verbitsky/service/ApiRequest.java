package com.verbitsky.service;

import org.springframework.util.MultiValueMap;

import java.net.URI;

public interface ApiRequest {
    URI getRequestedUri();

    MultiValueMap<String, String> getRequestHeaders();

    MultiValueMap<String, String> getRequestFields();
}
