package com.addventure.AddVenture.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.addventure.AddVenture.exception.SolicitudException;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.security.UsuarioDetails;
import com.addventure.AddVenture.service.GrupoViajeService;
import com.addventure.AddVenture.service.SolicitudGrupoService;

import jakarta.persistence.EntityNotFoundException;

@Controller
@PreAuthorize("isAuthenticated()")
public class SolicitudGrupoController {

    private final SolicitudGrupoService solicitudGrupoService;
    private final GrupoViajeService grupoViajeService;

    private static final Logger logger = LoggerFactory.getLogger(SolicitudGrupoController.class);

    @Autowired
    public SolicitudGrupoController(SolicitudGrupoService solicitudGrupoService,
            GrupoViajeService grupoViajeService) {
        this.solicitudGrupoService = solicitudGrupoService;
        this.grupoViajeService = grupoViajeService;
    }
    
    @PostMapping("/{id}/solicitar")
    public String enviarSolicitud(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UsuarioDetails principal,
                                  RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioActual = principal.getUsuario();

            GrupoViaje grupo = grupoViajeService.obtenerGrupoPorId(id)
                    .orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado."));

            solicitudGrupoService.enviarSolicitud(usuarioActual, grupo);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Tu solicitud fue enviada exitosamente. El organizador la revisará pronto. 📨");

        } catch (SolicitudException e) {
            logger.warn("Error de negocio en la solicitud por '{}': {}", principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (EntityNotFoundException e) {
            logger.warn("Intento de solicitud a grupo inexistente ID {} por '{}'", id, principal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeError",
                    "El grupo que estás buscando no existe o fue eliminado. 🗑️");

        } catch (Exception e) {
            logger.error("Error inesperado al solicitar ingreso al grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un problema al enviar tu solicitud. Intenta nuevamente más tarde. 🔁");
        }

        return "redirect:/mis-viajes/" + id;
    }

    @PostMapping("/{id}/cancelar-solicitud")
    public String cancelarSolicitud(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UsuarioDetails principal,
                                    RedirectAttributes redirectAttributes) {

        try {
            solicitudGrupoService.cancelarSolicitud(id, principal.getUsuario());
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu solicitud ha sido cancelada correctamente. ❎");
        
        } catch (SolicitudException e) {
            logger.warn("Error de negocio en la cancelación de solicitud por '{}': {}",
                    principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (EntityNotFoundException e) {
            logger.warn("Intento de cancelar solicitud a grupo inexistente ID {} por '{}'", id, principal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeError",
                    "El grupo que estás buscando no existe o fue eliminado. 🗑️");

        } catch (Exception e) {
            logger.warn("Error al cancelar solicitud para el grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un error al cancelar la solicitud. Intenta más tarde. 🔁");
        }

        return "redirect:/mis-viajes/" + id;
    }

    @PostMapping("/solicitudes/{id}/aceptar")
    public String aceptarSolicitud(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal UsuarioDetails principal,
                                   RedirectAttributes redirectAttributes) {

        try {
            Usuario creador = principal.getUsuario();
            Long idGrupo = solicitudGrupoService.aceptarSolicitud(id, creador);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Has aceptado la solicitud correctamente! 🎉");
            return "redirect:/mis-viajes/" + idGrupo;

        } catch (SecurityException e) {
            logger.warn("Acceso denegado al aceptar solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permisos para aceptar esta solicitud. 🚫");

        } catch (EntityNotFoundException e) {
            logger.warn("Solicitud no encontrada ID {} al intentar aceptar por '{}'", id, principal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeError", "La solicitud no existe o ya fue eliminada. ❌");

        } catch (SolicitudException e) {
            logger.warn("Error de negocio al aceptar la solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (Exception e) {
            logger.error("Error inesperado al aceptar solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un error al aceptar la solicitud. Intenta más tarde. 🔁");
        }

        return "redirect:/mis-viajes";
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UsuarioDetails principal,
                                    RedirectAttributes redirectAttributes) {

        try {
            Usuario creador = principal.getUsuario();
            Long idGrupo = solicitudGrupoService.rechazarSolicitud(id, creador);
            redirectAttributes.addFlashAttribute("mensajeExito", "La solicitud fue rechazada correctamente. 🗑️");
            return "redirect:/mis-viajes/" + idGrupo;

        } catch (SecurityException e) {
            logger.warn("Acceso denegado al rechazar solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permisos para rechazar esta solicitud. 🚫");

        } catch (EntityNotFoundException e) {
            logger.warn("Solicitud no encontrada ID {} al intentar rechazar por '{}'", id, principal.getUsername());
            redirectAttributes.addFlashAttribute("mensajeError", "La solicitud no existe o ya fue eliminada. ❌");

        } catch (SolicitudException e) {
            logger.warn("Error de negocio al rechazar la solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (Exception e) {
            logger.error("Error inesperado al rechazar solicitud ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un error al rechazar la solicitud. Intenta más tarde. 🔁");
        }

        return "redirect:/mis-viajes";
    }

}
