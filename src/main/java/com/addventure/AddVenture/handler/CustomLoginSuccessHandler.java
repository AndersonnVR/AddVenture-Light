package com.addventure.AddVenture.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Esta clase maneja el evento de éxito de autenticación y actualiza el último ingreso del usuario en la BD
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Este método se llama cuando la autenticación es exitosa
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String correo = authentication.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        usuarioOpt.ifPresent(usuario -> {
            usuario.setUltimoIngreso(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });

        // Redirige al usuario a su perfil después de iniciar sesión
        response.sendRedirect("/perfil");
    }
}
