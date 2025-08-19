package com.trevari.spring.trauthservice.interfaces.dto;

import lombok.Builder;

@Builder
public record UserLoginResponseDTO(
        boolean success,
        String userName,
        String userId,
        String accessToken,
        String refreshToken,
        String message
) {
    // 로그인 성공 시
    public static UserLoginResponseDTO success(
            String userId,
            String userName,
            String accessToken,
            String refreshToken
    ) {
        return UserLoginResponseDTO.builder()
                .success(true)
                .userId(userId)
                .userName(userName)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 로그인 실패 시
    public static UserLoginResponseDTO failure(String message) {
        return UserLoginResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }
}
