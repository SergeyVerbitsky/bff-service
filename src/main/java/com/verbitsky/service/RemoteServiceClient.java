package com.verbitsky.service;

import reactor.core.publisher.Mono;

public interface RemoteServiceClient {
    <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> get(T request, Class<R> responseType);

    <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> post(T request, Class<R> responseType);
}
