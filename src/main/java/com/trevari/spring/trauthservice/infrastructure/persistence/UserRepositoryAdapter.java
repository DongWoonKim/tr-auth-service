package com.trevari.spring.trauthservice.infrastructure.persistence;

import com.trevari.spring.trauthservice.domain.user.User;
import com.trevari.spring.trauthservice.domain.user.UserRepository;
import com.trevari.spring.trauthservice.interfaces.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        UserEntity saved = jpa.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return jpa.findByUserId(userId)
                .map(mapper::toDomain);
    }

}
