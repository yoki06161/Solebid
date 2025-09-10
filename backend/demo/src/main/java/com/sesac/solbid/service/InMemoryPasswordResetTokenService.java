package com.sesac.solbid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("!redis")
public class InMemoryPasswordResetTokenService implements PasswordResetTokenService {

    private final long ttlSeconds;
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public InMemoryPasswordResetTokenService(@Value("${password.reset.token-valid-seconds:900}") long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public String createToken(String email) {
        cleanup();
        String token = UUID.randomUUID().toString().replace("-", "");
        store.put(token, new Entry(email, Instant.now().plusSeconds(ttlSeconds)));
        return token;
    }

    @Override
    public String consumeToken(String token) {
        cleanup();
        Entry e = store.remove(token);
        if (e == null) return null;
        if (e.expiry.isBefore(Instant.now())) return null;
        return e.email;
    }

    @Override
    public boolean validateToken(String token) {
        cleanup();
        Entry e = store.get(token);
        return e != null && e.expiry.isAfter(Instant.now());
    }

    @Override
    public String getEmailIfValid(String token) {
        cleanup();
        Entry e = store.get(token);
        if (e == null) return null;
        return e.expiry.isAfter(Instant.now()) ? e.email : null;
    }

    private void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(en -> en.getValue().expiry.isBefore(now));
    }

    private record Entry(String email, Instant expiry) {}
}
