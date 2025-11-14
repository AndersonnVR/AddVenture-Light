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

/**
 * RegistroController
 * ------------------
 * Controlador encargado de gestionar todo lo relacionado con el registro
 * de nuevos usuarios dentro de la aplicación. Contiene rutas para mostrar
 * el formulario de registro y para procesar la creación de nuevos usuarios.
 *
 */
// Esta clase maneja las peticiones relacionadas con el registro de nuevos
// usuarios
@Controller
@RequestMapping("/registro")
public class RegistroController {

    /**
     * Servicio encargado de la lógica de negocio relacionada con la entidad
     * Usuario.
     * Aquí se utiliza para verificar existencia de correo/nombre de usuario
     * y para registrar nuevos usuarios de forma segura.
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra el formulario de registro.
     *
     * Método asociado a la ruta GET /registro.
     * Se ejecuta cuando un usuario visita la página de registro.
     *
     * Parámetros:
     * - Model model: permite enviar atributos a la vista.
     * - Principal principal: contiene la información del usuario autenticado.
     *
     * Comportamiento:
     * 1. Verifica si el usuario ya está autenticado (principal != null).
     * Si es así, se le redirige directamente a su perfil para evitar que
     * acceda al registro nuevamente.
     * 2. Si no está autenticado, se agrega al modelo un objeto vacío
     * RegistroUsuarioDTO para que el formulario pueda vincular los datos.
     * 3. Retorna la vista "registro" para que se muestre el formulario.
     */
    // Este método muestra el formulario de registro cuando el usuario no está
    // autenticado
    @GetMapping
    public String mostrarFormularioRegistro(Model model, Principal principal) {

        // Si el usuario ya inició sesión, se le envía a su perfil
        if (principal != null) {
            return "redirect:/perfil";
        }

        // Se crea un DTO vacío para enlazarlo con el formulario en la vista
        model.addAttribute("usuario", new RegistroUsuarioDTO());
        return "registro";
    }

    /**
     * Procesa el registro de un nuevo usuario.
     *
     * Método asociado a la ruta POST /registro.
     * Se ejecuta cuando el formulario de registro es enviado.
     *
     * Parámetros:
     * @ModelAttribute("usuario") RegistroUsuarioDTO usuarioDTO:
     * Toma los valores del formulario HTML y los coloca en el DTO.
     * @Valid: activa las validaciones definidas en el DTO.
     * - BindingResult result: contiene los errores de validación si existen.
     * - RedirectAttributes redirectAttributes: permite enviar mensajes Flash
     * en redirecciones.
     *
     * Flujo del proceso:
     * 1. Se validan reglas personalizadas:
     * - Verificar si el correo ya está registrado.
     * - Verificar si el nombre de usuario ya está en uso.
     * Si cualquiera es true, se agrega un error a BindingResult.
     *
     * 2. Si existen errores (result.hasErrors()), se retorna la vista
     * "registro" para mostrar los mensajes al usuario.
     *
     * 3. Si no hay errores, se llama a usuarioService.registrarUsuario()
     * para guardar el nuevo usuario en la base de datos.
     *
     * 4. Se añade un mensaje Flash indicando éxito.
     *
     * 5. Finalmente, se redirige al login para que el usuario ingrese.
     */
    // Este método maneja el registro de nuevos usuarios
    // Utiliza @ModelAttribute para vincular el formulario al DTO y @Valid para
    // validar los datos
    @PostMapping
    public String registrarUsuario(
            @ModelAttribute("usuario") @Valid RegistroUsuarioDTO usuarioDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Validación personalizada: correo existente
        if (usuarioService.existeCorreo(usuarioDTO.getCorreo())) {
            result.rejectValue("correo", "error.usuario", "Este correo ya está registrado");
        }

        // Validación personalizada: nombre de usuario existente
        if (usuarioService.existeNombreUsuario(usuarioDTO.getNombreUsuario())) {
            result.rejectValue("nombreUsuario", "error.usuario", "Este nombre de usuario ya está en uso");
        }

        // Si hay errores, retorna nuevamente al formulario de registro
        if (result.hasErrors()) {
            return "registro"; // retorna con errores
        }

        // Llama al servicio para registrar al nuevo usuario
        usuarioService.registrarUsuario(usuarioDTO);

        // Se envía un mensaje Flash para mostrar después de la redirección
        redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado correctamente");

        // Redirige al login para que el usuario pueda iniciar sesión
        return "redirect:/login";
    }
}