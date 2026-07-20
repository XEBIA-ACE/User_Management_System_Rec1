package com.usermanagement.domain.port.out;

import com.usermanagement.domain.model.User;

/**
 * Outbound port — JWT token generation and validation.
 */
public interface TokenPort {

    /**
     * Generate a signed JWT for the given user.
     *
     * @param user the authenticated user
     * @return signed JWT string
     */
    String generateToken(User user);

    /**
     * Extract the subject (user ID) from a JWT.
     *
     * @param token the JWT string
     * @return the subject claim value
     */
    String extractSubject(String token);

    /**
     * Validate a JWT token.
     *
     * @param token the JWT string
     * @return true if valid, false otherwise
     */
    boolean isValid(String token);
}
