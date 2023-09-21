package com.verbitsky.converter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.verbitsky.api.model.SessionModel;
import com.verbitsky.security.CustomUserDetails;

@Component
public class UserSessionToUserDetailsConverter implements ResponseConverter<SessionModel, CustomUserDetails> {
    @Override
    public CustomUserDetails convert(@NonNull SessionModel objectToConvert) {
        return null;
    }

    @Override
    public Class<SessionModel> getResponseType() {
        return SessionModel.class;
    }

    @Override
    public Class<CustomUserDetails> getResultType() {
        return CustomUserDetails.class;
    }
}
