package com.verbitsky.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verbitsky.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.keycloak.response.KeycloakLogoutResponse;
import com.verbitsky.keycloak.response.KeycloakTokenResponse;
import com.verbitsky.keycloak.response.KeycloakUserInfoResponse;
import com.verbitsky.model.ApiLoginRequest;
import com.verbitsky.service.auth.AuthServiceImpl;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
class LoginController {

    @Autowired
    AuthServiceImpl loginService;

    @PostMapping("/login")
    Mono<KeycloakTokenResponse> processLogin(@RequestBody ApiLoginRequest loginRequest) {
        return loginService.processLogin(loginRequest);
    }

    @PostMapping("/introspect")
    Mono<KeycloakIntrospectResponse> introspectToken(@RequestBody String token) {
        return loginService.introspectToken(token);
    }

    //todo move to another controller or rename current
    @PostMapping("/userinfo")
    Mono<KeycloakUserInfoResponse> getUserInfo(@RequestBody String token) {
        return loginService.getUserInfo(token);
    }

    @PostMapping("/{userSub}/logout")
    Mono<KeycloakLogoutResponse> processLogout(@PathVariable String userSub){
        return loginService.processLogout(userSub);
    }

    @GetMapping("/test")
    public String processLogin() {
        return "ok";
    }
}
