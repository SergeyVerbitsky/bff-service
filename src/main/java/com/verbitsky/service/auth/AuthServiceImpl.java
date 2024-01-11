package com.verbitsky.service.auth;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.api.client.response.CommonApiError;
import com.verbitsky.api.client.response.CommonApiResponse;
import com.verbitsky.api.converter.EntityDtoConverterManager;
import com.verbitsky.api.converter.ServiceResponseConverterProvider;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.entity.SessionEntity;
import com.verbitsky.exception.AuthException;
import com.verbitsky.model.LoginResponseData;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.security.CustomOAuth2TokenAuthentication;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.security.TokenDataProvider;
import com.verbitsky.service.keycloak.KeycloakService;
import com.verbitsky.service.keycloak.response.KeycloakTokenResponse;
import com.verbitsky.storage.SessionModelRepo;

import java.util.Objects;

import static com.verbitsky.security.CustomOAuth2TokenAuthentication.authenticationFromUserDetails;
import static com.verbitsky.service.keycloak.request.KeycloakFields.KEYCLOAK_USER_ID;
import static com.verbitsky.service.keycloak.request.KeycloakFields.USER_SESSION_ID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KeycloakService keycloakService;
    private final EntityDtoConverterManager entityDtoConverterManager;
    private final ServiceResponseConverterProvider responseConverterProvider;
    private final TokenDataProvider tokenDataProvider;
    private final AuthenticationManager authenticationManager;
    private final SessionModelRepo sessionRepo;

    public AuthServiceImpl(KeycloakService keycloakService,
                           EntityDtoConverterManager entityDtoConverterManager,
                           ServiceResponseConverterProvider responseConverterProvider,
                           TokenDataProvider tokenDataProvider,
                           AuthenticationManager authenticationManager,
                           SessionModelRepo sessionRepo) {

        this.keycloakService = keycloakService;
        this.entityDtoConverterManager = entityDtoConverterManager;
        this.responseConverterProvider = responseConverterProvider;
        this.tokenDataProvider = tokenDataProvider;
        this.authenticationManager = authenticationManager;
        this.sessionRepo = sessionRepo;
    }

    @Override
    public Mono<ApiResponse> processLoginUser(String login, String password, String deviceId) {
        SessionEntity session = sessionRepo.findByLogin(login);

        if (session != null
                && session.getDeviceId().equals(deviceId)
                && !tokenDataProvider.isTokenExpired(session.getAccessToken())) {

            log.warn("Received suspicious login request (userName: {})", login);
            CommonApiError apiError = CommonApiError.of("User already logged in");
            return Mono.just(CommonApiResponse.of(apiError, HttpStatus.CONFLICT));
        }

        return keycloakService.processLogin(login, password)
                .doOnNext(this::authenticateUser)
                .doOnNext(response -> saveUserSession(response, deviceId))
                .map(response -> convertLoginResponse(response, deviceId));
    }

    @Override
    public void processUserLogout(String userId, String sessionId, String deviceId) {
        var session = sessionRepo.findBySessionId(sessionId);
        if (isSessionValid(session, userId, deviceId)) {
            sessionRepo.delete(session);
            keycloakService.processLogout(userId, sessionId)
                    .subscribe(this::logResponse);

        } else {
            throw new AuthException(UNAUTHORIZED, "User logout processing error: Invalid session data");
        }
    }

    @Override
    public Mono<ApiResponse> processUserRegistration(RegisterRequest registerRequest) {
        return keycloakService.processUserRegistration(registerRequest);
    }

    @Override
    public CustomOAuth2TokenAuthentication resolveAuthentication(String userId, String sessionId, String deviceId) {
        SessionEntity sessionEntity = sessionRepo.findBySessionId(sessionId);

        if (isSessionValid(sessionEntity, userId, deviceId)) {
            CustomUserDetails userDetails = responseConverterProvider
                    .provideConverter(SessionModel.class, CustomUserDetails.class)
                    .convert(entityDtoConverterManager.convertToDto(sessionEntity, SessionModel.class));
            String accessToken = userDetails.getAccessToken();
            if (tokenDataProvider.isTokenExpired(accessToken)) {
                throw new AuthException(UNAUTHORIZED, "User session has expired");
            } else {
                return authenticationFromUserDetails(userDetails);
            }
        } else {
            log.error("Received suspicious user request params: userId={}, sessionId={}", userId, sessionId);
            throw new AuthException(UNAUTHORIZED, "Invalid user session data");
        }
    }

    private void authenticateUser(ApiResponse response) {
        if (response.isErrorResponse()) {
            throw buildAuthException(response);
        }

        try {
            var loginResponse = (KeycloakTokenResponse) response.getResponseObject();
            var userDetails = buildUserDetails(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            authenticationManager.authenticate(authenticationFromUserDetails(userDetails));
        } catch (ClassCastException exception) {
            throw new AuthException(INTERNAL_SERVER_ERROR,
                    "User login processing error: " + exception.getMessage());
        }
    }

    private void logResponse(ApiResponse apiResponse) {
        if (apiResponse.isErrorResponse()) {
            log.error("Keycloak response error: message: {}, cause: {}",
                    apiResponse.getApiError().getErrorMessage(), apiResponse.getApiError().getCause());
        }
    }

    private ApiResponse convertLoginResponse(ApiResponse tokenResponse, String deviceId) {
        var responseObject = (KeycloakTokenResponse) tokenResponse.getResponseObject();
        String userId = tokenDataProvider
                .getTokenClaim(responseObject.getAccessToken(), KEYCLOAK_USER_ID)
                .orElseThrow(() -> new AuthException(INTERNAL_SERVER_ERROR,
                        "Received wrong token", "Can't get userId claim from token"));
        String sessionId = tokenDataProvider.getTokenClaim(responseObject.getAccessToken(), USER_SESSION_ID)
                .orElseThrow(() -> new AuthException(INTERNAL_SERVER_ERROR,
                        "Received wrong token", "Can't get sessionId claim from token"));

        return CommonApiResponse.of(new LoginResponseData(userId, sessionId, deviceId), HttpStatus.OK);
    }

    private CustomUserDetails buildUserDetails(String accessToken, String refreshToken) {
        var tokenParams = tokenDataProvider.getParametersFromToken(accessToken);
        var userAuthorities = tokenDataProvider.getGrantedAuthorities(accessToken);
        var jwt = tokenDataProvider.buildJwt(accessToken, tokenParams);

        return new CustomUserDetails(jwt, refreshToken, userAuthorities);
    }

    private boolean isSessionValid(SessionEntity sessionModel, String userId, String deviceId) {
        return Objects.nonNull(sessionModel)
                && sessionModel.getKeycloakId().equals(userId)
                && sessionModel.getDeviceId().equals(deviceId);
    }

    private void saveUserSession(ApiResponse tokenResponse, String deviceId) {
        if (Objects.isNull(tokenResponse) || tokenResponse.isErrorResponse()) {
            throw buildAuthException(tokenResponse);
        }

        var responseObject = (KeycloakTokenResponse) tokenResponse.getResponseObject();
        var accessToken = responseObject.getAccessToken();
        var refreshToken = responseObject.getRefreshToken();

        String keycloakId = tokenDataProvider.getKeycloakUserId(accessToken);
        String sessionId = tokenDataProvider.getSessionId(accessToken);
        String login = tokenDataProvider.getUserLogin(accessToken);

        SessionEntity userSession = sessionRepo.findByLogin(login);
        if (userSession != null && userSession.getDeviceId().equals(deviceId)) {
            userSession.setAccessToken(accessToken);
            userSession.setRefreshToken(refreshToken);
            userSession.setSessionId(sessionId);
            sessionRepo.save(userSession);
        } else {
            SessionModel sessionModel = SessionModel.of(keycloakId, sessionId, deviceId, login, accessToken, refreshToken);
            sessionRepo.save(entityDtoConverterManager.convertToEntity(sessionModel, SessionEntity.class));
        }
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
