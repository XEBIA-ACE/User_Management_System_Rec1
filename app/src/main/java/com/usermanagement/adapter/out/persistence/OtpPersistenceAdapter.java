package com.usermanagement.adapter.out.persistence;

import com.usermanagement.domain.model.OtpToken;
import com.usermanagement.domain.port.out.OtpRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OtpPersistenceAdapter implements OtpRepository {

    private final SpringDataOtpRepository springDataOtpRepository;

    public OtpPersistenceAdapter(SpringDataOtpRepository springDataOtpRepository) {
        this.springDataOtpRepository = springDataOtpRepository;
    }

    @Override
    public OtpToken save(OtpToken otpToken) {
        OtpJpaEntity entity = toEntity(otpToken);
        OtpJpaEntity saved = springDataOtpRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OtpToken> findByUserIdAndToken(UUID userId, String token) {
        return springDataOtpRepository.findByUserIdAndToken(userId, token).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataOtpRepository.deleteByUserId(userId);
    }

    private OtpJpaEntity toEntity(OtpToken o) {
        OtpJpaEntity e = new OtpJpaEntity();
        e.setId(o.getId());
        e.setUserId(o.getUserId());
        e.setToken(o.getToken());
        e.setExpiresAt(o.getExpiresAt());
        e.setUsed(o.isUsed());
        e.setCreatedAt(o.getCreatedAt());
        return e;
    }

    private OtpToken toDomain(OtpJpaEntity e) {
        return new OtpToken(
                e.getId(),
                e.getUserId(),
                e.getToken(),
                e.getExpiresAt(),
                e.isUsed(),
                e.getCreatedAt()
        );
    }
}
