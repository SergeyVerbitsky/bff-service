package com.verbitsky.controller;

import com.verbitsky.model.BffLoginRequest;
import com.verbitsky.model.BffLoginResponse;
import com.verbitsky.service.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
//todo add exception handler
class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    Mono<BffLoginResponse> processLogin(@RequestBody BffLoginRequest loginRequest) {
        return userService.processLoginUser(loginRequest);
    }

//    @PostMapping("/token/introspect")
//    Mono<KeycloakIntrospectResponse> introspectToken(@RequestBody String token) {
//        return userService.introspectToken(token);
//    }
//
//    //todo move to another controller or rename current
//    @PostMapping("/userinfo")
//    Mono<KeycloakUserInfoResponse> getUserInfo(@RequestBody String userId) {
//        return userService.getUserInfoByToken(userId);
//    }

//    @PostMapping("/{userSub}/logout")
//    Mono<KeycloakLogoutResponse> processLogout(@PathVariable String userSub){
//        return loginService.processLogout(userSub);
//    }

    @GetMapping("/ping")
    public String processLogin() {
        return "ok";
    }
}
