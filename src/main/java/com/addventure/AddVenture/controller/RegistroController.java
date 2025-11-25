package com.addventure.AddVenture.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.addventure.AddVenture.dto.RegistroUsuarioDTO;
import com.addventure.AddVenture.service.EmailService;
import com.addventure.AddVenture.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;
    
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/perfil";
        }
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String iniciarRegistro(
            @ModelAttribute("usuario") @Valid RegistroUsuarioDTO usuarioDTO,
            BindingResult result,
            HttpSession session,
            Model model) {
        
        if (usuarioService.existeCorreo(usuarioDTO.getCorreo())) {
            result.rejectValue("correo", "error.usuario", "Este correo ya está registrado");
        }
        if (usuarioService.existeNombreUsuario(usuarioDTO.getNombreUsuario())) {
            result.rejectValue("nombreUsuario", "error.usuario", "Este nombre de usuario ya está en uso");
        }

        if (result.hasErrors()) {
            return "registro";
        }

        String nombreImagen = usuarioService.guardarImagenTemporal(usuarioDTO.getFotoPerfil());

        String codigo = String.valueOf((int) (Math.random() * 900000) + 100000);

        usuarioDTO.setFotoPerfil(null); 
        
        session.setAttribute("usuarioTemporal", usuarioDTO);
        session.setAttribute("nombreImagenTemporal", nombreImagen);
        session.setAttribute("codigoVerificacion", codigo);
        session.setAttribute("tiempoInicioCodigo", LocalDateTime.now());

        emailService.enviarCorreoVerificacion(usuarioDTO.getCorreo(), codigo);
        
        System.out.println("Código enviado: " + codigo);

        return "redirect:/verificar";
    }

    @GetMapping("/verificar")
    public String mostrarVerificacion(HttpSession session, Model model) {
        RegistroUsuarioDTO usuarioTmp = (RegistroUsuarioDTO) session.getAttribute("usuarioTemporal");
        
        if (usuarioTmp == null) {
            return "redirect:/registro";
        }

        model.addAttribute("correoOculto", enmascararCorreo(usuarioTmp.getCorreo()));
        return "verificar";
    }

    @PostMapping("/verificar")
    public String verificarCodigo(
            @RequestParam("codigo") String codigoIngresado,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String codigoReal = (String) session.getAttribute("codigoVerificacion");
        RegistroUsuarioDTO usuarioDTO = (RegistroUsuarioDTO) session.getAttribute("usuarioTemporal");
        String nombreImagen = (String) session.getAttribute("nombreImagenTemporal");
        LocalDateTime tiempoInicio = (LocalDateTime) session.getAttribute("tiempoInicioCodigo");

        if (usuarioDTO == null || codigoReal == null) {
            return "redirect:/registro";
        }

        long minutosTranscurridos = Duration.between(tiempoInicio, LocalDateTime.now()).toMinutes();

        if (minutosTranscurridos > 10) {
            model.addAttribute("error", "El código ha expirado. Por favor, solicita uno nuevo. 🔁");
            model.addAttribute("correoOculto", enmascararCorreo(usuarioDTO.getCorreo()));
            return "verificar";
        }

        if (codigoReal.equals(codigoIngresado)) {
            usuarioService.registrarUsuarioFinal(usuarioDTO, nombreImagen);

            session.removeAttribute("usuarioTemporal");
            session.removeAttribute("codigoVerificacion");
            session.removeAttribute("nombreImagenTemporal");

            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta verificada! Ya puedes iniciar sesión. 😄");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Código incorrecto, inténtalo de nuevo. 🚨");
            model.addAttribute("correoOculto", enmascararCorreo(usuarioDTO.getCorreo()));
            return "verificar";
        }
    }

    private String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) return correo;
        String[] partes = correo.split("@");
        if (partes[0].length() <= 2) return correo;
        return partes[0].substring(0, 2) + "***@" + partes[1];
    }

    @GetMapping("/reenviar-codigo")
    public String reenviarCodigo(HttpSession session, RedirectAttributes redirectAttributes) {
         RegistroUsuarioDTO usuarioTmp = (RegistroUsuarioDTO) session.getAttribute("usuarioTemporal");

         if (usuarioTmp == null) {
             return "redirect:/registro";
         }

         String nuevoCodigo = String.valueOf((int) (Math.random() * 900000) + 100000);
         session.setAttribute("codigoVerificacion", nuevoCodigo);
         session.setAttribute("tiempoInicioCodigo", LocalDateTime.now());
         
         System.out.println("Reenviando código: " + nuevoCodigo); // Debug
         
         try {
             emailService.enviarCorreoVerificacion(usuarioTmp.getCorreo(), nuevoCodigo);
             redirectAttributes.addFlashAttribute("mensaje", "Nuevo código enviado a tu correo. ✅");
         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("error", "Error al enviar el correo. Intenta nuevamente. 🔁");
         }
         
         return "redirect:/verificar";
    }
}