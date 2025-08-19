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
}
