package com.cola.attendance.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单内存 Token 存储（生产建议改为 Redis）。
 */
public class TokenStore {
    private static final Map<String, LoginUser> STORE = new ConcurrentHashMap<>();

    public static void put(String token, LoginUser user) {
        STORE.put(token, user);
    }

    public static LoginUser get(String token) {
        return STORE.get(token);
    }

    public static void remove(String token) {
        STORE.remove(token);
    }
}
