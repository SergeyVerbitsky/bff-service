package com.verbitsky.service.backend;

import com.verbitsky.service.RemoteApiRequest;
import com.verbitsky.service.RemoteServiceClient;
import com.verbitsky.service.backend.request.BackendRequestFactory;
import com.verbitsky.service.backend.request.RequestType;
import com.verbitsky.service.backend.response.UserSessionResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static com.verbitsky.service.backend.request.BackendGetRequest.USER_ID_FIELD;

@Service
public class BackendServiceImpl implements BackendService {
    private final RemoteServiceClient backendClient;
    private final BackendRequestFactory requestFactory;

    public BackendServiceImpl(@Qualifier("backendClient") RemoteServiceClient backendClient,
                              BackendRequestFactory requestFactory) {
        this.backendClient = backendClient;
        this.requestFactory = requestFactory;
    }

    @Override
    public Mono<UserSessionResponse> getUserSession(String userId) {
        URI uri = requestFactory.buildUri(RequestType.GET_USER_SESSION, Map.of(USER_ID_FIELD, userId));
        return backendClient.get(new RemoteApiRequest(uri), UserSessionResponse.class);
    }
}
