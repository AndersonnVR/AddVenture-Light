package com.addventure.AddVenture.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.addventure.AddVenture.exception.ParticipacionException;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.security.UsuarioDetails;
import com.addventure.AddVenture.service.ParticipanteGrupoService;

import jakarta.persistence.EntityNotFoundException;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/grupo")
public class ParticipanteGrupoController {

    private final ParticipanteGrupoService participanteGrupoService;

    private static final Logger logger = LoggerFactory.getLogger(ParticipanteGrupoController.class);

    @Autowired
    public ParticipanteGrupoController(ParticipanteGrupoService participanteGrupoService) {
        this.participanteGrupoService = participanteGrupoService;
    }

    @PostMapping("/{id}/unirse")
    public String unirseAGrupo(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioDetails principal,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioActual = principal.getUsuario();
            participanteGrupoService.unirseAGrupo(id, usuarioActual);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "¡Listo para la aventura! Te has unido al grupo exitosamente. ✈️");
        } catch (ParticipacionException e) {
            logger.warn("Error de negocio al unirse al grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Intento de unirse a grupo inexistente ID {} por '{}'", id, principal.getUsername(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "El grupo que estás buscando no existe o fue eliminado. 🗑️");
        } catch (Exception e) {
            logger.error("Error inesperado al unirse al grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un problema al intentar unirte del grupo. Por favor, intenta nuevamente más tarde. 🔁");
        }

        return "redirect:/mis-viajes/" + id;
    }

    @PostMapping("/{id}/salirse")
    public String salirseDeGrupo(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioDetails principal,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioActual = principal.getUsuario();
            participanteGrupoService.salirseDeGrupo(id, usuarioActual);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Has salido del grupo con éxito. ¡Esperamos verte en futuras aventuras! 👋");
        } catch (ParticipacionException e) {
            logger.warn("Error de negocio al salir del grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Intento de salir de grupo inexistente ID {} por '{}'", id, principal.getUsername(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "El grupo del que intentas salir ya no existe o fue eliminado. 🗑️");
        } catch (Exception e) {
            logger.error("Error inesperado al salir del grupo ID {} por '{}': {}",
                    id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un problema al intentar salir del grupo. Por favor, intenta nuevamente más tarde. 🔁");
        }

        return "redirect:/mis-viajes/" + id;
    }

    @PostMapping("/{id}/expulsar/{idParticipante}")
    public String expulsarParticipante(@PathVariable("id") Long id,
            @PathVariable("idParticipante") Long idParticipante,
            @AuthenticationPrincipal UsuarioDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario creador = principal.getUsuario();
            participanteGrupoService.eliminarParticipanteDelGrupo(id, idParticipante, creador);
            redirectAttributes.addFlashAttribute("mensajeExito", "Participante expulsado correctamente. ❌");

        } catch (SecurityException e) {
            logger.warn("Acceso denegado al expulsar ID {} del grupo ID {} por '{}': {}",
                    idParticipante, id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permisos para realizar esta acción. 🚫");

        } catch (EntityNotFoundException e) {
            logger.warn("Error al expulsar ID {} del grupo ID {} por '{}': {}",
                    idParticipante, id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró al participante o al grupo. ❌");

        } catch (ParticipacionException e) {
            logger.warn("Error de negocio en la expulsión ID {} del grupo ID {} por '{}': {}",
                    idParticipante, id, principal.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (Exception e) {
            logger.error("Error inesperado al expulsar participante ID {} del grupo ID {} por '{}': {}",
                    idParticipante, id, principal.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ocurrió un error al intentar expulsar al participante. Intenta más tarde. ");
        }

        return "redirect:/mis-viajes/" + id;
    }

}
