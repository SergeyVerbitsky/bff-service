package com.verbitsky.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.verbitsky.api.client.response.CommonApiError;
import com.verbitsky.api.client.response.CommonApiResponse;
import com.verbitsky.exception.AuthException;
import com.verbitsky.service.auth.AuthService;

import java.io.IOException;

@Component
public class CustomAuthFilter extends GenericFilterBean {
    private static final String SESSION_ID = "sessionId";
    private static final String USER_ID = "userId";
    private final AuthService authService;

    public CustomAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            processUserAuthentication(request);
            chain.doFilter(request, response);
        } catch (AuthException exception) {
            handleAuthException(response, exception);
        }
    }

    private void processUserAuthentication(ServletRequest request) throws AuthException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader(USER_ID);
        String sessionId = req.getHeader(SESSION_ID);

        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(sessionId)) {
            CustomOAuth2TokenAuthentication authentication = authService.resolveAuthentication(userId, sessionId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private void handleAuthException(ServletResponse response, AuthException exception) throws IOException {
        var res = (HttpServletResponse) response;
        var httpStatusCode = exception.getHttpStatusCode();
        var errorMessage = exception.getMessage();
        var errorResponse = CommonApiResponse.of(CommonApiError.of(errorMessage, exception), httpStatusCode);

        res.setStatus(httpStatusCode.value());
        res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}