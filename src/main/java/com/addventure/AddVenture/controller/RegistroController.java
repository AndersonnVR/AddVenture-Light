package com.addventure.AddVenture.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.addventure.AddVenture.dto.RegistroUsuarioDTO;
import com.addventure.AddVenture.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

<<<<<<< Updated upstream
    // Este método muestra el formulario de registro cuando el usuario no está autenticado
    @GetMapping
=======
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/registro")
>>>>>>> Stashed changes
    public String mostrarFormularioRegistro(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/perfil";
        }
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    // Este método maneja el registro de nuevos usuarios
    // Utiliza @ModelAttribute para vincular el formulario al DTO y @Valid para validar los datos
    @PostMapping
    public String registrarUsuario(
            @ModelAttribute("usuario") @Valid RegistroUsuarioDTO usuarioDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Validaciones personalizadas
        if (usuarioService.existeCorreo(usuarioDTO.getCorreo())) {
            result.rejectValue("correo", "error.usuario", "Este correo ya está registrado");
        }
        if (usuarioService.existeNombreUsuario(usuarioDTO.getNombreUsuario())) {
            result.rejectValue("nombreUsuario", "error.usuario", "Este nombre de usuario ya está en uso");
        }

        if (result.hasErrors()) {
            return "registro"; // retorna con errores
        }

        usuarioService.registrarUsuario(usuarioDTO);

        redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado correctamente");
        return "redirect:/login";
    }
}