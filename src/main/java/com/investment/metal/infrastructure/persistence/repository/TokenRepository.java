package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.domain.valueobject.Token;
import java.util.Optional;

/**
 * Repository interface for Token value objects.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface TokenRepository {
    
    /**
     * Find token by value
     */
    Optional<Token> findByValue(String value);
    
    /**
     * Save token
     */
    Token save(Token token);
    
    /**
     * Delete token by value
     */
    void deleteByValue(String value);
    
    /**
     * Delete expired tokens
     */
    void deleteExpiredTokens();
    
    /**
     * Find token by user ID and type
     */
    Optional<Token> findByUserIdAndType(Integer userId, Token.TokenType type);
}
