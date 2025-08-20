package com.trevari.spring.trauthservice.interfaces.dto;

import lombok.Builder;

@Builder
public record AuthLoginResponseDTO(
        boolean success,
        String userName,
        String userId,
        String accessToken,
        String refreshToken,
        String message
) {
    // 로그인 성공 시
    public static AuthLoginResponseDTO success(
            String userId,
            String userName,
            String accessToken,
            String refreshToken
    ) {
        return AuthLoginResponseDTO.builder()
                .success(true)
                .userId(userId)
                .userName(userName)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 로그인 실패 시
    public static AuthLoginResponseDTO failure(String message) {
        return AuthLoginResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    // refresh null 처리
    public AuthLoginResponseDTO withoutRefreshToken() {
        return AuthLoginResponseDTO.builder()
                .success(success)
                .userId(userId)
                .userName(userName)
                .accessToken(accessToken)
                .build();
    }
}
