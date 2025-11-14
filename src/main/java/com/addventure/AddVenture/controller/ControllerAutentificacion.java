package com.addventure.AddVenture.controller;

import java.util.Map;

import com.addventure.AddVenture.email.EmailService;
import com.addventure.AddVenture.ratelimit.RateLimiterService;
import com.addventure.AddVenture.verification.VerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Controlador REST encargado de:
 * - Enviar códigos de verificación por correo
 * - Validar dichos códigos
 * 
 * Es parte del sistema de autenticación sin contraseña (passwordless login).
 * Usa rate limiting, envío de email y almacenamiento temporal del código.
 */
@RestController
@RequestMapping("/api/auth")
public class ControllerAutentificacion {

    private final EmailService emailService; // Servicio de envío de correos
    private final VerificationService verificationService; // Genera y valida códigos
    private final RateLimiterService rateLimiter; // Limita solicitudes por email

    /**
     * Spring inyecta los servicios necesarios por constructor.
     */
    public ControllerAutentificacion(EmailService emailService, VerificationService verificationService,
            RateLimiterService rateLimiter) {
        this.emailService = emailService;
        this.verificationService = verificationService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * DTO para recibir solicitudes de envío de código.
     * Incluye validaciones automáticas.
     */
    public static record SendCodeRequest(@NotBlank @Email String email) {
    }

    /**
     * DTO para recibir solicitudes de verificación del código.
     */
    public static record VerifyCodeRequest(@NotBlank @Email String email, @NotBlank String code) {
    }

    /**
     * Endpoint: POST /api/auth/send-code
     * 
     * - Aplica rate limiting
     * - Genera un código temporal
     * - Lo envía al correo del usuario
     * - Devuelve respuesta genérica por seguridad
     */
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest req) {

        String email = req.email().toLowerCase();

        // Rate limiting
        // Limitar la cantidad de intentos permitidos por hora
        if (!rateLimiter.tryConsume(email)) {
            return ResponseEntity.status(429).body(Map.of("message", "Demasiadas solicitudes. Intenta más tarde."));
        }

        // Generar y almacenar el código temporal
        String code = verificationService.generateAndStoreCode(email);

        // Enviar el código por email (HTML + fallback de texto)
        emailService.sendVerificationCode(email, code, 10);

        // Respuesta estándar para evitar revelar si el email existe
        return ResponseEntity.ok(Map.of("message", "Código enviado si el correo existe"));
    }

    /**
     * Endpoint: POST /api/auth/verify-code
     * 
     * - Valida el código enviado por el usuario
     * - Si es correcto y no ha expirado → autenticación exitosa
     * - Si falla → 400 con mensaje
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        boolean ok = verificationService.verifyCode(req.email().toLowerCase(), req.code());
        if (ok) {
            return ResponseEntity.ok(Map.of("verified", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("verified", false, "message", "Código inválido o expirado"));
        }
    }
}
