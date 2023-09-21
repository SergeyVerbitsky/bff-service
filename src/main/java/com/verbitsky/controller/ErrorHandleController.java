package com.verbitsky.controller;

import org.apache.commons.lang3.StringUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.exception.AuthException;
import com.verbitsky.service.keycloak.client.ErrorMessage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
class ErrorHandleController {
    private static final String ERROR_MESSAGE_DELIMITER = ", ";

    @ExceptionHandler(value = {AuthException.class})
    ResponseEntity<ErrorMessage> handleServiceError(AuthException ex) {
        var errorMessage = new ErrorMessage(
                ex.getHttpStatusCode(), ex.getHttpStatusCode().value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    ResponseEntity<ErrorMessage> handleAccessDenied(AccessDeniedException ex) {
        var errorMessage = new ErrorMessage(
                HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {WebClientRequestException.class})
    ResponseEntity<ErrorMessage> handleException(WebClientRequestException ex) {
        logWebClientException(ex);
        var errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }


    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    ResponseEntity<ErrorMessage> handleInvalidArgsException(MethodArgumentNotValidException ex) {
        var message = getValidationErrors(ex.getFieldErrors());
        var errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), message);

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {Exception.class})
    ResponseEntity<ErrorMessage> handleException(Exception ex) {
        logException(ex);
        var errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    private void logException(Exception ex) {
        log.error("Unexpected error: error class{}, error message: {}",
                ex.getClass(), ex.getMessage());
    }

    private void logWebClientException(WebClientRequestException ex) {
        log.error("External service error: request method: {}, request uri: {}, error message: {}",
                ex.getMethod(), ex.getUri(), ex.getMessage());
    }

    private String getValidationErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(ERROR_MESSAGE_DELIMITER));
    }
}
