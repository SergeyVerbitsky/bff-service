package com.verbitsky.service.auth;

import com.google.common.cache.Cache;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.api.client.response.CommonApiResponse;
import com.verbitsky.api.converter.ServiceResponseConverterManager;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.exception.AuthException;
import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LoginResponseData;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.security.TokenDataProvider;
import com.verbitsky.service.backend.BackendUserService;
import com.verbitsky.service.keycloak.KeycloakService;
import com.verbitsky.service.keycloak.response.KeycloakTokenResponse;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.verbitsky.security.CustomOAuth2TokenAuthentication.authenticationFromUserDetails;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_ID;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_SESSION_ID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KeycloakService keycloakService;
    private final BackendUserService backendUserService;
    private final ServiceResponseConverterManager converterManager;
    private final AtomicReference<Cache<String, CustomUserDetails>> userCache;
    private final TokenDataProvider tokenDataProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(@Qualifier("tokenCache") Cache<String, CustomUserDetails> tokenCache,
                           KeycloakService keycloakService,
                           BackendUserService backendUserService,
                           ServiceResponseConverterManager converterManager,
                           TokenDataProvider tokenDataProvider,
                           AuthenticationManager authenticationManager) {

        this.keycloakService = keycloakService;
        this.userCache = new AtomicReference<>(tokenCache);
        this.backendUserService = backendUserService;
        this.converterManager = converterManager;
        this.tokenDataProvider = tokenDataProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<ApiResponse> processLoginUser(LoginRequest loginRequest) {
        return keycloakService
                .processLogin(loginRequest.userName(), loginRequest.password())
                .map(this::convertLoginResponse)
                .doOnNext(this::saveUserSession);
    }

    @Override
    public void processUserLogout(LogoutRequest logoutRequest) {
        String userId = logoutRequest.userId();
        var modelFromStorage = getDetailsFromStorage(userId);
        if (isSessionIdValid(modelFromStorage, logoutRequest.sessionId())) {
            keycloakService.processLogout(userId)
                    .subscribe(this::processKeycloakUserLogoutResponse);
            invalidateSession(modelFromStorage.getUserId());
        } else {
            log.warn("Received suspicious user logout request params: userId={}, sessionId={}",
                    userId, logoutRequest.sessionId());
        }
    }

    @Override
    public Mono<ApiResponse> processUserRegistration(RegisterRequest registerRequest) {
        return keycloakService.processUserRegistration(registerRequest);
    }

    @Override
    public CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId) {
        CustomUserDetails userDetails;
        try {
            userDetails = getDetailsFromStorage(userId);
        } catch (AuthException exception) {
            log.warn("Suspicious actions during authentication resolving process: userId={}, sessionId={}",
                    userId, sessionId);
            throw new AuthException(BAD_REQUEST, "Invalid user session");
        } catch (Exception exception) {
            log.warn("Unexpected exception: {}", exception.getMessage());
            throw new ServiceException(INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        if (isSessionIdValid(userDetails, sessionId)) {
            String accessToken = userDetails.getAccessToken();
            if (tokenDataProvider.isTokenExpired(accessToken)) {
                String refreshToken = userDetails.getRefreshToken();
                if (tokenDataProvider.isTokenExpired(refreshToken)) {
                    throw buildAuthException(
                            CommonApiResponse.of("User session has expired", UNAUTHORIZED));
                }

                return exchangeRefreshToken(userDetails.getRefreshToken())
                        .map(CustomOAuth2TokenAuthentication::authenticationFromUserDetails)
                        .block();
            } else {
                return authenticationFromUserDetails(userDetails);
            }
        } else {
            log.error("Received suspicious user request params: userId={}, sessionId={}", userId, sessionId);
            throw new AuthException(BAD_REQUEST, "Invalid user session");
        }
    }

    public void invalidateSession(String userId) {
        userCache.getAcquire().invalidate(userId);
        backendUserService.invalidateUserSession(userId);
    }

    private CustomUserDetails processTokenResponse(ApiResponse response) {
        if (response.isErrorResponse()) {
            throw buildAuthException(response);
        }

        try {
            saveUserSession(response);
            var loginResponse = (KeycloakTokenResponse) response.getResponseObject();
            var userDetails = buildUserDetails(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            authenticationManager.authenticate(authenticationFromUserDetails(userDetails));
            return userDetails;
        } catch (ClassCastException exception) {
            throw new AuthException(INTERNAL_SERVER_ERROR,
                    "User login processing error: " + exception.getMessage());
        }
    }

    private void processKeycloakUserLogoutResponse(ApiResponse apiResponse) {
        if (apiResponse.isErrorResponse()) {
            log.error("Keycloak response error: message: {}, cause: {}",
                    apiResponse.getApiError().getErrorMessage(), apiResponse.getApiError().getCause());
        }
    }

    private Mono<CustomUserDetails> exchangeRefreshToken(String refreshToken) {
        if (!tokenDataProvider.isTokenExpired(refreshToken)) {
            return keycloakService.exchangeRefreshToken(refreshToken)
                    .map(this::processTokenResponse);
        }

        throw new AuthException(INTERNAL_SERVER_ERROR, "Refresh token processing error");
    }

    private CustomUserDetails getDetailsFromStorage(String userId) {
        var userFromCache = userCache.getAcquire().getIfPresent(userId);
        return Objects.nonNull(userFromCache) ? userFromCache : getUserSessionFromDb(userId);
    }

    private CustomUserDetails getUserSessionFromDb(String userId) {
        ApiResponse apiResponse = backendUserService.getUserSession(userId).block();
        if (Objects.isNull(apiResponse) || apiResponse.isErrorResponse()) {
            throw buildAuthException(apiResponse);
        }

        return convertUserSessionResponse(apiResponse);
    }

    private ApiResponse convertLoginResponse(ApiResponse tokenResponse) {
        var responseObject = (KeycloakTokenResponse) tokenResponse.getResponseObject();
        String userId = tokenDataProvider
                .getTokenClaim(responseObject.getAccessToken(), USER_ID)
                .orElseThrow(() -> new AuthException(INTERNAL_SERVER_ERROR,
                        "Received wrong token", "Can't get userId claim from token"));
        String sessionId = tokenDataProvider.getTokenClaim(responseObject.getAccessToken(), USER_SESSION_ID)
                .orElseThrow(() -> new AuthException(INTERNAL_SERVER_ERROR,
                        "Received wrong token", "Can't get sessionId claim from token"));

        return CommonApiResponse.of(new LoginResponseData(userId, sessionId), HttpStatus.OK);
    }

    private CustomUserDetails convertUserSessionResponse(ApiResponse apiResponse) {
        if (apiResponse.isErrorResponse()) {
            throw new ServiceException(apiResponse.getApiError().toString());
        }
        var response = (SessionModel) apiResponse.getResponseObject();
        var converter = converterManager.provideConverter(SessionModel.class, CustomUserDetails.class);
        return converter.convert(response);
    }

    private CustomUserDetails buildUserDetails(String accessToken, String refreshToken) {
        var tokenParams = tokenDataProvider.getParametersFromToken(accessToken);
        var userAuthorities = tokenDataProvider.getGrantedAuthorities(accessToken);
        var jwt = tokenDataProvider.buildJwt(accessToken, tokenParams);

        return new CustomUserDetails(jwt, refreshToken, userAuthorities);
    }


    private void saveUserSession(ApiResponse tokenResponse) {
        if (Objects.isNull(tokenResponse) || tokenResponse.isErrorResponse()) {
            throw buildAuthException(tokenResponse);
        }

        var responseObject = (KeycloakTokenResponse) tokenResponse.getResponseObject();
        var accessToken = responseObject.getAccessToken();
        var refreshToken = responseObject.getRefreshToken();
        String userId = tokenDataProvider
                .getParametersFromToken(accessToken)
                .getClaims()
                .getClaim(USER_ID);

        backendUserService.saveUserSession(SessionModel.of(userId, accessToken, refreshToken)).subscribe();
    }

    private boolean isSessionIdValid(CustomUserDetails userDetails, String receivedSessionId) {
        return Objects.nonNull(userDetails)
                && StringUtils.isNotBlank(receivedSessionId)
                && receivedSessionId.equals(userDetails.getSessionId());
    }

    private AuthException buildAuthException(ApiResponse apiResponse) {
        if (Objects.isNull(apiResponse)) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "Received null apiResponse from remote service");
        }
        var errorMessage = apiResponse.getApiError().getErrorMessage();
        var cause = apiResponse.getApiError().getCause();
        var httpStatus = apiResponse.getStatusCode();

        return new AuthException(httpStatus, errorMessage, cause);
    }
}
