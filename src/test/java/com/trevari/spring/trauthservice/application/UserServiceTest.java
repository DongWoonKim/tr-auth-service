package com.trevari.spring.trauthservice.application;

import com.trevari.spring.trauthservice.domain.user.Role;
import com.trevari.spring.trauthservice.domain.user.User;
import com.trevari.spring.trauthservice.domain.user.UserRepository;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinRequestDTO;
import com.trevari.spring.trauthservice.interfaces.dto.UserJoinResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    private UserService userService;
    private FakeUserRepository fakeRepo;
    private FakePasswordEncoder fakeEncoder;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeUserRepository();
        fakeEncoder = new FakePasswordEncoder();
        userService = new UserService(fakeRepo, fakeEncoder);

        // 초기 데이터 1건
        fakeRepo.save(
                User.create(
                        "existUser",
                        fakeEncoder.encode("pw!"),
                        "기존유저",
                        Role.ROLE_USER
                )
        );
    }

    @Test
    void 회원가입_성공_테스트() {
        // given
        var req = new UserJoinRequestDTO("user01", "PlainPw!234", "홍길동", Role.ROLE_USER);

        // when
        UserJoinResponseDTO res = userService.join(req);

        // then
        assertThat(res.success()).isTrue();
        assertThat(res.userId()).isEqualTo("user01");
        assertThat(res.userName()).isEqualTo("홍길동");

        // 저장된 사용자 검증
        var savedOpt = fakeRepo.findByUserId("user01");
        assertThat(savedOpt).isPresent();
        var saved = savedOpt.get();
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_USER);

        // 평문이 아닌 해시(여기선 ENC: 접두)로 저장됐는지 확인
        assertThat(saved.getPassword()).startsWith("ENC:");
        assertThat(fakeEncoder.matches("PlainPw!234", saved.getPassword())).isTrue();
    }

    @Test
    void 회원가입_중복아이디_실패_테스트() {
        // given
        var req = new UserJoinRequestDTO("existUser", "pw", "아무개", Role.ROLE_USER);

        // when
        UserJoinResponseDTO res = userService.join(req);

        // then
        assertThat(res.success()).isFalse();
        assertThat(res.message()).isEqualTo("DUPLICATE_USER_ID");
        // 저장 안 됨
        assertThat(fakeRepo.count()).isEqualTo(1);
    }


    /** 인메모리 Fake Repository */
    static class FakeUserRepository implements UserRepository {
        private final Map<String, User> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(0);

        @Override
        public boolean existsByUserId(String userId) {
            return store.containsKey(userId);
        }

        @Override
        public Optional<User> findByUserId(String userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public User save(User user) {
            Long id = user.getId() != null ? user.getId() : seq.incrementAndGet();
            // 도메인 재구성(저장 후 id 채워진 객체 반환)
            User saved = User.reconstruct(
                    id,
                    user.getUserId(),
                    user.getPassword(),
                    user.getUserName(),
                    user.getRole()
            );
            store.put(saved.getUserId(), saved);
            return saved;
        }

        long count() { return store.size(); }
    }

    @Test
    void 회원가입_ROLE_ADMIN_저장_테스트() {
        // given
        var req = new UserJoinRequestDTO("admin01", "Secret!", "관리자", Role.ROLE_ADMIN);

        // when
        var res = userService.join(req);

        // then
        assertThat(res.success()).isTrue();

        var saved = fakeRepo.findByUserId("admin01").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }
    /** 간단한 Fake PasswordEncoder (접두사 ENC:) */
    static class FakePasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "ENC:" + rawPassword; // 테스트용 단순 변환
        }
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword.equals(encode(rawPassword));
        }
    }
}