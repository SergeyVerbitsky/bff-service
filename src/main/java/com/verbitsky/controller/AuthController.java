package com.verbitsky.controller;

import jakarta.validation.Valid;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.model.BffLogoutRequest;
import com.verbitsky.model.BffRegisterRequest;
import com.verbitsky.model.BffRegisterResponse;
import com.verbitsky.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    ResponseEntity<Mono<BffLoginResponse>> processLogin(
            @Valid @RequestBody BffLoginRequest loginRequest) {

        return ResponseEntity.ok(userService.processLoginUser(loginRequest));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> processLogout(
            @Valid @RequestBody BffLogoutRequest logoutRequest) {
        userService.processUserLogout(logoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    ResponseEntity<Mono<BffRegisterResponse>> processRegistration(
            @Valid @RequestBody BffRegisterRequest registerRequest) {

        return ResponseEntity.ok(userService.processUserRegistration(registerRequest));
    }

    @GetMapping("/ping")
    String ping() {
        return "ok";
    }
}
