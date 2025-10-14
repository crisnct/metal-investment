package com.investment.metal.application.repository.impl;

import com.investment.metal.application.repository.TokenRepository;
import com.investment.metal.domain.valueobject.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Implementation of TokenRepository.
 * In-memory implementation for demonstration purposes.
 * In production, this would use a database.
 */
@Slf4j
@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final Map<String, Token> tokenStore = new ConcurrentHashMap<>();

    @Override
    public Optional<Token> findByValue(String value) {
        return Optional.ofNullable(tokenStore.get(value));
    }

    @Override
    public Token save(Token token) {
        tokenStore.put(token.getValue(), token);
        log.debug("Token saved: {}", token.getValue());
        return token;
    }

    @Override
    public void deleteByValue(String value) {
        tokenStore.remove(value);
        log.debug("Token deleted: {}", value);
    }

    @Override
    public void deleteExpiredTokens() {
        tokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("Expired tokens cleaned up");
    }

    @Override
    public Optional<Token> findByUserIdAndType(Integer userId, Token.TokenType type) {
        return tokenStore.values().stream()
            .filter(token -> token.getUserId().equals(userId) && token.getType().equals(type))
            .findFirst();
    }
}
