package com.trevari.spring.trauthservice.domain.user;

import java.util.Optional;

public interface UserRepository {
    boolean existsByUserId(String userId);
    Optional<User> findByUserId(String userId);
    User save(User user);
}
