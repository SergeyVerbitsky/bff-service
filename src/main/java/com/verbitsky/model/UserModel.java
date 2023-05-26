package com.verbitsky.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class  UserModel extends BaseModel {
        private String userSub;
        private long expiresAt;
        @EqualsAndHashCode.Exclude
        private String token;
        @EqualsAndHashCode.Exclude
        private List<String> roles;

        public UserModel(String userSub, long expiresAt, String token, List<String> roles) {
                super(userSub);
                this.expiresAt = expiresAt;
                this.token = token;
                this.roles = roles;
        }
}
