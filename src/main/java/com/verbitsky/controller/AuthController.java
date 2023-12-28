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

import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LoginResponse;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.model.RegisterResponse;
import com.verbitsky.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    ResponseEntity<Mono<LoginResponse>> processLogin(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.processLoginUser(loginRequest));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> processLogout(@Valid @RequestBody LogoutRequest logoutRequest) {
        userService.processUserLogout(logoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    ResponseEntity<Mono<RegisterResponse>> processRegistration(@Valid @RequestBody RegisterRequest registerRequest) {

        return ResponseEntity.ok(userService.processUserRegistration(registerRequest));
    }

    @PostMapping("/keycloak/logout")
    ResponseEntity<Void> completeUserLogout() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/ping")
    String ping() {
        return "ok";
    }
}
