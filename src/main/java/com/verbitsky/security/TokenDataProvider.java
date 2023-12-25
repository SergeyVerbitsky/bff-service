package com.verbitsky.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.List;

public interface TokenDataProvider {
    List<SimpleGrantedAuthority> getGrantedAuthorities(String token);

    JwtEncoderParameters getParametersFromToken(String token);

    Jwt buildJwt(String tokenValue, JwtEncoderParameters parameters);

    boolean isTokenExpired(Jwt token);

    boolean isTokenExpired(String token);
}
