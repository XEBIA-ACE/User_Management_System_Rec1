package com.usermanagement.adapter.out.persistence;

import com.usermanagement.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
