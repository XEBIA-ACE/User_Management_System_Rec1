package com.usermanagement.domain.port.out;

import java.time.Duration;
import java.util.Optional;

public interface TokenCachePort {
    void store(String key, String value, Duration ttl);
    Optional<String> retrieve(String key);
    void invalidate(String key);
}
