package com.verbitsky.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @GetMapping("/test")
    String testBff() {
        return "Bff application is ok.";
    }
}
