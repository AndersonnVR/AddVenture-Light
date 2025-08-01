package com.addventure.AddVenture.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Esta clase maneja las peticiones relacionadas con la autenticación de usuarios
@Controller
public class AuthController {

    // Método para evitar el acceso a la página de login si el usuario ya está autenticado
    @GetMapping("/login")
    public String login() {
        if (estaAutenticado()) {
            return "redirect:/perfil";
        }
        return "login";
    }

    // Método para verificar si el usuario está autenticado
    private boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !auth.getAuthorities().toString().contains("ROLE_ANONYMOUS");
    }
}
