package com.verbitsky.service.backend;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.api.client.response.CommonApiResponse;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.service.backend.request.BackendRequestBuilder;
import com.verbitsky.service.backend.request.RequestType;

import java.util.Map;

import static com.verbitsky.service.backend.request.BackendGetRequest.USER_ID_FIELD;

@Slf4j
@Service
public class BackendUserServiceImpl implements BackendUserService {
    private static final boolean EXTERNAL_SERVICE_FLAG = false;
    private final RemoteServiceClient backendClient;
    private final BackendRequestBuilder requestBuilder;

    public BackendUserServiceImpl(RemoteServiceClient backendClient,
                                  BackendRequestBuilder requestBuilder) {

        this.backendClient = backendClient;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public Mono<ApiResponse> getUserSession(String userId) {
        var apiRequest = requestBuilder.buildRequest(RequestType.USER_SESSION_OP, Map.of(USER_ID_FIELD, userId));
        return backendClient.get(apiRequest, SessionModel.class, CommonApiResponse.class, EXTERNAL_SERVICE_FLAG);
    }

    @Override
    public Mono<ApiResponse> saveUserSession(SessionModel sessionDto) {
        var uriParams = Map.of(USER_ID_FIELD, sessionDto.getUserId());
        var bodyFields = sessionDto.fieldsToMap();
        var apiRequest = requestBuilder.buildRequest(RequestType.USER_SESSION_OP, uriParams, bodyFields);

        return backendClient.post(apiRequest, SessionModel.class, CommonApiResponse.class, EXTERNAL_SERVICE_FLAG);
    }

    @Override
    public void invalidateUserSession(String userId) {
        var apiRequest = requestBuilder.buildRequest(RequestType.USER_SESSION_OP, Map.of(USER_ID_FIELD, userId));
        backendClient
                .delete(apiRequest, SessionModel.class, CommonApiResponse.class, EXTERNAL_SERVICE_FLAG)
                .subscribe();
    }
}
