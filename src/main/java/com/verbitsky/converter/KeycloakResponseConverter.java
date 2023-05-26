package com.verbitsky.converter;

import com.verbitsky.keycloak.response.KeycloakAbstractResponse;
import com.verbitsky.model.BaseModel;

import java.util.Map;

public interface KeycloakResponseConverter<T extends KeycloakAbstractResponse, R extends BaseModel> {
    R convert (T response, Map<String, String> params);
}