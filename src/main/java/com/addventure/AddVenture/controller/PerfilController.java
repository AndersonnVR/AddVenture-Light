package com.addventure.AddVenture.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.OpinionRepository;
import com.addventure.AddVenture.repository.UsuarioRepository;
import com.addventure.AddVenture.service.GrupoViajeService;

import java.security.Principal;

/**
 * PerfilController
 * 
 * Controlador Spring MVC que maneja las rutas relacionadas con la visualización
 * del perfil de usuario (perfil propio y perfil público de otros usuarios).
 * 
 * NOTAS:
 * - Usa Model para pasar atributos a las vistas (Thymeleaf, JSP, etc.).
 * - Usa Principal para obtener el correo del usuario autenticado.
 * - Repositorios y servicios son inyectados con @Autowired (puede migrarse a
 * inyección por constructor para pruebas y claridad).
 */
// Esta clase maneja las peticiones relacionadas con el perfil del usuario
@Controller
public class PerfilController {

    /**
     * Repositorio para operaciones sobre la entidad Usuario.
     *
     * Por lo general proporciona métodos como findById, save, delete, y
     * consultas personalizadas como findByCorreo o findByNombreUsuario.
     *
     * Inyección por campo con @Autowired: funciona pero no es la alternativa
     * más recomendada para testeo (preferible inyección por constructor).
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Repositorio para gestionar Opiniones (comentarios o valoraciones) que
     * otros usuarios dejan a un destinatario. Usado para mostrar las opiniones
     * recibidas por un usuario en su perfil.
     */
    @Autowired
    private OpinionRepository opinionRepository;

    /**
     * Servicio que agrupa la lógica relacionada con los grupos de viaje.
     *
     * Se usa aquí para obtener los viajes finalizados (para la sección de
     * pasaportes/logros) y los próximos viajes a los que el usuario pertenece.
     */
    @Autowired
    private GrupoViajeService grupoViajeService;

    /**
     * Muestra el perfil del usuario autenticado.
     *
     * Ruta: GET /perfil
     *
     * Parámetros:
     * - Model model: contenedor de atributos que serán accesibles en la vista.
     * - Principal principal: información del usuario autenticado (por lo
     * general provista por Spring Security). principal.getName() suele
     * devolver el nombre de usuario o correo según configuración.
     *
     * Flujo:
     * 1. Se obtiene el correo del usuario autenticado mediante Principal.
     * 2. Se consulta el Usuario en la base de datos con findByCorreo.
     * - findByCorreo devuelve Optional<Usuario>; aquí se usa orElse(null)
     * para manejar ausencia de usuario (podría preferirse orElseThrow
     * con una excepción customizada).
     * 3. Si no existe el usuario (usuarioPrincipal == null) redirige a la
     * página de login con un parámetro ?error.
     * 4. Si existe, se añaden al model los atributos necesarios para la vista
     * (usuario, logros, pasaportes, opiniones, proximosViajes).
     * 5. Retorna la vista "perfil" (p. ej. perfil.html de Thymeleaf).
     *
     * Consideraciones de seguridad y robustez:
     * - Se asume que Principal no es null en rutas protegidas; si la ruta
     * fuese accesible públicamente habría que comprobar null.
     * - usar orElse(null) evita excepciones pero oculta la razón; en entornos
     * productivos es preferible manejar el caso con mensajes más concretos
     * o logs que faciliten debugging.
     */
    // Método que muestra el perfil del usuario autenticado
    @GetMapping("/perfil")
    public String index(Model model, Principal principal) {

        // Extrae el nombre (o correo) del usuario autenticado
        String correo = principal.getName();

        // Busca el usuario en la base de datos por correo. Se usa Optional.
        Usuario usuarioPrincipal = usuarioRepository.findByCorreo(correo)
                .orElse(null);

        // Si no existe el usuario autenticado en la BD, redirige al login
        if (usuarioPrincipal == null) {

            // ?error se puede usar en la vista de login para mostrar un mensaje
            return "redirect:/login?error";
        }

        // Agrega al modelo los datos que la vista de perfil necesita mostrar.
        // "usuario" -> objeto Usuario completo (nombre, foto, biografía, etc.)
        model.addAttribute("usuario", usuarioPrincipal);

        // "logros" -> colección/entidad que representa logros o badges del
        // usuario. Se asume un getter getLogros() que devuelve una lista.
        model.addAttribute("logros", usuarioPrincipal.getLogros());

        // "pasaportes" -> obtiene los grupos/viajes finalizados para mostrar
        // en la sección tipo "pasaporte" o historial de viajes.
        model.addAttribute("pasaportes", grupoViajeService.obtenerGruposFinalizadosParaPasaporte(usuarioPrincipal));

        // "opiniones" -> todas las opiniones recibidas por este usuario.
        model.addAttribute("opiniones", opinionRepository.findByDestinatario(usuarioPrincipal));

        // "proximosViajes" -> viajes activos a los que el usuario está apuntado
        // o que están próximos; útil para mostrar un resumen de futuras salidas.
        model.addAttribute("proximosViajes", grupoViajeService.obtenerGruposActivosParaUsuario(usuarioPrincipal));

        // Devuelve la vista "perfil". El motor de templates renderizará la
        // página usando los atributos añadidos al model.
        return "perfil";
    }

    /**
     * Muestra el perfil público de otro usuario identificado por su
     * nombre de usuario (username).
     *
     * Ruta: GET /perfil/{nombreUsuario}
     *
     * Parámetros:
     * 
     * @PathVariable nombreUsuario: parte variable de la URL que identifica
     *               al usuario cuyo perfil público se quiere ver.
     *               - Model model: para pasar atributos a la vista.
     *
     *               Flujo:
     *               1. Se busca el usuario por nombreUsuario usando
     *               usuarioRepository.
     *               2. Si no existe, se redirige a una página 404 personalizada
     *               (/404).
     *               3. Si existe, se agregan los mismos atributos que en el perfil
     *               propio
     *               pero para el usuario consultado (logros, pasaportes, opiniones,
     *               proximos viajes) y se retorna la vista "perfil".
     *
     *               Observaciones:
     *               - Para el perfil público no se usa Principal, porque cualquiera
     *               puede
     *               visitar el perfil. No obstante, la vista puede mostrar acciones
     *               diferentes si el visitante está autenticado o es el
     *               propietario.
     *               - Dependiendo de la política de privacidad, algunos campos (ej.
     *               correo
     *               o datos sensibles) deberían ocultarse en el perfil público.
     */
    // Método que muestra el perfil público de cualquier usuario por su
    // nombreUsuario
    @GetMapping("/perfil/{nombreUsuario}")
    public String verPerfilPublico(@PathVariable String nombreUsuario,
            Model model) {

        // Busca en la BD al usuario cuyo nombre de usuario coincide con la
        // variable de la ruta. Aquí el método devuelve directamente Usuario
        // (podría devolver Optional en implementaciones futuras).
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario);

        // Si no se encuentra el usuario, redirige a la página 404.
        if (usuario == null) {

            // /404 ruta mapeada a una página personalizada.
            return "redirect:/404";
        }

        // Agrega al modelo los atributos usados en la vista "perfil".
        // Nota: la vista puede distinguir entre perfil propio y público con
        // lógica adicional (p. ej. comparar el usuario autenticado con el
        // usuario cuya página se está viendo).
        model.addAttribute("usuario", usuario);
        model.addAttribute("logros", usuario.getLogros());
        model.addAttribute("pasaportes", grupoViajeService.obtenerGruposFinalizadosParaPasaporte(usuario));
        model.addAttribute("opiniones", opinionRepository.findByDestinatario(usuario));
        model.addAttribute("proximosViajes", grupoViajeService.obtenerGruposActivosParaUsuario(usuario));

        return "perfil";
    }

}