package com.verbitsky.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LoginResponseData;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
class AuthController {
    private static final String USER_ID_COOKIE = "userId";
    private static final String SESSION_ID_COOKIE = "sessionId";
    private static final String DEVICE_ID_COOKIE = "deviceId";
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    Mono<ResponseEntity<ApiResponse>> processLogin(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        String login = loginRequest.login();
        String deviceId = loginRequest.deviceId();
        String password = loginRequest.password();

        return userService
                .processLoginUser(login, password, deviceId)
                .map(apiResponse -> {
                    addLoginCookies(apiResponse, response);
                    return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
                });
    }

    @PostMapping("/logout")
    ResponseEntity<Void> processLogout(@Valid @RequestBody LogoutRequest logoutRequest, HttpServletResponse response) {
        String userId = logoutRequest.userId();
        String deviceId = logoutRequest.deviceId();
        String sessionId = logoutRequest.sessionId();

        userService.processUserLogout(userId, sessionId, deviceId);
        removeLoginCookies(response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    Mono<ResponseEntity<ApiResponse>> processRegistration(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.processUserRegistration(registerRequest)
                .map(apiResponse -> apiResponse.isErrorResponse()
                        ? ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse)
                        : ResponseEntity.status(apiResponse.getStatusCode()).build()
                );
    }

    @GetMapping("/ping")
    String ping() {
        return "ok";
    }

    private void addLoginCookies(ApiResponse serviceResponse, HttpServletResponse response) {
        if (!serviceResponse.isErrorResponse()) {
            LoginResponseData responseData = (LoginResponseData) serviceResponse.getResponseObject();

            response.addCookie(new Cookie(USER_ID_COOKIE, responseData.userId()));
            response.addCookie(new Cookie(SESSION_ID_COOKIE, responseData.sessionId()));
            response.addCookie(new Cookie(DEVICE_ID_COOKIE, responseData.userId()));
        }
    }

    private void removeLoginCookies(HttpServletResponse response) {

        Cookie userIdCookie = new Cookie(USER_ID_COOKIE, null);
        userIdCookie.setMaxAge(0);
        response.addCookie(userIdCookie);

        Cookie sessionIdCookie = new Cookie(SESSION_ID_COOKIE, null);
        sessionIdCookie.setMaxAge(0);
        response.addCookie(sessionIdCookie);

        Cookie deviceIdCookie = new Cookie(DEVICE_ID_COOKIE, null);
        deviceIdCookie.setMaxAge(0);
        response.addCookie(deviceIdCookie);
    }
}
