package com.trevari.spring.trauthservice.domain.auth;

import com.trevari.spring.trauthservice.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticatedUser {
    private final Long id;
    private final String userId;
    private final String userName;
    private final Role role;
}
