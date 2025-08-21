package com.trevari.spring.trauthservice.interfaces.http;

import com.trevari.spring.trauthservice.application.UserService;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;


    @PostMapping
    @Operation(summary = "회원가입", description = "신규 사용자 회원가입 처리")
    public ResponseEntity<UserJoinResponseDTO> createUser(@RequestBody UserJoinRequestDTO req) {
        var res = userService.join(req);
        return ResponseEntity
                .status(res.success() ? 201 : 409)
                .body(res);
    }
}
