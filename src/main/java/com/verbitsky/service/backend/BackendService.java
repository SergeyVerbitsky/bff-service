package com.verbitsky.service.backend;

import com.verbitsky.service.backend.response.UserSessionResponse;
import reactor.core.publisher.Mono;

public interface BackendService {
    Mono<UserSessionResponse> getUserSession(String userId);
}
