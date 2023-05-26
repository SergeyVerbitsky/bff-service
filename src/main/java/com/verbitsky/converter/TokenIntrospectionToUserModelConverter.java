package com.verbitsky.converter;

import com.verbitsky.keycloak.response.KeycloakIntrospectResponse;
import com.verbitsky.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.verbitsky.keycloak.request.KeycloakFields.TOKEN;

@Component
public class TokenIntrospectionToUserModelConverter
        implements KeycloakResponseConverter<KeycloakIntrospectResponse, UserModel> {

    @Override
    public UserModel convert(KeycloakIntrospectResponse response, Map<String, String> params) {
        return new UserModel(
                response.getSub(),
                response.getExp(),
                params.getOrDefault(TOKEN, StringUtils.EMPTY),
                response.getRoles());
    }
}
