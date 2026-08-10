package com.usermanagement.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOtpRepository extends JpaRepository<OtpJpaEntity, UUID> {
    Optional<OtpJpaEntity> findByUserIdAndToken(UUID userId, String token);

    @Transactional
    void deleteByUserId(UUID userId);
}
