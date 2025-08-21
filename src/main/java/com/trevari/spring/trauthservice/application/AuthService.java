package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.infrastructure.security.CustomUserDetails;
import com.trevari.spring.trauthservice.infrastructure.security.TokenProvider;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginResponseDTO;
import com.trevari.spring.trauthservice.interfaces.dto.ReissueTokenResponseDTO;
import com.trevari.spring.trauthservice.interfaces.dto.ValidTokenResponseDTO;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    public AuthLoginResponseDTO login(AuthLoginRequestDTO userLoginRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginRequestDTO.userId(),
                            userLoginRequestDTO.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = createToken(userDetails, Duration.ofHours(20));
            String refreshToken = createToken(userDetails, Duration.ofDays(2));

            return AuthLoginResponseDTO.success(
                    userDetails.getUserId(),
                    userDetails.getUsername(),
                    accessToken,
                    refreshToken
            );
        } catch (AuthenticationException e) {
            return AuthLoginResponseDTO.failure("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    public ReissueTokenResponseDTO reissueTokens(String refreshToken) {
        // 쿠키에서 Refresh Token을 추출
        if (refreshToken == null || validToken(refreshToken).statusNum() != TokenProvider.TokenStatus.VALID){
            return ReissueTokenResponseDTO.builder()
                    .success(false)
                    .statusNum(-1)
                    .build();
        }

        TokenProvider.TokenClaims parse = tokenProvider.parse(refreshToken);
        String accessToken = createToken(parse, Duration.ofSeconds(20));
        refreshToken = createToken(parse, Duration.ofDays(2));

        return ReissueTokenResponseDTO.builder()
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public ValidTokenResponseDTO validToken(String token) {
        TokenProvider.TokenStatus validate = tokenProvider.validate(token);
        return ValidTokenResponseDTO
                .builder()
                .statusNum(validate)
                .build();
    }

    private String createToken(CustomUserDetails userDetails, Duration ttl) {
        return tokenProvider.generateToken(
                userDetails.getUserId(),
                userDetails.getId(),
                userDetails.getRole(),
                userDetails.getUsername(),
                ttl
        );
    }

    private String createToken(TokenProvider.TokenClaims userDetails, Duration ttl) {
        return tokenProvider.generateToken(
                userDetails.userId(),
                userDetails.id(),
                userDetails.role(),
                userDetails.userName(),
                ttl
        );
    }

}
