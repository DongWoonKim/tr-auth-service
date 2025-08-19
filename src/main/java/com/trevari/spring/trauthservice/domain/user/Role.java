package com.trevari.spring.trauthservice.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
