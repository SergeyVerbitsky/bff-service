package com.verbitsky.security.annotation;

import org.springframework.security.core.GrantedAuthority;

public enum ApplicationRole implements GrantedAuthority {
    ROLE_APP_USER,
    ROLE_APP_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
