package com.verbitsky.service.auth;

import com.google.common.cache.Cache;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.ApiResponse;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.converter.ConverterManager;
import com.verbitsky.exception.AuthException;
import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.model.BffRegisterRequest;
import com.verbitsky.model.BffRegisterResponse;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.security.TokenDataProvider;
import com.verbitsky.service.backend.BackendService;
import com.verbitsky.service.keycloak.KeycloakService;
import com.verbitsky.service.keycloak.response.KeycloakLoginResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.verbitsky.security.CustomOAuth2TokenAuthentication.authenticationFromUserDetails;
import static com.verbitsky.service.keycloak.request.KeycloakFields.EMAIL;
import static com.verbitsky.service.keycloak.request.KeycloakFields.ENABLE_USER;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_FIRST_NAME;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_LAST_NAME;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_NAME;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KeycloakService keycloakService;
    private final BackendService backendService;
    private final ConverterManager converterManager;
    private final AtomicReference<Cache<String, CustomUserDetails>> userCache;
    private final TokenDataProvider tokenDataProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(@Qualifier("tokenCache") Cache<String, CustomUserDetails> tokenCache,
                           KeycloakService keycloakService,
                           BackendService backendService,
                           ConverterManager converterManager, TokenDataProvider tokenDataProvider,
                           AuthenticationManager authenticationManager) {

        this.keycloakService = keycloakService;
        this.userCache = new AtomicReference<>(tokenCache);
        this.backendService = backendService;
        this.converterManager = converterManager;
        this.tokenDataProvider = tokenDataProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public CustomUserDetails getUserById(String id) {
        return null;
    }

    @Override
    public Mono<BffLoginResponse> processLoginUser(BffLoginRequest loginRequest) {

        return keycloakService
                .processLogin(loginRequest.userName(), loginRequest.password())
                .map(this::processApiLoginResponse)
                .map(this::mapToLoginResponse);
    }


    @Override
    public void processUserLogout(String userId) {
        invalidateSession(userId);
        //todo remove from db
    }

    @Override
    public Mono<BffRegisterResponse> processUserRegistration(BffRegisterRequest registerRequest) {
        //todo add fields validation
        return keycloakService
                .processUserRegistration(buildRequestFieldsMap(registerRequest))
                .map(apiResponse -> mapToUserRegisterResponse(registerRequest));
    }

    @Override
    public CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId) {
        return authenticationFromUserDetails(getModelFromStorage(userId, sessionId));
    }

    private boolean isSessionIdValid(CustomUserDetails userDetails, String receivedSessionId) {
        return Objects.nonNull(userDetails)
                && StringUtils.isNotBlank(receivedSessionId)
                && receivedSessionId.equals(userDetails.getSessionId());
    }

    private CustomUserDetails getModelFromStorage(String userId, String sessionId) {
        var userFromCache = getUserFromCache(userId, sessionId);
        return userFromCache
                .orElseGet(() -> getUserSessionFromDb(userId, sessionId));
    }

    private Optional<CustomUserDetails> getUserFromCache(String userId, String sessionId) {
        var userDetails = userCache.get().getIfPresent(userId);

        if (isSessionIdValid(userDetails, sessionId)) {
            if (tokenDataProvider.isTokenValid(userDetails.getJwt())) {
                return Optional.of(userDetails);
            }
            String refreshToken = userDetails.getRefreshToken();
            if (tokenDataProvider.isTokenValid(refreshToken)) {
                CustomUserDetails customUserDetails = processRefreshToken(refreshToken).block();
                return Optional.of(customUserDetails);
            }
        }

        return Optional.empty();
    }

    private CustomUserDetails getUserSessionFromDb(String userId, String sessionId) {
        try {
            return backendService.getUserSession(userId)
                    .map(this::convertUserSessionResponse)
                    .block();
        } catch (Exception exception) {
            log.warn("Requested session id ({}) of user ({}) wasn't found: {}",
                    sessionId, userId, exception.getMessage());
            throw new AuthException(HttpStatus.FORBIDDEN, "Authentication error", exception);
        }
    }

    private CustomUserDetails convertUserSessionResponse(ApiResponse apiResponse) {
        if (apiResponse.isErrorResponse()) {
            throw new ServiceException(apiResponse.getApiError().toString());
        }
        var response = (SessionModel) apiResponse.getResponseObject();
        var converter = converterManager.provideConverter(SessionModel.class, CustomUserDetails.class);
        return converter.convert(response);
    }

    private Mono<CustomUserDetails> processRefreshToken(String refreshToken) {
        if (tokenDataProvider.isTokenValid(refreshToken)) {
            return keycloakService.processRefreshToken(refreshToken)
                    .map(this::processApiLoginResponse);
        }

        throw new AuthException(INTERNAL_SERVER_ERROR, "Refresh token processing error");
    }

    private CustomUserDetails processApiLoginResponse(ApiResponse response) {
        if (response.isErrorResponse()) {
            var errorMessage = response.getApiError().getErrorMessage();
            var cause = response.getApiError().getCause();
            var httpStatus = response.getApiError().getHttpStatusCode();

            throw new AuthException(
                    httpStatus, "User login processing error: " + errorMessage, cause);
        }

        try {
            KeycloakLoginResponse loginResponse = (KeycloakLoginResponse) response.getResponseObject();
            var userDetails = buildUserDetails(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            saveUserDetails(userDetails);
            authenticationManager.authenticate(authenticationFromUserDetails(userDetails));
            return userDetails;
        } catch (ParseException | IOException | ClassCastException exception) {
            throw new AuthException(INTERNAL_SERVER_ERROR,
                    "User login processing error: " + exception.getMessage());
        }
    }

    private BffRegisterResponse mapToUserRegisterResponse(BffRegisterRequest registerRequest) {
        return new BffRegisterResponse(registerRequest.userName());
    }

    private BffLoginResponse mapToLoginResponse(CustomUserDetails userDetails) {
        return new BffLoginResponse(userDetails.getUserId(), userDetails.getSessionId());
    }

    private CustomUserDetails buildUserDetails(String accessToken, String refreshToken)
            throws ParseException, IOException {

        var tokenParams = tokenDataProvider.getParametersFromToken(accessToken);
        var userAuthorities = tokenDataProvider.getGrantedAuthorities(accessToken);
        var jwt = tokenDataProvider.buildJwt(accessToken, tokenParams);

        return new CustomUserDetails(refreshToken, jwt, userAuthorities);
    }

    private void saveUserDetails(CustomUserDetails userDetails) {
        userCache.get().put(userDetails.getUserId(), userDetails);
        backendService.saveUserSession(buildSessionDto(userDetails)).subscribe()
        ;
    }

    private void invalidateSession(String userId) {
        var customUserDetails = userCache.get().getIfPresent(userId);
        if (Objects.nonNull(customUserDetails)) {
            userCache.get().invalidate(userId);
        }
        //todo remove from db (and keycloak?)
    }

    private Map<String, String> buildRequestFieldsMap(BffRegisterRequest registerRequest) {
        return Map.of(
                ENABLE_USER, Boolean.toString(true),
                USER_NAME, registerRequest.userName(),
                EMAIL, registerRequest.email(),
                USER_FIRST_NAME, registerRequest.firstName(),
                USER_LAST_NAME, registerRequest.lastName()
        );
    }

    private SessionModel buildSessionDto(CustomUserDetails userDetails) {
        var sessionDto = new SessionModel();
        sessionDto.setAccessToken(userDetails.getAccessToken());
        sessionDto.setRefreshToken(userDetails.getRefreshToken());
        sessionDto.setUserId(userDetails.getUserId());

        return sessionDto;
    }
}
