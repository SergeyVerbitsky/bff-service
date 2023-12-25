package com.verbitsky.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.verbitsky.api.exception.ServiceException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.verbitsky.testutil.TestDataReader.readDataFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("all")
class CustomTokenDataProviderTest {
    private static final String ISSUER = "https://mykeycloak:9443/realms/dog_app_realm";
    private static final String SUB = "1cc220f0-6da7-439d-af74-a6c93bcfd2d1";
    private static final String EXPECTED_EXC_MSG = "Token value is not valid";
    private static TokenDataProvider tokenDataProvider;
    private static List<String> validTokenList;
    private static List<String> wrongTokenList;

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        tokenDataProvider = new CustomTokenDataProvider(new ObjectMapper());
        validTokenList = readDataFile("token-data/correctToken.txt");
        wrongTokenList = readDataFile("token-data/wrongToken.txt");
    }

    @AfterAll
    static void tearDown() {
        tokenDataProvider = null;
        validTokenList = null;
        wrongTokenList = null;
    }

    @Test
    void getGrantedAuthoritiesPositive() throws ParseException, IOException {
        var actualAuthorities = tokenDataProvider.getGrantedAuthorities(validTokenList.get(0));
        SimpleGrantedAuthority expectedRole = new SimpleGrantedAuthority("ROLE_APP_USER");

        assertFalse(actualAuthorities.isEmpty());
        assertTrue(actualAuthorities.contains(expectedRole));
    }

    @Test
    void getGrantedAuthoritiesServiceExceptionOexpected() {
        ServiceException exception = assertThrows(
                ServiceException.class, () -> tokenDataProvider.getGrantedAuthorities(wrongTokenList.get(0)));

        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equalsIgnoreCase(EXPECTED_EXC_MSG));
    }

    @Test
    void getParametersFromTokenPositive() {
        var tokenParams = tokenDataProvider.getParametersFromToken(validTokenList.get(0));

        assertNotNull(tokenParams);
        JwtClaimsSet jwtClaimsSet = tokenParams.getClaims();
        assertNotNull(jwtClaimsSet);

        Map<String, Object> claims = jwtClaimsSet.getClaims();
        assertNotNull(claims);
        assertFalse(claims.isEmpty());

        JwsHeader jwsHeader = tokenParams.getJwsHeader();
        assertNotNull(jwsHeader);

        Map<String, Object> headers = jwsHeader.getHeaders();
        assertNotNull(headers);
        assertFalse(headers.isEmpty());
    }

    @Test
    void getParametersFromTokenServiceExceptionOexpected() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
                tokenDataProvider.getParametersFromToken(wrongTokenList.get(0)));

        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.equalsIgnoreCase(EXPECTED_EXC_MSG));
    }

    @Test
    void buildJwtPositive() {
        var tokenValue = validTokenList.get(0);
        var tokenParams = prepareTokenParams();

        Jwt actualJwt = tokenDataProvider.buildJwt(tokenValue, tokenParams);
        assertNotNull(actualJwt);
        assertTrue(actualJwt.hasClaim("iss"));
        assertTrue(actualJwt.hasClaim("sub"));
    }

    @Test
    void isTokenExpiredStringValue() {
        String token = validTokenList.get(0);
        boolean tokenExpired = tokenDataProvider.isTokenExpired(token);

        assertTrue(tokenExpired);
    }

    @Test
    void isTokenExpiredStringValueServiceExceptionExpected() {
        String token = wrongTokenList.get(0);
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenDataProvider.isTokenExpired(token));

        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.equalsIgnoreCase(EXPECTED_EXC_MSG));
    }

    @Test
    void isTokenExpiredJwtValueTrue() {
        String token = validTokenList.get(0);
        Jwt jwt = tokenDataProvider.buildJwt(token, prepareExpiredToken());

        assertTrue(tokenDataProvider.isTokenExpired(jwt));
    }

    @Test
    void isTokenExpiredJwtValueFalse() {
        String token = validTokenList.get(0);
        Jwt jwt = tokenDataProvider.buildJwt(token, prepareNonExpiredToken());

        assertFalse(tokenDataProvider.isTokenExpired(jwt));
    }

    @Test
    void isTokenExpiredJwtValueEmptyExpiredField() {
        String token = validTokenList.get(0);
        Jwt jwt = tokenDataProvider.buildJwt(token, prepareTokenParams());

        assertTrue(tokenDataProvider.isTokenExpired(jwt));
    }

    private JwtEncoderParameters prepareTokenParams() {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        builder.issuer(ISSUER);
        builder.subject(SUB);

        return JwtEncoderParameters.from(builder.build());
    }

    private JwtEncoderParameters prepareNonExpiredToken() {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        builder.expiresAt(Instant.MAX);

        return JwtEncoderParameters.from(builder.build());
    }

    private JwtEncoderParameters prepareExpiredToken() {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        builder.expiresAt(Instant.MIN);

        return JwtEncoderParameters.from(builder.build());
    }
}