package com.verbitsky.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.verbitsky.api.client.ApiResponse;
import com.verbitsky.api.client.CommonApiError;
import com.verbitsky.api.client.CommonApiResponse;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<ApiResponse> handleError() {
        var errorMessage = "Requested uri or resource not found";
        var errorResponse = CommonApiResponse.of(CommonApiError.of(errorMessage), NOT_FOUND);

        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }
}