package com.verbitsky.service.keycloak.client;

import com.verbitsky.service.AbstractApiResponse;
import com.verbitsky.service.ApiRequest;
import com.verbitsky.service.RemoteServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Qualifier("keycloakClient")
public class KeycloakClient implements RemoteServiceClient {
    private final WebClient webClient;

    public KeycloakClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> get(T request, Class<R> responseType) {
        return webClient
                .get()
                .uri(request.getRequestedUri())
                .headers(headers -> headers.addAll(request.getRequestHeaders()))
                .retrieve()
//                .onStatus(HttpStatusCode::isError, errorProcessingFunction(request.getAction()))
                .bodyToMono(responseType)
//                .doOnSuccess(response -> log.info(
//                        "Processed successfully: Action: {}, headers: {}",
//                        request.getAction(), request.getHeaders()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public <T extends ApiRequest, R extends AbstractApiResponse> Mono<R> post(T request, Class<R> responseType) {
        return webClient
                .post()
                .uri(request.getRequestedUri())
                .body(Mono.just(request.getRequestFields()), MultiValueMap.class)
                .headers(headers -> headers.addAll(request.getRequestHeaders()))
                .retrieve()
//                .onStatus(HttpStatusCode::isError, errorProcessingFunction(request.getAction()))
                .bodyToMono(responseType)
//                .doOnSuccess(response -> log.info(
                // TODO: 16.05.2023 hide secret values and password
//                        "Processed successfully: Action: {}, fields: {}, headers: {}",
//                        request.getAction(), request.getRequestFields(), request.getHeaders()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

//    private Function<ClientResponse, Mono<? extends Throwable>> errorProcessingFunction(KeycloakAction action) {
//        return clientResponse -> clientResponse.body((inputMessage, context) ->
//                clientResponse.bodyToMono(String.class)
//                        .map(JsonUtil::extractErrorDescription)
//                        .map(body -> new InvalidKeycloakRequestException(
//                                inputMessage.getStatusCode(), action, body)));
//    }
}
