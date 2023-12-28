package com.verbitsky.model;

import java.io.Serializable;

public record LoginResponseData(String userId, String sessionId) implements Serializable {
}
