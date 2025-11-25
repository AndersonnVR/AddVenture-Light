package com.addventure.AddVenture.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.UsuarioRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @ModelAttribute
    public void agregarUsuarioAlModelo(Model model, Principal principal) {
        if (principal != null) {
            String correo = principal.getName();
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
            model.addAttribute("usuarioPrincipal", usuario);
        }
    }
}
