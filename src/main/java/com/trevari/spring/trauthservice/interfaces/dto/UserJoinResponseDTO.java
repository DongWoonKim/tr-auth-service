package com.trevari.spring.trauthservice.interfaces.dto;

import lombok.Builder;

@Builder
public record UserJoinResponseDTO(
        boolean success,
        String userId,
        String userName,
        String message
) {
    public static UserJoinResponseDTO success(String userId, String userName) {
        return UserJoinResponseDTO.builder()
                .success(true)
                .userId(userId)
                .userName(userName)
                .message("OK")
                .build();
    }
    public static UserJoinResponseDTO failure(String message) {
        return UserJoinResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }
}
