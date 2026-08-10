package com.usermanagement.infrastructure.persistence.mapper;

import com.usermanagement.domain.model.User;
import com.usermanagement.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(User user);

    User toDomain(UserEntity entity);
}
