package com.verbitsky.service.backend.response;

import com.verbitsky.service.AbstractApiResponse;

public class UserSessionResponse extends AbstractApiResponse {
    private String sessionId;
    private String userId;

    private String accessToken;
    private String refreshToken;
}
