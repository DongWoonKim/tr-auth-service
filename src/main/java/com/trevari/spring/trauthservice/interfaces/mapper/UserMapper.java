package com.trevari.spring.trauthservice.interfaces.mapper;

import com.trevari.spring.trauthservice.domain.user.Role;
import com.trevari.spring.trauthservice.domain.user.User;
import com.trevari.spring.trauthservice.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User u) {
        return UserEntity.builder()
                .userId(u.getUserId())
                .userName(u.getUserName())
                .password(u.getPassword())
                .role(u.getRole().name())
                .build();
    }

    public User toDomain(UserEntity userEntity) {
        return User.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .password(userEntity.getPassword())
                .role(Role.valueOf(userEntity.getRole()))
                .build();
    }

}
