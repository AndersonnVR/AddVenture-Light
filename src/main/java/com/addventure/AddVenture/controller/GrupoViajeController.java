package com.addventure.AddVenture.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.addventure.AddVenture.exception.GrupoViajeException;
import com.addventure.AddVenture.model.EstadoSolicitud;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;
import com.addventure.AddVenture.model.SolicitudGrupo;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.security.UsuarioDetails;
import com.addventure.AddVenture.service.GrupoViajeService;
import com.addventure.AddVenture.service.ItinerarioService;
import com.addventure.AddVenture.service.ParticipanteGrupoService;
import com.addventure.AddVenture.service.SolicitudGrupoService;
import com.addventure.AddVenture.validacion.ValidacionUsuario;

@Controller
@PreAuthorize("isAuthenticated()")
public class GrupoViajeController {

    private final GrupoViajeService grupoViajeService;
    private final ItinerarioService itinerarioService;
    private final ParticipanteGrupoService participanteGrupoService;
    private final SolicitudGrupoService solicitudGrupoService;

    private static final Logger logger = LoggerFactory.getLogger(GrupoViajeController.class);
    
    @Autowired
    public GrupoViajeController(GrupoViajeService grupoViajeService,
                                ItinerarioService itinerarioService,
                                ParticipanteGrupoService participanteGrupoService,
                                SolicitudGrupoService solicitudGrupoService) {
        this.grupoViajeService = grupoViajeService;
        this.itinerarioService = itinerarioService;
        this.participanteGrupoService = participanteGrupoService;
        this.solicitudGrupoService = solicitudGrupoService;
    }

    @GetMapping("/mis-viajes")
    public String listarMisGruposViaje(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                       @RequestParam(required = false) String estado,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "6") int size,
                                       Model model,
                                       @AuthenticationPrincipal UsuarioDetails principal) {

        Usuario usuarioActual = principal.getUsuario();
        Page<GrupoViaje> pagina;

        boolean filtrosIncompletos = (fechaInicio != null || fechaFin != null || (estado != null && !estado.trim().isEmpty()));
        boolean filtrosAplicados = (fechaInicio != null && fechaFin != null && estado != null && !estado.trim().isEmpty());

        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            model.addAttribute("mensajeError", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            pagina = grupoViajeService.obtenerGruposParaUsuarioPaginado(usuarioActual, page, size);
        } else if (filtrosAplicados) {
            pagina = grupoViajeService.filtrarPorEstadosYFechas(fechaInicio, fechaFin, estado, usuarioActual, page, size);
             if (!pagina.isEmpty()) {
                fechaInicio = null;
                fechaFin = null;
                estado = null;
            }
        } else if (filtrosIncompletos) {
            model.addAttribute("error", "Se deben ingresar todos los campos.");
            pagina = grupoViajeService.obtenerGruposParaUsuarioPaginado(usuarioActual, page, size);
        } else {
            pagina = grupoViajeService.obtenerGruposParaUsuarioPaginado(usuarioActual, page, size);
        }

        model.addAttribute("usuarioLogueado", usuarioActual);
        model.addAttribute("pagina", pagina);
        model.addAttribute("numeroPagina", page);
        model.addAttribute("filtros", filtrosAplicados);

        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("estado", estado);
        model.addAttribute("mensaje", filtrosAplicados);

        return "mis-viajes";
    }

    @GetMapping("/mis-viajes/solicitudes")
    public String listarGruposSolicitados(@RequestParam(required = false) String destinoPrincipal,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "6") int size,
                                          Model model,
                                          @AuthenticationPrincipal UsuarioDetails principal) {

        Usuario usuarioActual = principal.getUsuario();
        Page<GrupoViaje> pagina;

        boolean filtrosIncompletos = (destinoPrincipal != null && !destinoPrincipal.isBlank()) || fechaInicio != null || fechaFin != null;
        boolean filtros = destinoPrincipal != null && !destinoPrincipal.isBlank() && fechaInicio != null && fechaFin != null;

        if (filtros) {
            pagina = solicitudGrupoService.obtenerGruposFiltrados(usuarioActual, destinoPrincipal, fechaInicio, fechaFin, page, size);
             if (!pagina.isEmpty()) {
                destinoPrincipal = null;
                fechaInicio = null;
                fechaFin = null;
            }
        } else if ( filtrosIncompletos) {
             model.addAttribute("error", "Se deben ingresar todos los campos.");
            pagina = solicitudGrupoService.obtenerGruposConSolicitudesPendientesPaginado(usuarioActual, page, size);
        } else {
            pagina = solicitudGrupoService.obtenerGruposConSolicitudesPendientesPaginado(usuarioActual, page, size);
        }

        model.addAttribute("usuarioLogueado", usuarioActual);
        model.addAttribute("pagina", pagina);
        model.addAttribute("numeroPagina", page);

        model.addAttribute("destinoPrincipal", destinoPrincipal);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("mensaje", filtros);

        return "mis-solicitudes";
    }

    @GetMapping("/crear-grupo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("grupoViaje", new GrupoViaje());
        return "crear-grupo";
    }

    @PostMapping("/crear-grupo")
    public String crearGrupoViaje(@Validated(ValidacionUsuario.class) @ModelAttribute("grupoViaje") GrupoViaje grupoViaje,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "etiquetasInput", required = false) String etiquetasString,
                                  RedirectAttributes redirectAttributes,
                                  Model model,
                                  @AuthenticationPrincipal UsuarioDetails principal) {
        
        if (bindingResult.hasErrors()) {
            return "crear-grupo";
        }

        Usuario creador = principal.getUsuario();

        Set<String> nombresEtiquetas = parsearEtiquetas(etiquetasString);

        List<Itinerario> itinerariosPropuestos = grupoViaje.getItinerarios();   

        try {
            grupoViajeService.crearGrupoViaje(grupoViaje, creador, nombresEtiquetas, itinerariosPropuestos);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Grupo de viaje creado exitosamente! Prepárate para grandes aventuras. 🎉");
        
        } catch (GrupoViajeException e) {
            logger.warn("Error de negocio al crear grupo por '{}': {}", creador.getCorreo(), e.getMessage());
            model.addAttribute("mensajeError", e.getMessage());
            return "crear-grupo";
        
        } catch (Exception e) {
            logger.error("Error inesperado al crear grupo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un problema al intentar crear el grupo. Por favor, intenta nuevamente más tarde. 🔁");
            return "redirect:/crear-grupo";
        }

        return "redirect:/mis-viajes";
    }

    private Set<String> parsearEtiquetas(String etiquetasString) {

        Set<String> nombres = new HashSet<>();

        if (etiquetasString != null && !etiquetasString.trim().isEmpty()) {
            String[] tagsArray = etiquetasString.split("[,;\\s]+");
            for (String tag : tagsArray) {
                tag = tag.trim();
                if (tag.startsWith("#")) {
                    tag = tag.substring(1);
                }
                if (!tag.isEmpty()) {
                    nombres.add(tag);
                }
            }
        }

        return nombres;
    }

    @GetMapping("/mis-viajes/{id}")
    public String verDetallesGrupo(@PathVariable("id") Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   @AuthenticationPrincipal UsuarioDetails principal) {

        Optional<GrupoViaje> grupoOptional = grupoViajeService.obtenerGrupoPorId(id);

        if (grupoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Grupo de viaje no encontrado. Revisa tus viajes o intenta más tarde. ❌");
            return "redirect:/mis-viajes";
        }

        GrupoViaje grupo = grupoOptional.get();
        model.addAttribute("grupo", grupo);

        List<Itinerario> itinerariosOrdenados = itinerarioService.obtenerItinerariosPorGrupo(grupo);
        model.addAttribute("itinerarios", itinerariosOrdenados);

        Usuario usuarioActual = principal.getUsuario();
        model.addAttribute("usuarioLogueado", usuarioActual);

        boolean esCreador = grupo.getCreador().getId().equals(usuarioActual.getId());
        model.addAttribute("esCreador", esCreador);

        boolean esParticipante = participanteGrupoService.esUsuarioParticipante(id, usuarioActual);
        model.addAttribute("esParticipante", esParticipante);

        boolean grupoLleno = grupo.getParticipantes().size() >= grupo.getMaxParticipantes();
        model.addAttribute("grupoLleno", grupoLleno);

        boolean permiteSolicitudes = grupoLleno || !solicitudGrupoService.obtenerSolicitudesPendientesDeGrupo(grupo).isEmpty();
        model.addAttribute("permiteSolicitudes", permiteSolicitudes);
        
        Optional<SolicitudGrupo> solicitudExistente = solicitudGrupoService.obtenerSolicitudPorUsuarioYGrupo(usuarioActual, grupo);
        boolean yaSolicito = solicitudExistente.isPresent() && solicitudExistente.get().getEstado() == EstadoSolicitud.PENDIENTE;
        model.addAttribute("yaSolicito", yaSolicito);

        if (esCreador) {
            List<SolicitudGrupo> solicitudesPendientes = solicitudGrupoService.obtenerSolicitudesPendientesDeGrupo(grupo);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        }

        return "detalles-viaje";
    }

    // Mostrar formulario de edición de un grupo de viaje
    @GetMapping("/mis-viajes/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, 
                                           Model model,
                                           RedirectAttributes redirectAttributes, 
                                           @AuthenticationPrincipal UsuarioDetails principal) {
        
        Optional<GrupoViaje> grupoOptional = grupoViajeService.obtenerGrupoPorId(id);

        if (grupoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Grupo de viaje a editar no encontrado. Revisa tus grupos o intenta más tarde. ❌");
            return "redirect:/mis-viajes";
        }

        GrupoViaje grupo = grupoOptional.get();

        if (!grupo.getCreador().getId().equals(principal.getUsuario().getId())) {
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permiso para editar este grupo. Solo el organizador puede hacerlo. 🚫");
            return "redirect:/mis-viajes";
        }

        model.addAttribute("grupoViaje", grupo);
        return "editar-grupo";
    }

    @PostMapping("/mis-viajes/editar/{id}")
    public String actualizarGrupoViaje(@PathVariable("id") Long id,
                                       @Validated(ValidacionUsuario.class) @ModelAttribute("grupoViaje") GrupoViaje grupoViaje,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       @AuthenticationPrincipal UsuarioDetails principal) {
        
        if (bindingResult.hasErrors()) {
            return "editar-grupo";
        }

        Usuario usuarioActual = principal.getUsuario();

        List<Itinerario> itinerariosActualizados = grupoViaje.getItinerarios();

        try {
            grupoViajeService.actualizarGrupoViaje(id, grupoViaje, itinerariosActualizados, usuarioActual);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Grupo de viaje actualizado exitosamente! Listo para nuevas aventuras. ✅");
        
        } catch (GrupoViajeException e) {
            logger.warn("Error de negocio al actualizar grupo ID {} por '{}': {}",
                    id, usuarioActual.getCorreo(), e.getMessage());
            model.addAttribute("mensajeError", e.getMessage());
            return "editar-grupo";
        
        }
        catch (SecurityException e) {
            logger.warn("Usuario '{}' intentó editar sin permisos el grupo ID {}", usuarioActual.getCorreo(), id, e);
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permiso para editar este grupo. Solo el organizador puede hacerlo. 🚫");
            return "redirect:/mis-viajes";
        
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar grupo ID {} por '{}': {}",
                    id, usuarioActual.getCorreo(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un problema al intentar actualizar el grupo. Por favor, intenta nuevamente más tarde. 🔁");
            return "redirect:/mis-viajes/editar/" + id;
        }

        return "redirect:/mis-viajes/" + id;
    }

    // Eliminar lógicamente un grupo de viaje
    @PostMapping("/mis-viajes/eliminar/{id}")
    public String desactivarGrupoViaje(@PathVariable("id") Long id,
                                       RedirectAttributes redirectAttributes,
                                       @AuthenticationPrincipal UsuarioDetails principal) {

        try {
            grupoViajeService.desactivarGrupoViaje(id, principal.getUsuario());
            redirectAttributes.addFlashAttribute("mensajeExito", "Grupo de viaje eliminado con éxito. ¡Nos vemos en la próxima aventura! 😉");
        
        } catch (GrupoViajeException e) {
            logger.warn("Error de negocio al eliminar grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        
        } catch (SecurityException e) {
            logger.warn("Usuario '{}' intentó eliminar sin permisos el grupo ID {}", principal.getUsername(), id, e);
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permiso para eliminar este grupo. Solo el organizador puede hacerlo. 🚫");
        
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un problema al intentar eliminar el grupo. Por favor, intenta nuevamente más tarde. 🔁");
        }

        return "redirect:/mis-viajes";
    }

    @GetMapping("/admin/actualizar-estados")
    public String ejecutarActualizacionDeEstados(RedirectAttributes redirectAttributes) {
        try {
            grupoViajeService.actualizarEstadosDeGrupos();
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Tarea de actualización de estados ejecutada exitosamente! ✅");
        
        } catch (Exception e) {
            logger.error("Error técnico al actualizar manualmente los estados de los grupos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un error durante la actualización manual de estados. ⛔");
        }
        
        return "redirect:/mis-viajes";
    }

}
