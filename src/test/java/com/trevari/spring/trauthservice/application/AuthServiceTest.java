package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.infrastructure.security.CustomUserDetails;
import com.trevari.spring.trauthservice.infrastructure.security.JwtProperties;
import com.trevari.spring.trauthservice.infrastructure.security.TokenProvider;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.AuthLoginResponseDTO;
import com.trevari.spring.trauthservice.interfaces.dto.ReissueTokenResponseDTO;
import com.trevari.spring.trauthservice.interfaces.dto.ValidTokenResponseDTO;
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
import java.time.Duration;
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
    private final String ROLE = "ROLE_USER";
    private final String USER_NAME = "홍길동";


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

    @Test
    void 재발급_실패_형식오류토큰_INVALID() {
        String invalid = "not-a-jwt-token";

        ReissueTokenResponseDTO res = authService.reissueTokens(invalid);

        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
        assertThat(res.statusNum()).isEqualTo(-1);
    }

    @Test
    void 재발급_실패_만료토큰_EXPIRED() {
        // 과거 만료(음수 TTL)로 만든 RT
        String expiredRt = tokenProvider.generateToken(USER_ID, USER_DB_ID, ROLE, USER_NAME, Duration.ofSeconds(-30));

        ReissueTokenResponseDTO res = authService.reissueTokens(expiredRt);

        assertThat(res).isNotNull();
        assertThat(res.success()).isFalse();
        assertThat(res.statusNum()).isEqualTo(-1);
    }

    @Test
    void 재발급_성공_VALID() {
        // 유효한 RT 생성
        String rt = tokenProvider.generateToken(USER_ID, USER_DB_ID, ROLE, USER_NAME, Duration.ofDays(2));

        ReissueTokenResponseDTO res = authService.reissueTokens(rt);

        // 성공 여부
        assertThat(res).isNotNull();
        assertThat(res.success()).isTrue();
        assertThat(res.accessToken()).isNotBlank();
        assertThat(res.refreshToken()).isNotBlank();

        // 새로 받은 AT/RT 모두 VALID
        assertThat(tokenProvider.validate(res.accessToken())).isEqualTo(TokenProvider.TokenStatus.VALID);
        assertThat(tokenProvider.validate(res.refreshToken())).isEqualTo(TokenProvider.TokenStatus.VALID);

        // 클레임 유지 확인
        TokenProvider.TokenClaims atClaims = tokenProvider.parse(res.accessToken());
        TokenProvider.TokenClaims rtClaims = tokenProvider.parse(res.refreshToken());

        assertThat(atClaims.userId()).isEqualTo(USER_ID);
        assertThat(atClaims.id()).isEqualTo(USER_DB_ID);
        assertThat(atClaims.role()).isEqualTo(ROLE);
        assertThat(atClaims.userName()).isEqualTo(USER_NAME);

        assertThat(rtClaims.userId()).isEqualTo(USER_ID);
        assertThat(rtClaims.id()).isEqualTo(USER_DB_ID);
        assertThat(rtClaims.role()).isEqualTo(ROLE);
        assertThat(rtClaims.userName()).isEqualTo(USER_NAME);
    }

    // ---------------------------
    // validToken() 테스트
    // ---------------------------

    @Test
    void 토큰검증_VALID() {
        String at = tokenProvider.generateToken(USER_ID, USER_DB_ID, ROLE, USER_NAME, Duration.ofMinutes(10));
        ValidTokenResponseDTO res = authService.validToken(at);
        assertThat(res).isNotNull();
        assertThat(res.statusNum()).isEqualTo(TokenProvider.TokenStatus.VALID);
    }

    @Test
    void 토큰검증_EXPIRED() {
        String expired = tokenProvider.generateToken(USER_ID, USER_DB_ID, ROLE, USER_NAME, Duration.ofSeconds(-1));
        ValidTokenResponseDTO res = authService.validToken(expired);
        assertThat(res.statusNum()).isEqualTo(TokenProvider.TokenStatus.EXPIRED);
    }

    @Test
    void 토큰검증_INVALID() {
        String invalid = "!!!broken.token.value!!!";
        ValidTokenResponseDTO res = authService.validToken(invalid);
        assertThat(res.statusNum()).isEqualTo(TokenProvider.TokenStatus.INVALID);
    }

    // === 유틸: 안전한 Base64 시크릿 생성 ===
    private static String generateBase64Secret(int bytes) {
        byte[] key = new byte[bytes];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}