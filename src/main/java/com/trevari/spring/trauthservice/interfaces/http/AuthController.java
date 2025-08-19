package com.trevari.spring.trauthservice.interfaces.http;

import com.trevari.spring.trauthservice.application.AuthService;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sessions")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO req) {
        var res = authService.login(req);
        if (res.success()) {
            return ResponseEntity
                    .ok(res); // 200 OK
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(res);
        }
    }

}
