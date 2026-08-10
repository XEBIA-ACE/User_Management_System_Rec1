package com.usermanagement.infrastructure.persistence.adapter;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.port.out.OtpRepository;
import com.usermanagement.infrastructure.persistence.entity.OtpTokenEntity;
import com.usermanagement.infrastructure.persistence.mapper.OtpEntityMapper;
import com.usermanagement.infrastructure.persistence.repository.JpaOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OtpRepositoryAdapter implements OtpRepository {

    private final JpaOtpRepository jpaOtpRepository;
    private final OtpEntityMapper mapper;

    @Override
    public OtpToken save(OtpToken token) {
        OtpTokenEntity entity = mapper.toEntity(token);
        return mapper.toDomain(jpaOtpRepository.save(entity));
    }

    @Override
    public Optional<OtpToken> findByUserIdAndCode(UUID userId, String code) {
        return jpaOtpRepository.findByUserIdAndCode(userId, code).map(mapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaOtpRepository.deleteByUserId(userId);
    }
}
