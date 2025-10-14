package com.investment.metal.infrastructure.encryption;

interface MultipleKeysTransformation {

    char call(String key1, String key2, char character);

}
