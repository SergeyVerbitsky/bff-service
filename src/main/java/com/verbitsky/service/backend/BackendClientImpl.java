package com.verbitsky.service.backend;

import com.verbitsky.exception.RemoteServiceException;
import com.verbitsky.service.AbstractApiResponse;
import com.verbitsky.service.ApiRequest;
import com.verbitsky.service.RemoteServiceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Qualifier("backendClient")
class BackendClientImpl implements RemoteServiceClient {
    private final WebClient client;

    BackendClientImpl(WebClient client) {
        this.client = client;
    }

    @Override
    public <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> get(T request, Class<R> responseType) {
        return client
                .get()
                .uri(request.getRequestedUri())
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(objectResponseEntity -> {
                    //todo log it
                })
                .doOnError(err -> {
                    //log it
                });
    }

    @Override
    public <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> post(T request, Class<R> responseType) {
        throw new RemoteServiceException("this method is temporary not implemented");
    }
}
