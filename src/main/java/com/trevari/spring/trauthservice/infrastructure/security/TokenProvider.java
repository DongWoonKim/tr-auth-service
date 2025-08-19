package com.trevari.spring.trauthservice.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.time.Clock;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties props;
    private final Clock clock = Clock.systemUTC(); // 테스트 용이성↑
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // props.getSecret() 이 이미 Base64 라면 decode, 아니면 bytes 그대로 사용
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(props.getSecret());
        } catch (IllegalArgumentException e) {
            keyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 사용자 정보로 토큰 생성 (access/refresh 공용) */
    public String generateToken(String userId, Long id, String role, String userName, Duration ttl) {
        Instant now = clock.instant();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(props.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .setSubject(userId)
                .claim("id", id)
                .claim("role", role)
                .claim("userName", userName)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /** 토큰 상태만 판단 */
    public TokenStatus validate(String token) {
        try {
            parseClaims(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException e) {
            log.info("JWT expired: {}", e.getMessage());
            return TokenStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("JWT invalid: {}", e.getMessage());
            return TokenStatus.INVALID;
        }
    }

    /** 토큰을 파싱해 클레임 VO로 반환 (도메인/DTO 생성 금지) */
    public TokenClaims parse(String token) {
        Claims c = parseClaims(token);
        return new TokenClaims(
                c.getSubject(),
                c.get("id", Long.class),
                c.get("role", String.class),
                c.get("userName", String.class),
                c.getExpiration().toInstant()
        );
    }

    // ---------- 내부 유틸 ----------
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public enum TokenStatus { VALID, EXPIRED, INVALID }

    /** 인프라 전용 파싱 결과 (계층 간 운반용) */
    public record TokenClaims(String userId, Long id, String role, String userName, Instant exp) {}
}
