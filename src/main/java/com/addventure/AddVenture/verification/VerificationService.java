package com.addventure.AddVenture.verification;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/**
 * Servicio para generar, almacenar temporalmente y verificar códigos de un solo
 * uso (OTP)
 * asociados a una dirección de email.
 *
 * Comportamiento principal:
 * - Genera códigos numéricos de longitud configurable (p. ej. 6 dígitos).
 * - Guarda cada código en memoria (ConcurrentHashMap) junto con su instante de
 * expiración.
 * - Provee verificación del código; si la verificación es exitosa, el código se
 * invalida (uso único).
 * - Realiza limpieza periódica de entradas expiradas mediante un
 * ScheduledExecutorService.
 *
 * NOTAS IMPORTANTES:
 * - Implementación IN-MEMORY: NO es adecuada para entornos con múltiples
 * instancias
 * (usar Redis u otro store distribuido con TTL en producción).
 * - No se debe loguear nunca el código en logs de producción.
 */

@Service
public class VerificationService {

    /**
     * Mapa que contiene (clave=email en minúsculas) -> CodeEntry (código + expiry).
     * ConcurrentHashMap garantiza comportamiento seguro en concurrencia simple de
     * lectura/escritura.
     */
    private final Map<String, CodeEntry> storage = new ConcurrentHashMap<>();

    /**
     * Hilo único encargado de ejecutar la tarea periódica de limpieza (eliminar
     * expirados).
     * Se usa un único hilo porque la tarea de limpieza es ligera y evita
     * sincronizaciones complejas.
     */
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    /**
     * SecureRandom para generar números aleatorios con buena entropía (más seguro
     * que Random).
     */
    private final SecureRandom random = new SecureRandom();

    /**
     * Longitud del código (p. ej. 6). Se inyecta desde properties con valor por
     * defecto 6.
     */
    private final int codeLength;

    /**
     * Tiempo de vida (TTL) del código expresado como Duration (ej. 10 minutos).
     */
    private final Duration ttl;

    /**
     * Constructor inyectado por Spring.
     *
     * @param codeLength número de dígitos del código (propiedad
     *                   app.verification.code-length, default 6)
     * @param ttlMinutes tiempo de expiración en minutos (propiedad
     *                   app.verification.ttl-minutes, default 10)
     */
    public VerificationService(@Value("${app.verification.code-length:6}") int codeLength,
            @Value("${app.verification.ttl-minutes:10}") int ttlMinutes) {
        // Guardar configuración y convertir minutos a Duration
        this.codeLength = Math.max(1, codeLength); // proteger contra valores inválidos (>=1)
        this.ttl = Duration.ofMinutes(Math.max(1, ttlMinutes)); // proteger contra ttl <= 0

        // Programar la tarea de limpieza: inicia en 1 minuto y se repite cada 1 minuto.
        // Razonamiento: limpiar con frecuencia evita acumulación en memoria y mantiene
        // el mapa pequeño.
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Genera un código numérico de longitud configurada, lo almacena asociado al
     * email y devuelve el código.
     *
     * Decisiones importantes:
     * - La clave en el storage es el email en minúsculas (normalización para evitar
     * duplicates por mayúsculas).
     * - Si ya existía un código para ese email, se sobrescribe con el nuevo
     * (comportamiento intencional:
     * el último código enviado es el válido).
     *
     * @param email dirección de correo del usuario (se normaliza a minúsculas).
     * @return el código generado (por lo general se enviará por email; no
     *         retornarlo al cliente en producción).
     * @throws IllegalArgumentException si email es null o vacío.
     */
    public String generateAndStoreCode(String email) {
        Objects.requireNonNull(email, "email no puede ser null");
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("email no puede estar vacío");
        }

        // Generar codigo y calcular instante de expiración.
        String code = generateNumericCode(codeLength);
        Instant expiry = Instant.now().plus(ttl);

        // Guardar en el mapa (reemplaza cualquier entrada previa)
        storage.put(email.toLowerCase(), new CodeEntry(code, expiry));

        // DEVUELVE el codigo para que el servicio llamador lo envie por correo.
        return code;
    }

    /**
     * Verifica si el código proporcionado es válido para el email dado.
     *
     * Flujo:
     * - Recupera la entrada desde storage.
     * - Si no existe, devuelve false.
     * - Si existe pero expiró, la elimina y devuelve false.
     * - Si coincide, elimina la entrada (uso único) y devuelve true.
     *
     * @param email email a verificar (se normaliza a minúsculas)
     * @param code  código que quiere verificarse
     * @return true si el código es correcto y vigente; false en caso contrario
     */
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null)
            return false;
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty() || code.isEmpty())
            return false;

        CodeEntry entry = storage.get(normalized);
        if (entry == null)
            // No hay código almacenado para ese email
            return false;

        // Si expiró, borramos y devolvemos false.
        if (Instant.now().isAfter(entry.expiry)) {
            storage.remove(normalized);
            return false;
        }
        // Comparacion simple: si coincide, invalidamos (un solo uso) y devolvemos true
        boolean match = entry.code.equals(code);
        if (match)
            storage.remove(normalized); // un solo uso
        return match;
    }

    /**
     * Tarea periódica para eliminar entradas expiradas del mapa.
     * Ejecutada por el ScheduledExecutorService cada minuto.
     *
     * Implementación: removeIf sobre entrySet es seguro en ConcurrentHashMap.
     */
    private void cleanup() {
        Instant now = Instant.now();
        storage.entrySet().removeIf(e -> e.getValue().expiry.isBefore(now));
    }

    /**
     * Genera un código numérico de longitud 'length'.
     * - Usa SecureRandom para mejor entropía.
     * - Formatea con ceros a la izquierda para garantizar longitud constante (ej.
     * "000123").
     *
     * Consideraciones:
     * - Para length demasiado grande (>=10), el cálculo int max = (int)Math.pow(10,
     * length) puede overflowear.
     * Por eso el constructor limita codeLength a valores razonables (1..9) si lo
     * deseas.
     *
     * @param length número de dígitos
     * @return string con el código formateado
     */
    private String generateNumericCode(int length) {

        // Seguridad: asegurar un length al menos 1 y limitar a 9 para evitar overflow
        // int (opcional).
        int safeLength = Math.max(1, Math.min(9, length)); // ajuste opcional
        int max = (int) Math.pow(10, safeLength); // 10^length
        int num = random.nextInt(max); // rango [0, max-1]
        return String.format("%0" + safeLength + "d", num);
    }

    /**
     * Clase interna inmutable que representa una entrada almacenada: código +
     * tiempo de expiración.
     * Fields final -> inmutabilidad y seguridad en concurrencia.
     */
    private static class CodeEntry {
        final String code;
        final Instant expiry;

        CodeEntry(String code, Instant expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }

    /**
     * Método invocado por el contenedor en el shutdown del bean para cerrar el scheduler
     * y evitar que queden hilos en ejecución (resource leak).
     */
    @PreDestroy
    public void shutdown() {
        cleaner.shutdown();
        // opcional: esperar un tiempo y forzar shutdownNow si no termina.
        // try { cleaner.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }
}

// ========== Opcion: Redis ============
/*
 * En producción conviene usar Redis con TTL para soporte multi-instancia.
 * - Injectar RedisTemplate<String, String>
 * - Almacenar clave: "verification:{email}" -> value code, y poner expire(ttl)
 * - verify: get clave y compare, y delete en verificación exitosa
 */