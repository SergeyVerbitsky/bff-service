package com.verbitsky.keycloak.request;

import lombok.Getter;
import lombok.Setter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.verbitsky.keycloak.client.KeycloakAction;

@Getter
@Setter
public class KeycloakRequest {

    private String endpointUrl;
    private MultiValueMap<String, String> headers;
    private MultiValueMap<String, String> requestFields;
    private KeycloakAction action;

    public KeycloakRequest() {
        this.requestFields = new LinkedMultiValueMap<>();
        this.headers = new LinkedMultiValueMap<>();
    }
}
