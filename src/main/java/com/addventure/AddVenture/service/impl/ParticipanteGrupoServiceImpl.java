package com.addventure.AddVenture.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.addventure.AddVenture.exception.ParticipacionException;
import com.addventure.AddVenture.model.EstadoSolicitud;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.ParticipanteGrupo;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.ParticipanteGrupoRepository;
import com.addventure.AddVenture.repository.SolicitudGrupoRepository;
import com.addventure.AddVenture.service.GrupoViajeService;
import com.addventure.AddVenture.service.ParticipanteGrupoService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ParticipanteGrupoServiceImpl implements ParticipanteGrupoService {

    private final ParticipanteGrupoRepository participanteGrupoRepository;
    private final GrupoViajeService grupoViajeService;
    private final SolicitudGrupoRepository solicitudGrupoRepository;

    private static final Logger logger = LoggerFactory.getLogger(ParticipanteGrupoServiceImpl.class);

    @Autowired
    public ParticipanteGrupoServiceImpl(ParticipanteGrupoRepository participanteGrupoRepository,
                                        GrupoViajeService grupoViajeService,
                                        SolicitudGrupoRepository solicitudGrupoRepository) {
        this.participanteGrupoRepository = participanteGrupoRepository;
        this.grupoViajeService = grupoViajeService;
        this.solicitudGrupoRepository = solicitudGrupoRepository;
    }

    @Override
    @Transactional
    public void unirseAGrupo(Long idGrupo, Usuario usuarioActual) {
        GrupoViaje grupo = grupoViajeService.obtenerGrupoPorId(idGrupo)
                .orElseThrow(() -> {
                    logger.warn("Intento de unirse a grupo inexistente. ID: {}", idGrupo);
                    return new EntityNotFoundException("El grupo de viaje no existe.");
                });
        
        if (!"activo".equalsIgnoreCase(grupo.getEstado())) {
            logger.warn("Usuario '{}' intentó unirse al grupo ID {} que no está activo (estado: {})",
                    usuarioActual.getCorreo(), idGrupo, grupo.getEstado());
            throw new ParticipacionException("No puedes unirte al viaje porque ya ha comenzado o ha finalizado. ¡Esperemos que pronto haya uno nuevo! 😊");
        }

        if (grupo.getCreador().getId().equals(usuarioActual.getId())) {
            logger.warn("Usuario '{}' intentó unirse como participante a su propio grupo ID {}",
                    usuarioActual.getCorreo(), idGrupo);
            throw new ParticipacionException("¡Ya eres el organizador de este viaje! No necesitas unirte como participante. 🎒");
        }

        if (esUsuarioParticipante(idGrupo, usuarioActual)) {
            logger.warn("Usuario '{}' ya es participante del grupo ID {}",
                    usuarioActual.getCorreo(), idGrupo);
            throw new ParticipacionException("¡Ya eres un viajero en este grupo! 🚀");
        }

        if (participanteGrupoRepository.existeConflictoDeFechasParaParticipante(usuarioActual, grupo.getFechaInicio(), grupo.getFechaFin())) {
            logger.warn("Usuario '{}' intentó unirse a un grupo con fechas que se solapan con otro grupo en el que ya participa ({} - {})",
                    usuarioActual.getCorreo(), grupo.getFechaInicio(), grupo.getFechaFin());
            throw new ParticipacionException("Ya estás participando en otro viaje en fechas similares. ¡Disfrútalo y luego podrás unirte a otro! ✈️");
        }
        
        int participantesActuales = grupo.getParticipantes().size();
        int capacidadMaxima = grupo.getMaxParticipantes();

        boolean haySolicitudesPendientes = !solicitudGrupoRepository.findByGrupoAndEstado(grupo, EstadoSolicitud.PENDIENTE).isEmpty();

        if (participantesActuales >= capacidadMaxima || haySolicitudesPendientes) {
            logger.warn("Unión directa bloqueada para grupo ID {}. Participantes: {}/{}. Solicitudes pendientes: {}. Usuario: {}",
                    idGrupo, participantesActuales, capacidadMaxima, haySolicitudesPendientes, usuarioActual.getCorreo());
            throw new ParticipacionException("Este grupo no admite nuevas uniones directas en este momento. Puedes enviar una solicitud. ⏳");
        }

        ParticipanteGrupo nuevaParticipacion = new ParticipanteGrupo();
        nuevaParticipacion.setUsuario(usuarioActual);
        nuevaParticipacion.setGrupo(grupo);
        nuevaParticipacion.setEstado("ACTIVO");

        participanteGrupoRepository.save(nuevaParticipacion);
    }

    @Override
    @Transactional
    public void salirseDeGrupo(Long idGrupo, Usuario usuarioActual) {
        GrupoViaje grupo = grupoViajeService.obtenerGrupoPorId(idGrupo)
                .orElseThrow(() -> {
                    logger.warn("Intento de salir de grupo inexistente. ID: {}", idGrupo);
                    return new EntityNotFoundException("El grupo de viaje no existe.");
                });
        
        if (!"activo".equalsIgnoreCase(grupo.getEstado()) && !"finalizado".equalsIgnoreCase(grupo.getEstado())) {
            logger.warn("Usuario '{}' intentó salir de un grupo ID {} en estado no permitido ('{}')",
                    usuarioActual.getCorreo(), idGrupo, grupo.getEstado());
            throw new ParticipacionException("No puedes salir de un viaje ya que ha comenzado. Por favor, espera a que finalice para poder salir. 😊");
        }
        
        Optional<ParticipanteGrupo> participacionOptional = participanteGrupoRepository.findByUsuarioAndGrupo(usuarioActual, grupo);

        if (participacionOptional.isPresent()) {
            participanteGrupoRepository.delete(participacionOptional.get());
        } else {
            logger.warn("Usuario '{}' intentó salir de un grupo ID {} del cual no es participante",
                    usuarioActual.getCorreo(), idGrupo);
            throw new ParticipacionException("No formas parte de este grupo, por lo que no puedes salir de él. ❌");
        }
    }

    @Override
    @Transactional
    public void eliminarParticipanteDelGrupo(Long idGrupo, Long idParticipante, Usuario creador) {
        GrupoViaje grupo = grupoViajeService.obtenerGrupoPorId(idGrupo)
                .orElseThrow(() -> {
                    logger.warn("Intento de expulsar participante de grupo inexistente ID {}", idGrupo);
                    return new EntityNotFoundException("El grupo de viaje no existe.");
                });
        
        if (!grupo.getCreador().getId().equals(creador.getId())) {
            logger.warn("Usuario '{}' intentó eliminar un participante del grupo ID {} sin ser el creador.",
                    creador.getCorreo(), idGrupo);
            throw new SecurityException("No tienes permisos para realizar esta acción.");
        }

        Usuario usuarioAEliminar = new Usuario();
        usuarioAEliminar.setId(idParticipante);

        ParticipanteGrupo participacion = participanteGrupoRepository.findByUsuarioAndGrupo(usuarioAEliminar, grupo)
                .orElseThrow(() -> {
                    logger.warn("No se encontró participación del usuario ID {} en el grupo ID {}", idParticipante, idGrupo);
                    return new EntityNotFoundException("El participante no fue encontrado en este grupo.");
                });
        
        if (!participacion.getGrupo().getIdGrupo().equals(idGrupo)) {
            logger.warn("Participación ID {} no corresponde al grupo ID {}", idParticipante, idGrupo);
            throw new ParticipacionException("Esta participación no pertenece a este grupo. 😕");
        }

        if (participacion.getUsuario().getId().equals(creador.getId())) {
            logger.warn("Usuario '{}' intentó eliminarse a sí mismo como creador del grupo ID {}", creador.getCorreo(), idGrupo);
            throw new ParticipacionException("No puedes eliminarte a ti mismo como creador del grupo. 🙅‍♂️");
        }
        
        participanteGrupoRepository.delete(participacion);
        logger.info("Participante '{}' fue eliminado del grupo ID {} por su creador '{}'.",
                participacion.getUsuario().getCorreo(), idGrupo, creador.getCorreo());

        solicitudGrupoRepository.findBySolicitanteAndGrupo(participacion.getUsuario(), grupo)
                                .filter(s -> s.getEstado() == EstadoSolicitud.ACEPTADA)
                                .ifPresent(solicitudGrupoRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esUsuarioParticipante(Long idGrupo, Usuario usuarioActual) {
        Optional<GrupoViaje> grupoOptional = grupoViajeService.obtenerGrupoPorId(idGrupo);
        if (grupoOptional.isEmpty()) {
            return false;
        }
        
        GrupoViaje grupo = grupoOptional.get();
        
        return participanteGrupoRepository.findByUsuarioAndGrupo(usuarioActual, grupo).isPresent();
    }
    
}
