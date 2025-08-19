package com.trevari.spring.trauthservice.domain.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private final Long id;
    private final String userId;
    private final String password;
    private final String userName;
    private final Role role;

    public static User create(String userId, String encodedPassword, String userName, Role role) {
        return User.builder()
                .userId(userId)
                .password(encodedPassword)
                .userName(userName)
                .role(role)
                .build();
    }

    public static User reconstruct(Long id, String userId, String encodedPassword, String userName, Role role) {
        return User.builder()
                .id(id)
                .userId(userId)
                .password(encodedPassword)
                .userName(userName)
                .role(role)
                .build();
    }
}
