package com.trevari.spring.trauthservice.interfaces.dto;

public record UserLoginRequestDTO(
        String userId,
        String password
) {}
