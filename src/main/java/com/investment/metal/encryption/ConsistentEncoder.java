package com.investment.metal.encryption;

public interface ConsistentEncoder {

    String encrypt(String value);

    String decrypt(String value);

}
