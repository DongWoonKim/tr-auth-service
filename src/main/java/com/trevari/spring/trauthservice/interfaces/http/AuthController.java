package com.trevari.spring.trauthservice.interfaces.http;

import com.trevari.spring.trauthservice.application.AuthService;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserLoginResponseDTO;
import lombok.RequiredArgsConstructor;
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
    public UserLoginResponseDTO login(@RequestBody UserLoginRequestDTO req) {
        return authService.login(req);
    }

}
