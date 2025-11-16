package com.addventure.AddVenture.controller;

import java.util.Map;

import com.addventure.AddVenture.email.EmailService;
import com.addventure.AddVenture.ratelimit.RateLimiterService;
import com.addventure.AddVenture.repository.UsuarioRepository;
import com.addventure.AddVenture.verification.VerificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class ControllerAutentificacion {

    private final EmailService emailService;
    private final VerificationService verificationService;
    private final RateLimiterService rateLimiter;
    private final UsuarioRepository usuarioRepository;

    public ControllerAutentificacion(EmailService emailService, VerificationService verificationService,
            RateLimiterService rateLimiter, UsuarioRepository usuarioRepository) {
        this.emailService = emailService;
        this.verificationService = verificationService;
        this.rateLimiter = rateLimiter;
        this.usuarioRepository = usuarioRepository;
    }

    public static record SendCodeRequest(@NotBlank @Email String email) {
    }

    public static record VerifyCodeRequest(@NotBlank @Email String email, @NotBlank String code) {
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest req) {
        String email = req.email().toLowerCase();

        if (!rateLimiter.tryConsume(email)) {
            return ResponseEntity.status(429).body(Map.of("message", "Demasiadas solicitudes. Intenta más tarde."));
        }

        String code = verificationService.generateAndStoreCode(email);
        emailService.sendVerificationCode(email, code, 10);

        return ResponseEntity.ok(Map.of("message", "Código enviado si el correo existe"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest req,
                                        HttpSession session) {
        
        String email = req.email().toLowerCase();
        String code = req.code().trim();
        
        boolean ok = verificationService.verifyCode(email, code);

        if (ok) {
            // ✅ Marcar código como válido en la sesión
            session.setAttribute("codeValid", true);
            
            System.out.println("✅ Código verificado correctamente para: " + email);
            System.out.println("✅ Session ID: " + session.getId());
            System.out.println("✅ codeValid set to: " + session.getAttribute("codeValid"));
            
            return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Código verificado correctamente",
                "redirect", "/login"
            ));
        } else {
            System.out.println("❌ Código inválido para: " + email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "verified", false,
                "message", "Código inválido o expirado"
            ));
        }
    }
}