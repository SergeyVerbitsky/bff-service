package com.verbitsky.security;

import com.verbitsky.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component(value = "customAuthProvider")
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomOAuth2TokenAuthentication auth = (CustomOAuth2TokenAuthentication) authentication;
        if (Objects.isNull(auth)) {
            throw new AuthException("Received null authentication object", HttpStatus.FORBIDDEN);
        }

        if (auth.getAuthorities().isEmpty()) {
            throw new AuthException("User authorities is empty", HttpStatus.FORBIDDEN);
        }

        auth.setAuthenticated(auth.isAuthenticationValid());

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(CustomOAuth2TokenAuthentication.class);
    }
}
