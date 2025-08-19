package com.trevari.spring.trauthservice.interfaces.dto;

import com.trevari.spring.trauthservice.domain.user.Role;

public record UserJoinRequestDTO(
        String userId,
        String password,
        String userName,
        Role role
) {}
