package com.trevari.spring.trauthservice.interfaces.http;

import com.trevari.spring.trauthservice.application.UserService;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;


    @PostMapping("/users")
    public ResponseEntity<UserJoinResponseDTO> createUser(@RequestBody UserJoinRequestDTO req) {
        var res = userService.join(req);
        return ResponseEntity
                .status(res.success() ? 201 : 409)
                .body(res);
    }
}
