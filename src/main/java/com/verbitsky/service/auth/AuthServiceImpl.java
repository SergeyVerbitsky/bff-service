package com.verbitsky.service.auth;

import com.google.common.cache.Cache;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Qualifier;
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
import com.verbitsky.model.BffLogoutRequest;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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
    public Mono<BffLoginResponse> processLoginUser(BffLoginRequest loginRequest) {
        return keycloakService
                .processLogin(loginRequest.userName(), loginRequest.password())
                .map(this::processApiLoginResponse)
                .map(this::mapToLoginResponse);
    }

    @Override
    public void processUserLogout(BffLogoutRequest logoutRequest) {
        var modelFromStorage = getModelFromStorage(logoutRequest.userId());
        if (modelFromStorage.isPresent()) {
            CustomUserDetails userDetails = modelFromStorage.get();
            if (isSessionIdValid(userDetails, logoutRequest.sessionId())) {
                invalidateSession(userDetails.getUserId());
            }
        }
    }

    @Override
    public Mono<BffRegisterResponse> processUserRegistration(BffRegisterRequest registerRequest) {
        return keycloakService
                .processUserRegistration(buildRequestFieldsMap(registerRequest))
                .map(apiResponse -> processApiRegisterResponse(apiResponse, registerRequest));
    }

    @Override
    public CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId) {
        var modelFromStorage = getModelFromStorage(userId);
        if (modelFromStorage.isPresent()) {
            CustomUserDetails userDetails = modelFromStorage.get();
            if (isSessionIdValid(userDetails, sessionId)) {
                String accessToken = userDetails.getAccessToken();
                if (tokenDataProvider.isTokenExpired(accessToken)) {
                    CustomUserDetails updatedModel = processRefreshToken(userDetails.getRefreshToken()).block();
                    return authenticationFromUserDetails(updatedModel);
                } else {
                    return authenticationFromUserDetails(userDetails);
                }
            } else {
                throw new AuthException(BAD_REQUEST, "Invalid userId or sessionId");
            }
        }

        throw new AuthException(FORBIDDEN, "User session has expired");
    }

    private CustomUserDetails processApiLoginResponse(ApiResponse response) {
        if (response.isErrorResponse()) {
            throwAuthException(response, AuthErrorType.LOGIN);
        }

        try {
            var loginResponse = (KeycloakLoginResponse) response.getResponseObject();
            var userDetails = buildUserDetails(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            saveUserDetails(userDetails);
            authenticationManager.authenticate(authenticationFromUserDetails(userDetails));
            return userDetails;
        } catch (ParseException | IOException | ClassCastException exception) {
            throw new AuthException(INTERNAL_SERVER_ERROR,
                    "User login processing error: " + exception.getMessage());
        }
    }

    private BffLoginResponse mapToLoginResponse(CustomUserDetails userDetails) {
        return new BffLoginResponse(userDetails.getUserId(), userDetails.getSessionId());
    }

    private BffRegisterResponse processApiRegisterResponse(ApiResponse response, BffRegisterRequest request) {
        if (response.isErrorResponse()) {
            throwAuthException(response, AuthErrorType.REGISTRATION);
        }

        return new BffRegisterResponse(request.userName());
    }

    private Mono<CustomUserDetails> processRefreshToken(String refreshToken) {
        if (!tokenDataProvider.isTokenExpired(refreshToken)) {
            return keycloakService.processRefreshToken(refreshToken)
                    .map(this::processApiLoginResponse);
        }

        throw new AuthException(INTERNAL_SERVER_ERROR, "Refresh token processing error");
    }

    private Optional<CustomUserDetails> getModelFromStorage(String userId) {
        var userFromCache = userCache.getAcquire().getIfPresent(userId);
        return Objects.nonNull(userFromCache) ? Optional.of(userFromCache) : getUserSessionFromDb(userId);
    }

    private Optional<CustomUserDetails> getUserSessionFromDb(String userId) {
        try {
            var customUserDetails = backendService.getUserSession(userId)
                    .map(this::convertUserSessionResponse)
                    .block();
            return Optional.of(customUserDetails);
        } catch (Exception exception) {
            return Optional.empty();
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

    private CustomUserDetails buildUserDetails(String accessToken, String refreshToken)
            throws ParseException, IOException {

        var tokenParams = tokenDataProvider.getParametersFromToken(accessToken);
        var userAuthorities = tokenDataProvider.getGrantedAuthorities(accessToken);
        var jwt = tokenDataProvider.buildJwt(accessToken, tokenParams);

        return new CustomUserDetails(refreshToken, jwt, userAuthorities);
    }

    private void saveUserDetails(CustomUserDetails userDetails) {
        userCache.getAcquire().put(userDetails.getUserId(), userDetails);
        backendService.saveUserSession(buildSessionDto(userDetails)).subscribe();
    }

    private void invalidateSession(String userId) {
        userCache.getAcquire().invalidate(userId);
        backendService.invalidateUserSession(userId);
    }

    private boolean isSessionIdValid(CustomUserDetails userDetails, String receivedSessionId) {
        return Objects.nonNull(userDetails)
                && StringUtils.isNotBlank(receivedSessionId)
                && receivedSessionId.equals(userDetails.getSessionId());
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

    private void throwAuthException(ApiResponse response, AuthErrorType authErrorType) {
        var errorMessage = response.getApiError().getErrorMessage();
        var cause = response.getApiError().getCause();
        var httpStatus = response.getApiError().getHttpStatusCode();

        throw new AuthException(
                httpStatus, String.format(authErrorType.getErrorMessage(), errorMessage), cause);
    }
}
