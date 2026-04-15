package com.example.agent.common.utils;

import java.util.UUID;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
