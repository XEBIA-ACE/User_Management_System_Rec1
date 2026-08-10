package com.usermanagement.adapter.out.persistence;

import com.usermanagement.domain.model.User;
import com.usermanagement.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserPersistenceAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.setId(user.getId());
        e.setEmail(user.getEmail());
        e.setPasswordHash(user.getPasswordHash());
        e.setFullName(user.getFullName());
        e.setVerified(user.isVerified());
        e.setActive(user.isActive());
        e.setCreatedAt(user.getCreatedAt());
        e.setUpdatedAt(user.getUpdatedAt());
        return e;
    }

    private User toDomain(UserJpaEntity e) {
        return new User(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getFullName(),
                e.isVerified(),
                e.isActive(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
