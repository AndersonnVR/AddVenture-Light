package com.addventure.AddVenture.verification;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class VerificationService {

    private final Map<String, CodeEntry> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private final SecureRandom random = new SecureRandom();
    private final int codeLength;
    private final Duration ttl;

    public VerificationService(@Value("${app.verification.code-length:6}") int codeLength,
            @Value("${app.verification.ttl-minutes:10}") int ttlMinutes) {
        this.codeLength = codeLength;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public String generateAndStoreCode(String email) {
        String code = generateNumericCode(codeLength);
        Instant expiry = Instant.now().plus(ttl);
        storage.put(email.toLowerCase(), new CodeEntry(code, expiry));
        return code;
    }

    public boolean verifyCode(String email, String code) {
        CodeEntry entry = storage.get(email.toLowerCase());
        if (entry == null)
            return false;
        if (Instant.now().isAfter(entry.expiry)) {
            storage.remove(email.toLowerCase());
            return false;
        }
        boolean match = entry.code.equals(code);
        if (match)
            storage.remove(email.toLowerCase()); // un solo uso
        return match;
    }

    private void cleanup() {
        Instant now = Instant.now();
        storage.entrySet().removeIf(e -> e.getValue().expiry.isBefore(now));
    }

    private String generateNumericCode(int length) {
        int max = (int) Math.pow(10, length);
        int num = random.nextInt(max);
        return String.format("%0" + length + "d", num);
    }

    private static class CodeEntry {
        final String code;
        final Instant expiry;

        CodeEntry(String code, Instant expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }

    // ========== Opcion: Redis ============
    /*
     * En producción conviene usar Redis con TTL para soporte multi-instancia.
     * - Injectar RedisTemplate<String, String>
     * - Almacenar clave: "verification:{email}" -> value code, y poner expire(ttl)
     * - verify: get clave y compare, y delete en verificación exitosa
     */
}
