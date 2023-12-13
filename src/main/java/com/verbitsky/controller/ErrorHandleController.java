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

import com.verbitsky.api.client.ApiError;
import com.verbitsky.api.client.CommonApiError;
import com.verbitsky.api.client.CommonApiResponse;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.exception.AuthException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
class ErrorHandleController {
    private static final String ERROR_MESSAGE_DELIMITER = ", ";

    @ExceptionHandler(value = {AuthException.class})
    ResponseEntity<CommonApiResponse> handleAuthServiceError(AuthException ex) {
        var httpStatusCode = ex.getHttpStatusCode();
        var response = CommonApiResponse.of(buildApiError(ex), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(response);
    }


    @ExceptionHandler(value = {ServiceException.class})
    ResponseEntity<CommonApiResponse> handleInternalServiceError(ServiceException ex) {
        var httpStatusCode = ex.getHttpStatusCode();
        var apiResponse = CommonApiResponse.of(CommonApiError.of(ex.getMessage(), ex), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(apiResponse);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    ResponseEntity<CommonApiResponse> handleAccessDenied(AccessDeniedException ex) {
        var httpStatusCode = HttpStatus.FORBIDDEN;
        var apiResponse = CommonApiResponse.of(CommonApiError.of(ex.getMessage(), ex), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(apiResponse);
    }

    @ExceptionHandler(value = {WebClientRequestException.class})
    ResponseEntity<CommonApiResponse> handleWebClientException(WebClientRequestException ex) {
        logWebClientException(ex);
        var httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        var apiResponse = CommonApiResponse.of(CommonApiError.of(ex.getMessage(), ex), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(apiResponse);
    }


    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    ResponseEntity<CommonApiResponse> handleInvalidArgsException(MethodArgumentNotValidException ex) {
        var message = getValidationErrors(ex.getFieldErrors());
        var httpStatusCode = HttpStatus.BAD_REQUEST;
        var apiResponse = CommonApiResponse.of(CommonApiError.of(message, "Wrong request data"), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(apiResponse);
    }

    @ExceptionHandler(value = {Exception.class})
    ResponseEntity<CommonApiResponse> handleException(Exception ex) {
        logException(ex);
        var httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        var apiResponse = CommonApiResponse.of(CommonApiError.of(ex.getMessage(), ex), httpStatusCode);

        return ResponseEntity.status(httpStatusCode).body(apiResponse);
    }

    private ApiError buildApiError(ServiceException exception) {
        Throwable cause = exception.getCause();
        return Objects.nonNull(cause)
                ? CommonApiError.of(exception.getMessage(), exception)
                : CommonApiError.of(exception.getMessage(), exception.getCauseAsString());
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
