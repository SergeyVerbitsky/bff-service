package com.verbitsky.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.List;
import java.util.Optional;

public interface TokenDataProvider {
    List<SimpleGrantedAuthority> getGrantedAuthorities(String token);

    JwtEncoderParameters getParametersFromToken(String token);

    Optional<String> getTokenClaim(String token, String claimName);

    String getUserLogin(String token);

    String getKeycloakUserId(String token);

    String getSessionId(String token);

    Jwt buildJwt(String tokenValue, JwtEncoderParameters parameters);

    boolean isTokenExpired(Jwt token);

    boolean isTokenExpired(String token);
}
