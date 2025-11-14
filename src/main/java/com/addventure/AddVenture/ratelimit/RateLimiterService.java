package com.addventure.AddVenture.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Servicio de rate limit que controla cuántas veces un usuario puede solicitar
 * un envío de código de verificación por correo en un periodo determinado.
 *
 * Implementación:
 * - Usa Bucket4j para asignar un "bucket" (depósito de tokens) por email.
 * - Cada bucket tiene un límite máximo de solicitudes por hora.
 * - Cada solicitud consume 1 token.
 * - El bucket se recarga automáticamente cada hora.
 *
 * Propósito:
 * - Evitar abuso (spam) del endpoint de envío de código.
 * - Evitar que un usuario fuerce múltiples envíos de email.
 */
@Service
public class RateLimiterService {

    /**
     * Mapa donde cada email (clave normalizada) tiene su Bucket.
     * ConcurrentHashMap es seguro para concurrent access dentro de una misma JVM.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Número máximo de solicitudes permitidas por hora para un mismo email.
     * Se lee desde application.propeties, ejemplo:
     * app.verification.max-sends-per-hour=5
     */
    private final int maxSendsPerHour;

    /** Constructor con inyección del límite configurado */
    public RateLimiterService(@Value("${app.verification.max-sends-per-hour:5}") int maxSendsPerHour) {
        this.maxSendsPerHour = maxSendsPerHour;
    }

    /**
     * Construye un bucket nuevo con:
     * - Capacidad: maxSendsPerHour tokens
     * - Recarga completa cada 1 hora
     */
    private Bucket newBucket() {

        // Cada hora se recargan completamente los tokens
        Refill refill = Refill.intervally(maxSendsPerHour, Duration.ofHours(1));

        // Bandwidth clásico: máximo de tokens y cómo se recargan
        Bandwidth limit = Bandwidth.classic(maxSendsPerHour, refill);

        // Crear el bucket con esa configuración
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Intenta consumir 1 token del bucket asociado al email.
     *
     * @return true si aún tiene tokens (permitido),
     *         false si ya agotó el límite horario.
     */
    public boolean tryConsume(String email) {
        // Normalizar email para evitar duplicación por mayúsculas
        String key = email.trim().toLowerCase();

        // Obtener bucket existente o crear uno nuevo si no existe
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        // Intentar consumir 1 token
        return bucket.tryConsume(1);
    }
}
