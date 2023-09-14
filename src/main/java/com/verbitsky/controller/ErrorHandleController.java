package com.verbitsky.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import lombok.extern.slf4j.Slf4j;

import com.verbitsky.exception.AuthException;
import com.verbitsky.exception.RemoteServiceException;
import com.verbitsky.exception.ServiceException;
import com.verbitsky.service.keycloak.client.ErrorMessage;
import com.verbitsky.service.keycloak.exception.InvalidKeycloakRequestException;

@Slf4j
@ControllerAdvice
class ErrorHandleController {
    @ExceptionHandler(value = {InvalidKeycloakRequestException.class})
    ResponseEntity<ErrorMessage> handleConflict(InvalidKeycloakRequestException ex) {
        ErrorMessage errorMessage = new ErrorMessage(
                ex.getErrorCode(), ex.getErrorCode().value(), ex.getErrorMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {RemoteServiceException.class})
    ResponseEntity<ErrorMessage> handleRemoteServiceError(RemoteServiceException ex) {
        ErrorMessage errorMessage = new ErrorMessage(
                ex.getHttpStatus(), ex.getHttpStatus().value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {ServiceException.class})
    ResponseEntity<ErrorMessage> handleServiceError(ServiceException ex) {
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {AuthException.class})
    ResponseEntity<ErrorMessage> handleServiceError(AuthException ex) {
        ErrorMessage errorMessage = new ErrorMessage(
                ex.getHttpStatus(), ex.getHttpStatus().value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    ResponseEntity<ErrorMessage> handleAccessDenied(AccessDeniedException ex) {
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {WebClientRequestException.class})
    ResponseEntity<ErrorMessage> handleException(WebClientRequestException ex) {
        logWebClientException(ex);
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

        return ResponseEntity.ofNullable(errorMessage);
    }

    @ExceptionHandler(value = {Exception.class})
    ResponseEntity<ErrorMessage> handleException(Exception ex) {
        logException(ex);
        ErrorMessage errorMessage = new ErrorMessage(
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
}
