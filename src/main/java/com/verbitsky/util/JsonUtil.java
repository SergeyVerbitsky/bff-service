package com.verbitsky.util;

import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;

@Slf4j
public final class JsonUtil {
    private JsonUtil() {
    }

    public static String extractErrorDescription(@NonNull String errorResponseBody) {
        String errorDescription;
        try {
            JSONObject jsonObject = new JSONObject(errorResponseBody);
            errorDescription = jsonObject.get("error_description").toString();

        } catch (Exception e) {
            log.error("Unable to extract error description from source: {}", errorResponseBody);
            errorDescription = errorResponseBody;
        }

        return errorDescription;
    }
}
