package com.verbitsky.keycloak.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

import com.verbitsky.exception.InvalidKeycloakRequest;
import com.verbitsky.keycloak.request.KeycloakRequest;
import com.verbitsky.keycloak.response.KeycloakAbstractResponse;
import com.verbitsky.util.JsonUtil;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KeycloakClient {
    private final WebClient webClient;

    public KeycloakClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public <R extends KeycloakAbstractResponse> Mono<R> post(KeycloakRequest request, Class<R> responseClass) {
        return webClient
                .post()
                .uri(request.getEndpointUrl())
                .body(Mono.just(request.getRequestFields()), MultiValueMap.class)
                .headers(headers -> headers.addAll(request.getHeaders()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorProcessingFunction(request.getAction()))
                .bodyToMono(responseClass)
//                .doOnSuccess(response -> log.info(
//                        // TODO: 16.05.2023 hide secret values and password
//                        "Processed successfully: Action: {}, fields: {}, headers: {}",
//                        request.getAction(), request.getRequestFields(), request.getHeaders()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    public <R extends KeycloakAbstractResponse> Mono<R> get(KeycloakRequest request, Class<R> responseClass) {
        return webClient
                .get()
                .uri(request.getEndpointUrl())
                .headers(headers -> headers.addAll(request.getHeaders()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorProcessingFunction(request.getAction()))
                .bodyToMono(responseClass)
//                .doOnSuccess(response -> log.info(
//                        "Processed successfully: Action: {}, headers: {}",
//                        request.getAction(), request.getHeaders()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> errorProcessingFunction(KeycloakAction action) {
        return clientResponse -> clientResponse.body((inputMessage, context) ->
                clientResponse.bodyToMono(String.class)
                        .map(JsonUtil::extractErrorDescription)
                        .map(body -> new InvalidKeycloakRequest(
                                inputMessage.getStatusCode(), action, body)));
    }
}
