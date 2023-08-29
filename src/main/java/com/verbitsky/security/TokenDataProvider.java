package com.verbitsky.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface TokenDataProvider {
    List<SimpleGrantedAuthority> getGrantedAuthorities(String token)
            throws ParseException, IOException;

    JwtEncoderParameters getParametersFromToken(String token) throws ParseException;

    Jwt buildJwt(String tokenValue, JwtEncoderParameters parameters) throws JwtEncodingException;

    boolean isTokenValid(Jwt token);

    boolean isTokenValid(String token);
}
