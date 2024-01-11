package com.verbitsky.service.converter;

import org.springframework.stereotype.Component;

import com.verbitsky.api.converter.ServiceResponseConverter;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.security.TokenDataProvider;

@Component
public class SessionConverter implements ServiceResponseConverter<SessionModel, CustomUserDetails> {
    private final TokenDataProvider tokenDataProvider;

    public SessionConverter(TokenDataProvider tokenDataProvider) {
        this.tokenDataProvider = tokenDataProvider;
    }

    @Override
    public Class<SessionModel> getResponseType() {
        return SessionModel.class;
    }

    @Override
    public Class<CustomUserDetails> getResultType() {
        return CustomUserDetails.class;
    }

    @Override
    public CustomUserDetails convert(SessionModel source) {
        var accessToken = source.getAccessToken();
        var refreshToken = source.getRefreshToken();
        var tokenParams = tokenDataProvider.getParametersFromToken(accessToken);
        var userAuthorities = tokenDataProvider.getGrantedAuthorities(accessToken);
        var jwt = tokenDataProvider.buildJwt(accessToken, tokenParams);

        return new CustomUserDetails(jwt, refreshToken, userAuthorities);
    }
}
