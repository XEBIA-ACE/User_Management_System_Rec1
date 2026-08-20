package com.usermanagement.adapter.out.persistence;

import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}
