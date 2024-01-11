package com.verbitsky.service.backend;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.service.backend.request.BackendRequestBuilder;

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
}
