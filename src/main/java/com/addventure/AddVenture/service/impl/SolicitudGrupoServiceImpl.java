package com.addventure.AddVenture.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.addventure.AddVenture.exception.SolicitudException;
import com.addventure.AddVenture.model.EstadoSolicitud;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.ParticipanteGrupo;
import com.addventure.AddVenture.model.SolicitudGrupo;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.ParticipanteGrupoRepository;
import com.addventure.AddVenture.repository.SolicitudGrupoRepository;
import com.addventure.AddVenture.service.GrupoViajeService;
import com.addventure.AddVenture.service.ParticipanteGrupoService;
import com.addventure.AddVenture.service.SolicitudGrupoService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SolicitudGrupoServiceImpl implements SolicitudGrupoService{

    private final SolicitudGrupoRepository solicitudGrupoRepository;
    private final ParticipanteGrupoRepository participanteGrupoRepository;
    private final ParticipanteGrupoService participanteGrupoService;
    private final GrupoViajeService grupoViajeService;

    private static final Logger logger = LoggerFactory.getLogger(SolicitudGrupoServiceImpl.class);

    @Autowired
    public SolicitudGrupoServiceImpl(
            SolicitudGrupoRepository solicitudGrupoRepository,
            ParticipanteGrupoRepository participanteGrupoRepository,
            ParticipanteGrupoService participanteGrupoService,
            GrupoViajeService grupoViajeService) {

        this.solicitudGrupoRepository = solicitudGrupoRepository;
        this.participanteGrupoRepository = participanteGrupoRepository;
        this.participanteGrupoService = participanteGrupoService;
        this.grupoViajeService = grupoViajeService;
    }

    @Override
    @Transactional
    public void enviarSolicitud(Usuario solicitante, GrupoViaje grupo) {

        Long idGrupo = grupo.getIdGrupo();

        // Verificar si el grupo está activo
        if (!"activo".equalsIgnoreCase(grupo.getEstado())) {
            logger.warn("Usuario '{}' intentó solicitar ingreso a grupo ID {} que no está activo (estado: {})",
                    solicitante.getCorreo(), grupo.getIdGrupo(), grupo.getEstado());
            throw new SolicitudException("El grupo no está activo, no puedes enviar una solicitud en este momento. 😕");
        }

        // Verificar si ya es participante
        if (participanteGrupoService.esUsuarioParticipante(idGrupo, solicitante)) {
            logger.warn("Usuario '{}' ya forma parte del grupo ID {}", solicitante.getCorreo(), idGrupo);
            throw new SolicitudException("¡Ya eres un viajero en este grupo! 🚀");
        }

        // Verificar si ya envió una solicitud
        Optional<SolicitudGrupo> solicitudExistente = solicitudGrupoRepository.findBySolicitanteAndGrupo(solicitante, grupo);
        if (solicitudExistente.isPresent()) {
            EstadoSolicitud estado = solicitudExistente.get().getEstado();
            logger.warn("Usuario '{}' ya tiene una solicitud con estado {} para el grupo ID {}",
                    solicitante.getCorreo(), estado, idGrupo);

            switch (estado) {
                case PENDIENTE:
                    throw new SolicitudException("Tu solicitud está pendiente. ¡Sé paciente! ⏳");
                case ACEPTADA:
                    throw new SolicitudException("¡Ya eres un viajero en este grupo! 🚀");
                case CANCELADA:
                case RECHAZADA:
                    // Reutilizar la solicitud existente
                    SolicitudGrupo solicitud = solicitudExistente.get();
                    solicitud.setEstado(EstadoSolicitud.PENDIENTE);
                    solicitud.setFechaSolicitud(LocalDateTime.now());
                    solicitudGrupoRepository.save(solicitud);

                    logger.info("Usuario '{}' ha reenviado una solicitud al grupo ID {} (estado anterior: {}).",
                            solicitante.getCorreo(), idGrupo, estado);
                    return;
            }
        }

        // Verificar que el grupo esté lleno o existan solicitudes pendientes antes de permitir enviar la solicitud
        int numParticipantes = grupo.getParticipantes().size();
        int max = grupo.getMaxParticipantes();
        boolean haySolicitudesPendientes = !solicitudGrupoRepository.findByGrupoAndEstado(grupo, EstadoSolicitud.PENDIENTE).isEmpty();

        if (numParticipantes < max && !haySolicitudesPendientes) {
            logger.warn(
                    "Grupo ID {} aún tiene cupo libre y no tiene solicitudes pendientes. Usuario '{}' debería unirse directamente.",
                    idGrupo, solicitante.getCorreo());
            throw new SolicitudException(
                    "Este grupo tiene espacio disponible y no hay solicitudes pendientes. ¡Puedes unirte ahora! 🚀");
        }

        // Crear y guardar la solicitud
        SolicitudGrupo nuevaSolicitud = SolicitudGrupo.builder()
                .solicitante(solicitante)
                .grupo(grupo)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        solicitudGrupoRepository.save(nuevaSolicitud);
    }

    @Override
    @Transactional
    public void cancelarSolicitud(Long idGrupo, Usuario usuarioActual) {
        // Obtener el grupo o lanzar excepción si no existe
        GrupoViaje grupo = grupoViajeService.obtenerGrupoPorId(idGrupo)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado."));

        // Obtener la solicitud del usuario para el grupo o lanzar excepción si no existe
        SolicitudGrupo solicitud = solicitudGrupoRepository
                .findBySolicitanteAndGrupo(usuarioActual, grupo)
                .orElseThrow(() -> new SolicitudException("No tienes ninguna solicitud activa para este grupo. 😕"));

        // Verificar que la solicitud esté pendiente antes de cancelarla
        if (!solicitud.getEstado().equals(EstadoSolicitud.PENDIENTE)) {
            throw new SolicitudException("Solo puedes cancelar solicitudes que están pendientes. 🛑");
        }

        // Cambiar el estado de la solicitud a CANCELADA
        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        solicitudGrupoRepository.save(solicitud);
    }

    @Override
    @Transactional
    public Long aceptarSolicitud(Long idSolicitud, Usuario creador) {

        SolicitudGrupo solicitud = solicitudGrupoRepository.findById(idSolicitud)
                .orElseThrow(() -> {
                    logger.warn("Solicitud con ID {} no encontrada", idSolicitud);
                    return new SolicitudException("La solicitud que buscas no existe. 😕");
                });

        GrupoViaje grupo = solicitud.getGrupo();

        // Validar que el usuario que intenta aceptar sea el creador del grupo
        if (!grupo.getCreador().getId().equals(creador.getId())) {
            logger.warn("Usuario '{}' intentó aceptar solicitud del grupo ID {} sin ser el creador.",
                    creador.getCorreo(), grupo.getIdGrupo());
            throw new SecurityException("Solo el creador del grupo puede aceptar solicitudes.");
        }

        // Verificar que la solicitud esté pendiente
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            logger.warn("Solicitud ID {} no está en estado pendiente. Estado actual: {}", idSolicitud,
                    solicitud.getEstado());
            throw new SolicitudException("Esta solicitud ya ha sido procesada. ✅");
        }

        // Verificar que aún haya espacio en el grupo
        int participantesActuales = grupo.getParticipantes().size();
        if (participantesActuales >= grupo.getMaxParticipantes()) {
            logger.warn("El grupo ID {} ya alcanzó el máximo de participantes. No se puede aceptar la solicitud ID {}.",
                    grupo.getIdGrupo(), idSolicitud);
           throw new SolicitudException("Este grupo ya está completo. No puedes aceptar más solicitudes. 🚫");
        }

        // Verificar que el solicitante no tenga conflictos de fechas
        Usuario solicitante = solicitud.getSolicitante();
        if (participanteGrupoRepository.existeConflictoDeFechasParaParticipante(solicitante, grupo.getFechaInicio(), grupo.getFechaFin())) {
            logger.warn("No se puede aceptar la solicitud ID {}: el usuario '{}' ya participa en otro grupo con fechas cruzadas ({} - {}).",
                    idSolicitud, solicitante.getCorreo(), grupo.getFechaInicio(), grupo.getFechaFin());
            throw new SolicitudException("Este usuario ya participa en otro viaje con fechas que se cruzan. 🚷");
        }

        // Crear nueva participación
        ParticipanteGrupo nuevoParticipante = new ParticipanteGrupo();
        nuevoParticipante.setUsuario(solicitud.getSolicitante());
        nuevoParticipante.setGrupo(grupo);
        nuevoParticipante.setEstado("ACTIVO");

        participanteGrupoRepository.save(nuevoParticipante);

        // Actualizar estado de solicitud
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitudGrupoRepository.save(solicitud);

        logger.info("Solicitud ID {} aceptada por '{}'. Usuario '{}' agregado al grupo ID {}.",
                idSolicitud, creador.getCorreo(), solicitud.getSolicitante().getCorreo(), grupo.getIdGrupo());

        return grupo.getIdGrupo();
    }

    @Override
    @Transactional
    public Long rechazarSolicitud(Long idSolicitud, Usuario creador) {

        SolicitudGrupo solicitud = solicitudGrupoRepository.findById(idSolicitud)
                .orElseThrow(() -> {
                    logger.warn("Solicitud con ID {} no encontrada", idSolicitud);
                    return new SolicitudException("La solicitud que buscas no existe. 😕");
                });

        GrupoViaje grupo = solicitud.getGrupo();

        // Validar que solo el creador pueda rechazar
        if (!grupo.getCreador().getId().equals(creador.getId())) {
            logger.warn("Usuario '{}' intentó rechazar solicitud del grupo ID {} sin ser el creador.",
                    creador.getCorreo(), grupo.getIdGrupo());
            throw new SecurityException("Solo el creador del grupo puede rechazar solicitudes.");
        }

        // Verificar que la solicitud esté pendiente
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            logger.warn("Solicitud ID {} ya fue procesada. Estado: {}", idSolicitud, solicitud.getEstado());
            throw new SolicitudException("Esta solicitud ya ha sido procesada. ✅");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudGrupoRepository.save(solicitud);

        return grupo.getIdGrupo();
    }

    @Override
    public List<SolicitudGrupo> obtenerSolicitudesPendientesDeGrupo(GrupoViaje grupo) {
        return solicitudGrupoRepository.findByGrupoAndEstado(grupo, EstadoSolicitud.PENDIENTE);
    }

    @Override
    public Optional<SolicitudGrupo> obtenerSolicitudPorUsuarioYGrupo(Usuario usuario, GrupoViaje grupo) {
        return solicitudGrupoRepository.findBySolicitanteAndGrupo(usuario, grupo);
    }

    @Override
    public Page<GrupoViaje> obtenerGruposConSolicitudesPendientesPaginado(Usuario usuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return solicitudGrupoRepository.findGruposConSolicitudPendiente(usuario, pageable);
    }

    //MÉTODO DE FILTRO -- falta
    @Override
    public Page<GrupoViaje> obtenerGruposFiltrados(Usuario usuario, String destino, LocalDate fechaInicio,
            LocalDate fechaFin, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return solicitudGrupoRepository.findGruposConSolicitudesPendienteFiltrado(usuario, destino, fechaInicio, fechaFin, pageable);
    }
    
}
