package com.hdfclife.auth_service.auth_service.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTokenStore {
    private final Set<String> validTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addToken(String token) {
        validTokens.add(token);
    }

    public boolean isTokenValid(String token) {
        return validTokens.contains(token);
    }

    public void invalidateToken(String token) {
        validTokens.remove(token);
    }
}