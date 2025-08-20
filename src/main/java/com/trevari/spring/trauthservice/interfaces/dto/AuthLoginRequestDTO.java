package com.trevari.spring.trauthservice.interfaces.dto;

public record AuthLoginRequestDTO(
        String userId,
        String password
) {}
