package com.trevari.spring.trauthservice.interfaces.dto;

import com.trevari.spring.trauthservice.infrastructure.security.TokenProvider;
import lombok.Builder;

@Builder
public record ValidTokenResponseDTO(
        TokenProvider.TokenStatus statusNum
) {}
