package com.verbitsky.service.keycloak.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.verbitsky.service.AbstractApiResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KeycloakIntrospectResponse extends AbstractApiResponse {
    private boolean active;
    private long exp;
    @JsonProperty("username")
    private String userName;
    private String scope;
    private String sub;
    private List<String> roles;

    @JsonProperty("resource_access")
    private void unpackRoles(JsonNode account) {
        JsonNode rolesNode = account.get("account").get("roles");
        try {
            roles = new ObjectMapper().readerForListOf(String.class).readValue(rolesNode);
        } catch (IOException e) {
            log.error("Error during user roles parsing from response: {}", e.getMessage());
            roles = new ArrayList<>();
        }
    }
}