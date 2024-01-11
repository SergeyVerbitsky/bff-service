package com.verbitsky.storage;

import java.util.List;

public final class CacheConstants {
    //cache names
    public static final String USER_SESSION_BY_ID = "findBySessionId";
    public static final String USER_SESSION_BY_LOGIN = "findByLogin";

    //cache keys
    public static final String SESSION_ID_KEY = "#sessionId";
    public static final String ENTITY_SESSION_ID_KEY = "#entity.sessionId";
    public static final String USER_LOGIN_KEY = "#login";
    public static final String ENTITY_USER_LOGIN_KEY = "#entity.login";

    //cache conditions
    public static final String NULL_RESULT_CONDITION = "#result == null";

    public static List<String> getCacheNames() {
        return List.of(
                USER_SESSION_BY_ID,
                USER_SESSION_BY_LOGIN
        );
    }

    private CacheConstants() {
    }
}
