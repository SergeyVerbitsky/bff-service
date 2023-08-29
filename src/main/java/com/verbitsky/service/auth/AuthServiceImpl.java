package com.verbitsky.service.auth;

import com.google.common.cache.Cache;
import com.verbitsky.exception.AuthException;
import com.verbitsky.keycloak.KeycloakService;
import com.verbitsky.keycloak.response.KeycloakLoginResponse;
import com.verbitsky.model.ApiLoginRequest;
import com.verbitsky.model.ApiLoginResponse;
import com.verbitsky.model.ApiRegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.security.TokenDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.verbitsky.security.CustomOAuth2TokenAuthentication.authenticationFromUserDetails;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KeycloakService keycloakService;
    private final AtomicReference<Cache<String, CustomUserDetails>> userCache;
    private final TokenDataProvider tokenDataProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(@Qualifier("tokenCache") Cache<String, CustomUserDetails> tokenCache,
                           KeycloakService keycloakService, TokenDataProvider tokenDataProvider,
                           AuthenticationManager authenticationManager) {

        this.keycloakService = keycloakService;
        this.userCache = new AtomicReference<>(tokenCache);
        this.tokenDataProvider = tokenDataProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public CustomUserDetails getUserById(String id) {
        return null;
    }

    @Override
    public Mono<ApiLoginResponse> processLoginUser(ApiLoginRequest loginRequest) {
        return keycloakService
                .processLogin(loginRequest.userName(), loginRequest.password())
                .map(this::processKeycloakLoginResponse)
                .map(userDetails -> new ApiLoginResponse(userDetails.getUserId(), userDetails.getSessionId()));
    }

    @Override
    public void processUserLogout(String userId) {
        invalidateSession(userId);
        //todo remove from db
    }

    @Override
    public void processUserRegistration(ApiRegisterRequest registerRequest) {
        //todo implement this
    }

    @Override
    public CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId) {
        return authenticationFromUserDetails(getModelFromStorage(userId, sessionId));
    }

    private boolean isSessionIdValid(String storedSessionId, String receivedSessionId) {
        return StringUtils.isNotBlank(receivedSessionId) && receivedSessionId.equals(storedSessionId);
    }

    private CustomUserDetails getModelFromStorage(String userId, String sessionId) {
        Optional<CustomUserDetails> userFromCache = getUserFromCache(userId, sessionId);
        return userFromCache.orElseGet(() -> getUserFromDb(userId, sessionId));
    }

    private Optional<CustomUserDetails> getUserFromCache(String userId, String sessionId) {
        var userDetails = userCache.get().getIfPresent(userId);
        if (Objects.nonNull(userDetails) && isSessionIdValid(userDetails.getSessionId(), sessionId)) {

            if (tokenDataProvider.isTokenValid(userDetails.getJwt())) {
                return Optional.of(userDetails);
            }
            String refreshToken = userDetails.getRefreshToken();
            if (tokenDataProvider.isTokenValid(refreshToken)) {
                return Optional.of(processRefreshToken(refreshToken).block());

            }
        }

        return Optional.empty();
    }

    private CustomUserDetails getUserFromDb(String userId, String sessionId) {
        //todo add db search (get from backend app)
        log.warn("Requested session id ({}) of user ({}) wasn't found", sessionId, userId);
        throw new AuthException("get from db is not implemented");
    }

    private Mono<CustomUserDetails> processRefreshToken(String refreshToken) {
        if (tokenDataProvider.isTokenValid(refreshToken)) {
            return keycloakService.processRefreshToken(refreshToken)
                    .map(this::processKeycloakLoginResponse);
        }

        throw new AuthException("Couldn't process refresh token and ");
    }

    private CustomUserDetails processKeycloakLoginResponse(KeycloakLoginResponse response) {
        try {
            var userDetails = buildUserDetails(response.getAccessToken(), response.getRefreshToken());
            saveUserDetails(userDetails);
            authenticationManager.authenticate(authenticationFromUserDetails(userDetails));
            return userDetails;
        } catch (Exception e) {
            throw new AuthException("KeycloakLoginResponse processing error: " + e.getMessage());
        }
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
        //todo impl repo to save
    }

    private void invalidateSession(String userId) {
        var customUserDetails = userCache.get().getIfPresent(userId);
        if (Objects.nonNull(customUserDetails)) {
            userCache.get().invalidate(userId);
        }
        //todo remove from db (and keycloak?)
    }
}
