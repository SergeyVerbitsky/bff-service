package com.verbitsky.converter;

import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.service.backend.response.UserSessionResponse;
import org.springframework.stereotype.Component;

@Component
public class UserSessionToUserDetailsConverter implements ResponseConverter<UserSessionResponse, CustomUserDetails> {


    @Override
    public CustomUserDetails convert(UserSessionResponse objectToConvert) {
        return null;
    }

    @Override
    public Class<UserSessionResponse> getResponseType() {
        return UserSessionResponse.class;
    }

    @Override
    public Class<CustomUserDetails> getResultType() {
        return CustomUserDetails.class;
    }
}
