package com.verbitsky.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verbitsky.security.annotation.AdminAccess;
import com.verbitsky.security.annotation.AnyAuthorizedUser;
import com.verbitsky.security.annotation.UserAccess;

@RestController
@RequestMapping("/test_roles")
public class TestEndpointsController {

    @GetMapping("/admin")
    @AdminAccess
    public String testRoleAdmin() {
        return "ok_admin";
    }

    @GetMapping("/user")
    @UserAccess
    public String testAnyRole() {
        return "ok_user";
    }

    @GetMapping("/any")
    @AnyAuthorizedUser
    public String testAnyRole2() {
        return "ok_any_user";
    }
}
