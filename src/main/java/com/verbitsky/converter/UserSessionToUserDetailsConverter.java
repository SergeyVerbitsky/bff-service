package com.verbitsky.converter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.verbitsky.api.model.dto.SessionDto;
import com.verbitsky.security.CustomUserDetails;

@Component
public class UserSessionToUserDetailsConverter implements ResponseConverter<SessionDto, CustomUserDetails> {
    @Override
    public CustomUserDetails convert(@NonNull SessionDto objectToConvert) {
        return null;
    }

    @Override
    public Class<SessionDto> getResponseType() {
        return SessionDto.class;
    }

    @Override
    public Class<CustomUserDetails> getResultType() {
        return CustomUserDetails.class;
    }
}
