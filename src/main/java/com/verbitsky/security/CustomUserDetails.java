package com.verbitsky.security;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.verbitsky.security.TokenFields.SESSION_ID;
import static com.verbitsky.security.TokenFields.USER_ACCOUNT_ENABLED;
import static com.verbitsky.security.TokenFields.USER_ACCOUNT_NON_EXPIRED;
import static com.verbitsky.security.TokenFields.USER_ACCOUNT_NON_LOCKED;
import static com.verbitsky.security.TokenFields.USER_CREDENTIALS_NON_EXPIRED;
import static com.verbitsky.security.TokenFields.USER_NAME;

public class CustomUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = -2078667642572119835L;

    @Getter
    private final String accessToken;
    @Getter
    private final String refreshToken;
    @Getter
    private final Jwt jwt;
    private String userId;
    private String sessionId;

    private final List<? extends GrantedAuthority> grantedAuthorities;

    public CustomUserDetails(String refreshToken, Jwt jwt,
                             List<? extends GrantedAuthority> grantedAuthorities) {

        this.refreshToken = refreshToken;
        this.jwt = jwt;
        this.accessToken = jwt.getTokenValue();
        this.grantedAuthorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableList(grantedAuthorities);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return jwt.getClaimAsString(USER_NAME);
    }

    @Override
    public boolean isAccountNonExpired() {
        return jwt.getClaimAsBoolean(USER_ACCOUNT_NON_EXPIRED);
    }

    @Override
    public boolean isAccountNonLocked() {
        return jwt.getClaimAsBoolean(USER_ACCOUNT_NON_LOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return jwt.getClaimAsBoolean(USER_CREDENTIALS_NON_EXPIRED);
    }

    @Override
    public boolean isEnabled() {
        return jwt.getClaimAsBoolean(USER_ACCOUNT_ENABLED);
    }

    public String getUserId() {
        if (StringUtils.isBlank(userId)) {
            userId = String.valueOf(jwt.getClaims().get(JwtClaimNames.SUB));
        }

        return userId;
    }

    public String getSessionId() {
        if (StringUtils.isBlank(sessionId)) {
            sessionId = String.valueOf(jwt.getClaims().get(SESSION_ID));
        }

        return sessionId;
    }
}
