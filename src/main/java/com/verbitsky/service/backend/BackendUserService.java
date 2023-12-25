package com.verbitsky.service.backend;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.api.model.SessionModel;

public interface BackendUserService {
    Mono<ApiResponse> getUserSession(String userId);

    Mono<ApiResponse> saveUserSession(SessionModel sessionDto);

    void invalidateUserSession(String userId);
}
