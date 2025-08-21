package com.trevari.spring.trauthservice.interfaces.http;

import com.trevari.spring.trauthservice.application.AuthService;
import com.trevari.spring.trauthservice.interfaces.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sessions")
    @Operation(summary = "로그인", description = "사용자 로그인 후 토큰 발급")
    public ResponseEntity<AuthLoginResponseDTO> login(@RequestBody AuthLoginRequestDTO req) {
        log.info("/api/auth/sessions");

        AuthLoginResponseDTO res = authService.login(req);

        return res != null && res.success()
                ? ResponseEntity.ok(res)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // •	DELETE /api/auth/sessions/me (로그아웃)

    //	•	POST /api/auth/tokens (재발급)
    @PostMapping("/tokens")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access/Refresh 토큰 재발급")
    public ResponseEntity<ReissueTokenResponseDTO> reissueToken(@RequestBody ReissueTokenRequestDTO req) {
        log.info("/api/auth/tokens");

        ReissueTokenResponseDTO res = authService.reissueTokens(req.refreshToken());

        // 성공(유효 RT) → 200 OK + 새 AT/RT 본문 반환
        // 실패(무효 RT 등) → 401 Unauthorized + 실패 본문 반환
        return res != null && res.success()
                ? ResponseEntity.ok(res)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    //	•	POST /api/auth/tokens/validate (검증)
    @PostMapping("/tokens/validate")
    @Operation(summary = "토큰 유효성 검증", description = "Access Token의 유효성 검증 (1: 유효, 2: 만료, -1: 서버 오류)")
    public ResponseEntity<Integer> validateToken(@RequestBody ValidTokenRequestDTO req) {
        log.info("/api/auth/tokens/validate");

        var res = authService.validToken(req.token());

        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1);
        }

        return switch (res.statusNum()) {
            case VALID -> ResponseEntity.ok(1);   // 유효
            case EXPIRED, INVALID -> ResponseEntity.ok(2); // 만료
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1);
        };
    }


}
