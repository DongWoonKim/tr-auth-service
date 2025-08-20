package com.trevari.spring.trauthservice.interfaces.dto;

import lombok.Builder;

@Builder
public record ReissueTokenResponseDTO(
    boolean success,
    int statusNum,
    String accessToken,
    String refreshToken
) {
    // refresh null 처리
    public ReissueTokenResponseDTO withoutRefreshToken() {
        return ReissueTokenResponseDTO.builder()
                .success(success)
                .accessToken(accessToken)
                .build();
    }
}
