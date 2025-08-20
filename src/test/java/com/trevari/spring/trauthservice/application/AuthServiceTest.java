package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.infrastructure.security.CustomUserDetails;
import com.trevari.spring.trauthservice.infrastructure.security.JwtProperties;
import com.trevari.spring.trauthservice.infrastructure.security.TokenProvider;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 통합테스트
class AuthServiceTest {

    private AuthService authService;
    private TokenProvider tokenProvider;

    // --- 테스트 고정값 ---
    private final String USER_ID = "hong";
    private final String RAW_PW = "1234";
    private final Long   USER_DB_ID = 1L;


    @BeforeEach
    void setUp() {
        // 1) 진짜 PasswordEncoder
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // 2) 진짜 UserDetailsService (인메모리)
        UserDetailsService uds = username -> {
            if (!USER_ID.equals(username)) throw new UsernameNotFoundException(username);
            // 도메인에서 가져왔다고 가정하고 CustomUserDetails 생성
            return CustomUserDetails.builder()
                    .id(USER_DB_ID)
                    .userId(USER_ID)
                    .password(encoder.encode(RAW_PW))
                    .authorities(List.of((GrantedAuthority) () -> "ROLE_USER"))
                    .build();
        };

        // 3) DaoAuthenticationProvider + ProviderManager → 진짜 AuthenticationManager
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        AuthenticationManager authenticationManager = new ProviderManager(provider);

        // 4) 진짜 TokenProvider
        JwtProperties props = new JwtProperties();
        props.setIssuer("trevari-auth");
        props.setSecret(generateBase64Secret(64));
        this.tokenProvider = new TokenProvider(props);
        this.tokenProvider.init();

        // 5) 테스트 대상 서비스
        this.authService = new AuthService(authenticationManager, tokenProvider);
    }

    @Test
    void 로그인_성공시_success와_AT_RT발급() {
        // given
        AuthLoginRequestDTO req = new AuthLoginRequestDTO(USER_ID, RAW_PW);

        // when
        AuthLoginResponseDTO res = authService.login(req);

        // then
        assertThat(res.success()).isTrue();
        assertThat(res.userId()).isEqualTo(USER_ID);
        assertThat(res.userName()).isEqualTo(USER_ID); // 현재 구현상 username = userId라면
        assertThat(res.accessToken()).isNotBlank();
        assertThat(res.refreshToken()).isNotBlank();

        // 토큰 유효성
        assertThat(tokenProvider.validate(res.accessToken()).name()).isEqualTo("VALID");
        assertThat(tokenProvider.validate(res.refreshToken()).name()).isEqualTo("VALID");

        var claims = tokenProvider.parse(res.accessToken());
        assertThat(claims.userId()).isEqualTo(USER_ID);
        assertThat(claims.id()).isEqualTo(USER_DB_ID);
        assertThat(claims.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void 로그인_실패시_failure와_토큰_null() {
        // given
        AuthLoginRequestDTO wrong = new AuthLoginRequestDTO(USER_ID, "bad-password");

        // when
        AuthLoginResponseDTO res = authService.login(wrong);

        // then
        assertThat(res.success()).isFalse();
        assertThat(res.message()).contains("아이디 또는 비밀번호");
        assertThat(res.accessToken()).isNull();
        assertThat(res.refreshToken()).isNull();
    }

    // === 유틸: 안전한 Base64 시크릿 생성 ===
    private static String generateBase64Secret(int bytes) {
        byte[] key = new byte[bytes];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}