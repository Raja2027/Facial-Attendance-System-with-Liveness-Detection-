package com.example.attendancesystem.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class AttendanceCacheService {

    private static final long TTL_SECONDS = 60;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public Optional<Map<String, Object>> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            cache.remove(key);
            return Optional.empty();
        }

        return Optional.of(entry.value());
    }

    public void put(String key, Map<String, Object> value) {
        cache.put(key, new CacheEntry(value, Instant.now().plusSeconds(TTL_SECONDS)));
    }

    private record CacheEntry(Map<String, Object> value, Instant expiresAt) {
    }
}
