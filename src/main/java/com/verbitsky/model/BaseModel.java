package com.verbitsky.model;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseModel {
    @Getter
    @Setter
    private String id;

    protected BaseModel(String id) {
        this.id = id;
    }
}
