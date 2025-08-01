package com.addventure.AddVenture.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.UsuarioRepository;

//Esta clase se encarga de agregar el usuario autenticado al modelo para que esté disponible en todas las vistas
@ControllerAdvice
public class GlobalControllerAdvice {

    // Inyectamos el repositorio de Usuario para poder acceder a los datos del usuario autenticado
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Este método se ejecuta antes de cada controlador y agrega el usuario autenticado al modelo
    @ModelAttribute
    public void agregarUsuarioAlModelo(Model model, Principal principal) {
        if (principal != null) {
            String correo = principal.getName();
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
            model.addAttribute("usuarioPrincipal", usuario);
        }
    }
}
