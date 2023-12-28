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

import com.verbitsky.api.client.response.ApiResponse;
import com.verbitsky.model.LoginRequest;
import com.verbitsky.model.LogoutRequest;
import com.verbitsky.model.RegisterRequest;
import com.verbitsky.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    ResponseEntity<Mono<ApiResponse>> processLogin(@Valid @RequestBody LoginRequest loginRequest) {
        Mono<ApiResponse> response = userService.processLoginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> processLogout(@Valid @RequestBody LogoutRequest logoutRequest) {
        userService.processUserLogout(logoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    ResponseEntity<Mono<ApiResponse>> processRegistration(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(userService.processUserRegistration(registerRequest));
    }

    @GetMapping("/ping")
    String ping() {
        return "ok";
    }
}
