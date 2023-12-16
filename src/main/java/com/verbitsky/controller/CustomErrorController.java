package com.verbitsky.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.verbitsky.api.client.ApiResponse;
import com.verbitsky.api.client.CommonApiError;
import com.verbitsky.api.client.CommonApiResponse;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public ResponseEntity<ApiResponse> handleGetError() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getErrorResponse());
    }

    @PostMapping("/error")
    public ResponseEntity<ApiResponse> handlePostError() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(getErrorResponse());
    }

    private ApiResponse getErrorResponse() {
        var errorMessage = "Requested uri or resource not found";
        return CommonApiResponse.of(CommonApiError.of(errorMessage), NOT_FOUND);
    }
}