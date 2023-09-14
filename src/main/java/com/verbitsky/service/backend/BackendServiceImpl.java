package com.verbitsky.service.backend;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.ApiRequest;
import com.verbitsky.api.client.CommonApiResponse;
import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.api.model.dto.SessionDto;
import com.verbitsky.exception.RemoteServiceException;
import com.verbitsky.service.backend.request.BackendRequestBuilder;
import com.verbitsky.service.backend.request.RequestType;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.verbitsky.service.backend.request.BackendGetRequest.USER_ID_FIELD;

@Slf4j
@Service
public class BackendServiceImpl implements BackendService {
    private final RemoteServiceClient backendClient;
    private final BackendRequestBuilder requestFactory;

    public BackendServiceImpl(RemoteServiceClient backendClient,
                              BackendRequestBuilder requestFactory) {
        this.backendClient = backendClient;
        this.requestFactory = requestFactory;
    }

    @Override
    public Mono<CommonApiResponse> getUserSession(String userId) {
        ApiRequest request = requestFactory.buildRequest(RequestType.USER_SESSION_OP, Map.of(USER_ID_FIELD, userId));
        return backendClient.get(
                request,
                CommonApiResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnSuccess(),
                getOnError());
    }

    @Override
    public Mono<CommonApiResponse> saveUserSession(SessionDto sessionDto) {
        var uriParams = Map.of(USER_ID_FIELD, sessionDto.getUserId());
        var bodyFields = sessionDto.fieldsToMap();
        var apiRequest = requestFactory.buildRequest(RequestType.USER_SESSION_OP, uriParams, bodyFields);

        return backendClient.post(
                apiRequest,
                CommonApiResponse.class,
                getStatusPredicate(),
                getStatusExceptionFunc(),
                getOnSuccess(),
                getOnError());
    }

    private Predicate<HttpStatusCode> getStatusPredicate() {
        return HttpStatusCode::isError;
    }

    private Function<ClientResponse, Mono<? extends Throwable>> getStatusExceptionFunc() {
        return response -> {
            log.error("received error response from backend service: {}", response.statusCode());
            return Mono.just(
                    new RemoteServiceException(response.statusCode().toString(), response.statusCode()));
        };
    }

    private Consumer<CommonApiResponse> getOnSuccess() {
        return response -> log.info("Received remote service response. Response object type: {}",
                response.getResponseObject().getClass());
    }

    private Consumer<? super Throwable> getOnError() {
        return throwable -> log.error("Remote service call error: error type: {}, error message: {}, ",
                throwable.getClass(), throwable.getMessage());
    }
}
