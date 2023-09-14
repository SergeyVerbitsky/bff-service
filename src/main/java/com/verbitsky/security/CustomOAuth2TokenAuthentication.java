package com.verbitsky.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomOAuth2TokenAuthentication extends AbstractOAuth2TokenAuthenticationToken<Jwt> {
    @EqualsAndHashCode.Exclude
    private final boolean accountNonExpired;
    @EqualsAndHashCode.Exclude
    private final boolean accountNonLocked;
    @EqualsAndHashCode.Exclude
    private final boolean credentialsNonExpired;
    @EqualsAndHashCode.Exclude
    private final boolean accountEnabled;
    @EqualsAndHashCode.Exclude
    private final boolean accountHasRoles;
    @Serial
    private static final long serialVersionUID = -4057430805752370407L;

    protected CustomOAuth2TokenAuthentication(CustomUserDetails userDetails) {
        super(userDetails.getJwt(), userDetails.getAuthorities());

        this.accountNonExpired = userDetails.isAccountNonExpired();
        this.accountNonLocked = userDetails.isAccountNonLocked();
        this.credentialsNonExpired = userDetails.isCredentialsNonExpired();
        this.accountEnabled = userDetails.isEnabled();
        this.accountHasRoles = !userDetails.getAuthorities().isEmpty();
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return Collections.unmodifiableMap(getToken().getClaims());
    }

    public static CustomOAuth2TokenAuthentication authenticationFromUserDetails(CustomUserDetails userDetails) {
        return new CustomOAuth2TokenAuthentication(userDetails);
    }

    public boolean isAuthenticationValid() {
        return isAccountValid() && areCredentialsValid() && hasRoles();
    }

    private boolean isAccountValid() {
        return accountNonExpired && accountNonLocked && accountEnabled;
    }

    private boolean areCredentialsValid() {
        return credentialsNonExpired;
    }

    private boolean hasRoles() {
        return accountHasRoles;
    }
}
