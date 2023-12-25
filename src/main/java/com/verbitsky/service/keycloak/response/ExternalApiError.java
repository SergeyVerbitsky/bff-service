package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.verbitsky.api.client.response.ApiError;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiError implements ApiError {
    @Serial
    private static final long serialVersionUID = -3480624977886665216L;
    private static final String DEFAULT_ERROR_MESSAGE = "Unknown error";
    private static final String DEFAULT_CAUSE = "External service error";
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("error_description")
    private String errorDescription;
    @JsonProperty("error")
    private String error;


    public void setError(String error) {
        this.error = error;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonIgnore
    public String getErrorDescription() {
        List<String> errorValues = Arrays.asList(errorDescription, errorMessage, error);

        String errorValue = errorValues.stream()
                .filter(value -> Objects.nonNull(value) && !value.isBlank())
                .collect(Collectors.joining(" "));

        return errorValue.isEmpty() ? DEFAULT_ERROR_MESSAGE : errorValue;
    }

    @Override
    public String getErrorMessage() {
        return getErrorDescription();
    }

    @Override
    public String getCause() {
        return DEFAULT_CAUSE;
    }
}