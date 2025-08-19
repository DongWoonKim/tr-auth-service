package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.domain.user.User;
import com.trevari.spring.trauthservice.domain.user.UserRepository;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserJoinResponseDTO join(UserJoinRequestDTO req) {
        // 1) 중복 체크
        if (userRepository.existsByUserId(req.userId())) {
            return UserJoinResponseDTO.failure("DUPLICATE_USER_ID");
        }

        // 2) 도메인 생성 (비밀번호 해시)
        User toSave = User.create(
                req.userId(),
                passwordEncoder.encode(req.password()),
                req.userName(),
                req.role()
        );

        // 3) 저장
        User saved = userRepository.save(toSave);

        // 4) 응답
        return UserJoinResponseDTO.success(
                saved.getUserId(),
                saved.getUserName()
        );
    }

}
