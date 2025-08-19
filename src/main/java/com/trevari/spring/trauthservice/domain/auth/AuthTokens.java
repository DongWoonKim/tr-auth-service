package com.trevari.spring.trauthservice.domain.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AuthTokens {
    private final String accessToken;
    private final String refreshToken;
    private final Instant issuedAt;
    private final Instant accessExpiresAt;
    private final Instant refreshExpiresAt;
}
