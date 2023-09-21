package com.verbitsky.service.keycloak;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.ApiResponse;
import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.property.KeycloakPropertyProvider;
import com.verbitsky.service.keycloak.client.KeycloakAction;
import com.verbitsky.service.keycloak.exception.InvalidKeycloakRequestException;
import com.verbitsky.service.keycloak.request.KeycloakRequestFactory;
import com.verbitsky.service.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.service.keycloak.response.KeycloakUserRegisterResponse;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@Lazy(value = false)
class KeycloakServiceImpl implements KeycloakService, KeycloakScheduledService {
    private final RemoteServiceClient keycloakClient;
    private final KeycloakRequestFactory requestFactory;
    private final KeycloakPropertyProvider propertyProvider;
    private final AtomicReference<String> adminAccessToken;
    private final AtomicReference<String> adminRefreshToken;

    KeycloakServiceImpl(RemoteServiceClient keycloakClient, KeycloakRequestFactory requestBuilder,
                        KeycloakPropertyProvider propertyProvider) {

        this.keycloakClient = keycloakClient;
        this.requestFactory = requestBuilder;
        this.propertyProvider = propertyProvider;
        adminAccessToken = new AtomicReference<>(StringUtils.EMPTY);
        adminRefreshToken = new AtomicReference<>(StringUtils.EMPTY);
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Keycloak service initialization");
        initAdminTokenUpdate();
    }

    @Override
    public Mono<ApiResponse> processLogin(String userName, String password) {
        var request = requestFactory.buildLoginRequest(userName, password);
        return keycloakClient.post(request, KeycloakLoginResponse.class);
    }

    @Override
    public Mono<ApiResponse> processUserRegistration(Map<String, String> regData) {
        var request = requestFactory.buildUserRegistrationRequest(regData, adminAccessToken.getAcquire());
        return keycloakClient.post(request, KeycloakUserRegisterResponse.class);
    }

    @Override
    public Mono<ApiResponse> processRefreshToken(String token) {
        var request = requestFactory.buildRefreshTokenRequest(token);
        return keycloakClient
                .post(request, KeycloakLoginResponse.class);
    }

    /*
     * Updates admin token 1 minute after the application starts and then repeat every 25 minutes.
     */
    @Async
    @Scheduled(cron = "0 */10 * * * ?")
    @Override
    public void initAdminTokenUpdate() {
        log.info("Admin token update process initiated");

        if (StringUtils.isBlank(adminAccessToken.getAcquire())) {
            doUpdateAdminTokens();
        } else {
            introspectToken(adminAccessToken.get())
                    .doOnSuccess(this::processTokenIntrospection)
                    .subscribe();
        }

        log.info("Admin token update finished");
    }

    private Mono<ApiResponse> introspectToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new InvalidKeycloakRequestException(
                    HttpStatus.BAD_REQUEST, KeycloakAction.LOGIN, "Null or empty token");
        }
        var request = requestFactory.buildTokenIntrospectionRequest(token);

        return keycloakClient
                .post(request, ApiResponse.class);
    }

    private void processTokenIntrospection(ApiResponse introspectResponse) {
        if (introspectResponse.isErrorResponse()) {
            log.error("Error during admin token introspection: {}", introspectResponse.getApiError().toString());
        } else {
            var response = (KeycloakIntrospectResponse) introspectResponse.getResponseObject();
            if (!response.isTokenActive()) {
                processRefreshToken(adminRefreshToken.getAcquire())
                        .doOnSuccess(this::updateAminTokenValues)
                        .subscribe();
            }
        }
    }

    private void doUpdateAdminTokens() {
        log.info("Starting admin token update process");

        var adminUserName = propertyProvider.provideAdminUserName();
        var adminUserPassword = propertyProvider.provideAdminUserPassword();
        processLogin(adminUserName, adminUserPassword).subscribe(this::updateAminTokenValues);

        log.info("Successfully updated admin token");
    }

    private void updateAminTokenValues(ApiResponse loginResponse) {
        if (loginResponse.isErrorResponse()) {
            log.error("Error during admin user login: {}", loginResponse.getApiError().toString());
        } else {
            var response = (KeycloakLoginResponse) loginResponse.getResponseObject();
            this.adminAccessToken.getAndSet(response.getAccessToken());
            this.adminRefreshToken.getAndSet(response.getRefreshToken());
        }
    }
}