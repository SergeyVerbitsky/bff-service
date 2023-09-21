package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public abstract class KeycloakAbstractResponse implements Serializable {
    private String errorType;
    private String errorDescription;
    @Serial
    private static final long serialVersionUID = 4534475632069679902L;
}
