package com.bookstore.security.token;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public static String generateJti() {
        return UUID.randomUUID().toString();
    }

    public static String generateRandomToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return BASE64_ENCODER.encodeToString(tokenBytes);
    }
}
