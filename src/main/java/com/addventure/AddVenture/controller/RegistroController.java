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

// Esta clase maneja las peticiones relacionadas con el registro de nuevos usuarios
@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    // Este método muestra el formulario de registro cuando el usuario no está autenticado
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/perfil";
        }
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    // Este método maneja el registro de nuevos usuarios
    // Utiliza @ModelAttribute para vincular el formulario al DTO y @Valid para validar los datos
    @PostMapping("/registro")
    public String iniciarRegistro(
            @ModelAttribute("usuario") @Valid RegistroUsuarioDTO usuarioDTO,
            BindingResult result,
            HttpSession session,
            Model model) {

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

        String nombreImagen = usuarioService.guardarImagenTemporal(usuarioDTO.getFotoPerfil());

        String codigo = String.valueOf((int) (Math.random() * 900000) + 100000);

        usuarioDTO.setFotoPerfil(null); 
        
        session.setAttribute("usuarioTemporal", usuarioDTO);
        session.setAttribute("nombreImagenTemporal", nombreImagen);
        session.setAttribute("codigoVerificacion", codigo);
        session.setAttribute("tiempoInicioCodigo", LocalDateTime.now());

        emailService.enviarCorreoVerificacion(usuarioDTO.getCorreo(), codigo);
        
        System.out.println("Código enviado: " + codigo); // Para pruebas en consola

        return "redirect:/verificar";
    }

    //Mostrar vista de verificación
    @GetMapping("/verificar")
    public String mostrarVerificacion(HttpSession session, Model model) {
        RegistroUsuarioDTO usuarioTmp = (RegistroUsuarioDTO) session.getAttribute("usuarioTemporal");
        
        // Si no hay usuario en sesión, devolver al registro (seguridad)
        if (usuarioTmp == null) {
            return "redirect:/registro";
        }

        model.addAttribute("correoOculto", enmascararCorreo(usuarioTmp.getCorreo()));
        return "verificar"; // Retorna verificar.html
    }

    // Procesar el código ingresado
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
            return "redirect:/registro"; // Sesión expirada
        }

        long minutosTranscurridos = Duration.between(tiempoInicio, LocalDateTime.now()).toMinutes();

        if (minutosTranscurridos > 10) {
            model.addAttribute("error", "El código ha expirado. Por favor, solicita uno nuevo.");
            model.addAttribute("correoOculto", enmascararCorreo(usuarioDTO.getCorreo()));
            return "verificar"; // Se queda en la misma pantalla
        }

        if (codigoReal.equals(codigoIngresado)) {
            // ¡CÓDIGO CORRECTO! -> Guardar en Base de Datos
            usuarioService.registrarUsuarioFinal(usuarioDTO, nombreImagen);

            // Limpiar sesión
            session.removeAttribute("usuarioTemporal");
            session.removeAttribute("codigoVerificacion");
            session.removeAttribute("nombreImagenTemporal");

            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta verificada! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } else {
            // CÓDIGO INCORRECTO
            model.addAttribute("error", "Código incorrecto, inténtalo de nuevo.");
            model.addAttribute("correoOculto", enmascararCorreo(usuarioDTO.getCorreo()));
            return "verificar"; // Volver a mostrar la vista con error
        }
    }

    // Utilidad para mostrar el correo parcialmente (ej: j***@gmail.com)
    private String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) return correo;
        String[] partes = correo.split("@");
        if (partes[0].length() <= 2) return correo;
        return partes[0].substring(0, 2) + "***@" + partes[1];
    }

    @GetMapping("/reenviar-codigo")
    public String reenviarCodigo(HttpSession session, RedirectAttributes redirectAttributes) {
         RegistroUsuarioDTO usuarioTmp = (RegistroUsuarioDTO) session.getAttribute("usuarioTemporal");
         
         // Validación: Si la sesión expiró, mandar al registro
         if (usuarioTmp == null) {
             return "redirect:/registro";
         }

         // Generar nuevo código
         String nuevoCodigo = String.valueOf((int) (Math.random() * 900000) + 100000);
         session.setAttribute("codigoVerificacion", nuevoCodigo);
         session.setAttribute("tiempoInicioCodigo", LocalDateTime.now());
         
         System.out.println("Reenviando código: " + nuevoCodigo); // Debug
         
         // Enviar el nuevo correo bonito
         try {
             emailService.enviarCorreoVerificacion(usuarioTmp.getCorreo(), nuevoCodigo);
             redirectAttributes.addFlashAttribute("mensaje", "✅ Nuevo código enviado a tu correo.");
         } catch (Exception e) {
             redirectAttributes.addFlashAttribute("error", "Error al enviar el correo. Intenta nuevamente.");
         }
         
         return "redirect:/verificar";
    }
}