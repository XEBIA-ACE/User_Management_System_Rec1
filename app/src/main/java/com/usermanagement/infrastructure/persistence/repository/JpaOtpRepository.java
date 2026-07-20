package com.usermanagement.infrastructure.persistence.repository;

import com.usermanagement.infrastructure.persistence.entity.OtpTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaOtpRepository extends JpaRepository<OtpTokenEntity, UUID> {

    Optional<OtpTokenEntity> findByUserIdAndCode(UUID userId, String code);

    void deleteByUserId(UUID userId);
}
