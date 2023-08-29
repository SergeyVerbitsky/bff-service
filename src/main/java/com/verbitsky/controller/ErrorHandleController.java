package com.verbitsky.controller;


import com.verbitsky.keycloak.exception.InvalidKeycloakRequestException;
import com.verbitsky.keycloak.client.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
class ErrorHandleController {
    @ExceptionHandler(value = {InvalidKeycloakRequestException.class})
    protected ResponseEntity<ErrorMessage> handleConflict(InvalidKeycloakRequestException ex/*, WebRequest request*/) {
        //todo add log of request
        return ResponseEntity.ofNullable(new ErrorMessage(ex.getErrorCode(), ex.getErrorMessage()));
    }

    //todo add method to process 400 from UI (without body or etc)
}
