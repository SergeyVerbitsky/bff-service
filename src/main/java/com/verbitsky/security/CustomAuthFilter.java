package com.verbitsky.security;

import com.verbitsky.exception.AuthException;
import com.verbitsky.service.auth.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static com.verbitsky.security.RequestFields.SESSION_ID;
import static com.verbitsky.security.RequestFields.USER_ID;

@Component
public class CustomAuthFilter extends GenericFilterBean {
    private final AuthService authService;

    public CustomAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        processUserAuthentication(request, response);
        chain.doFilter(request, response);
    }

    private void processUserAuthentication(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader(USER_ID);
        String sessionId = req.getHeader(SESSION_ID);

        try {
            if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(sessionId)) {
                CustomOAuth2TokenAuthentication authentication = authService.resolveAuthentication(userId, sessionId);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (AuthException exception) {
            SecurityContextHolder.clearContext();
            ((HttpServletResponse) response).sendError(exception.getHttpStatus().value());
            throw new AuthException("Token is not valid or expired");
        }
    }
}