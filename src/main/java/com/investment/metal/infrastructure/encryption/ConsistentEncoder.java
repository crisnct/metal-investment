package com.investment.metal.infrastructure.encryption;

/**
 * Infrastructure interface for encryption operations.
 * This interface is used by infrastructure implementations.
 */
public interface ConsistentEncoder {

    String encrypt(String value);

    String decrypt(String value);

}
