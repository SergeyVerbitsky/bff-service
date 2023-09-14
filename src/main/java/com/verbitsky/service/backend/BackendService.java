package com.verbitsky.service.backend;

import reactor.core.publisher.Mono;

import com.verbitsky.api.client.CommonApiResponse;
import com.verbitsky.api.model.dto.SessionDto;

public interface BackendService {
    Mono<CommonApiResponse> getUserSession(String userId);

    Mono<CommonApiResponse> saveUserSession(SessionDto sessionDto);
}
