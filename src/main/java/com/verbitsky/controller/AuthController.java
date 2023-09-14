package com.verbitsky.controller;

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    Mono<BffLoginResponse> processLogin(@RequestBody BffLoginRequest loginRequest) {
        return userService.processLoginUser(loginRequest);
    }

    @GetMapping("/ping")
    public String processLogin() {
        return "ok";
    }
}
