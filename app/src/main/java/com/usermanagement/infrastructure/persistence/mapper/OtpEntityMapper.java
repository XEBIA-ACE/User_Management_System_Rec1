package com.usermanagement.infrastructure.persistence.mapper;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.infrastructure.persistence.entity.OtpTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OtpEntityMapper {

    OtpTokenEntity toEntity(OtpToken token);

    OtpToken toDomain(OtpTokenEntity entity);
}
