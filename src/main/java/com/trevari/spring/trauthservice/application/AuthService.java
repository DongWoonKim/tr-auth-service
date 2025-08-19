package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.infrastructure.security.CustomUserDetails;
import com.trevari.spring.trauthservice.infrastructure.security.TokenProvider;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginResponseDTO;
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

    public UserLoginResponseDTO login(UserLoginRequestDTO userLoginRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginRequestDTO.userId(),
                            userLoginRequestDTO.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = createToken(userDetails, Duration.ofHours(2));
            String refreshToken = createToken(userDetails, Duration.ofDays(2));

            return UserLoginResponseDTO.success(
                    userDetails.getUserId(),
                    userDetails.getUsername(),
                    accessToken,
                    refreshToken
            );
        } catch (AuthenticationException e) {
            return UserLoginResponseDTO.failure("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
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

}
