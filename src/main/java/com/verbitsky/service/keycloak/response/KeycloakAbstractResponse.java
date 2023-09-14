package com.verbitsky.service.keycloak.response;

import com.verbitsky.api.client.ApiResponse;
import com.verbitsky.api.error.ApiError;
import com.verbitsky.api.model.dto.ApiModel;

import java.io.Serial;

public abstract class KeycloakAbstractResponse implements ApiResponse {
    @Serial
    private static final long serialVersionUID = 4534475632069679902L;

    @Override
    public ApiError getApiError() {
        return null;
    }

    @Override
    public ApiModel getResponseObject() {
        return null;
    }
    //todo transofrm to interface add response code processing
    // (добавить в респонс поля с ошибками error и errorDescription)
}
