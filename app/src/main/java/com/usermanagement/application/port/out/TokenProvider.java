package com.usermanagement.application.port.out;

/**
 * Outbound port — JWT token operations.
 */
public interface TokenProvider {
    String generateToken(String subject, java.util.Map<String, Object> claims);
    boolean validateToken(String token);
    String extractSubject(String token);
}
