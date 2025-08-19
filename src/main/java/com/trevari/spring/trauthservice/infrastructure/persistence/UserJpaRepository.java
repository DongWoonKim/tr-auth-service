package com.trevari.spring.trauthservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUserId(String userId);
    Optional<UserEntity> findByUserId(String userId);
}
