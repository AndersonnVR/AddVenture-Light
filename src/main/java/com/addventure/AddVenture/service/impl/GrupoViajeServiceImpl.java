package com.addventure.AddVenture.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.addventure.AddVenture.exception.GrupoViajeException;
import com.addventure.AddVenture.model.Etiqueta;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;
import com.addventure.AddVenture.model.ParticipanteGrupo;
import com.addventure.AddVenture.model.Usuario;
import com.addventure.AddVenture.repository.GrupoViajeRepository;
import com.addventure.AddVenture.repository.ParticipanteGrupoRepository;
import com.addventure.AddVenture.repository.SolicitudGrupoRepository;
import com.addventure.AddVenture.service.EtiquetaService;
import com.addventure.AddVenture.service.GrupoViajeService;
import com.addventure.AddVenture.service.ItinerarioService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GrupoViajeServiceImpl implements GrupoViajeService {

    private final GrupoViajeRepository grupoViajeRepository;
    private final EtiquetaService etiquetaService;
    private final ItinerarioService itinerarioService;
    private final SolicitudGrupoRepository solicitudGrupoRepository;
    private final ParticipanteGrupoRepository participanteGrupoRepository;

    private static final Logger logger = LoggerFactory.getLogger(GrupoViajeServiceImpl.class);

    @Autowired
    public GrupoViajeServiceImpl(GrupoViajeRepository grupoViajeRepository,
                                 EtiquetaService etiquetaService,
                                 ItinerarioService itinerarioService,
                                 SolicitudGrupoRepository solicitudGrupoRepository,
                                 ParticipanteGrupoRepository participanteGrupoRepository) {
        this.grupoViajeRepository = grupoViajeRepository;
        this.etiquetaService = etiquetaService;
        this.itinerarioService = itinerarioService;
        this.solicitudGrupoRepository = solicitudGrupoRepository;
        this.participanteGrupoRepository = participanteGrupoRepository;
    }

    @Override
    @Transactional
    public GrupoViaje crearGrupoViaje(GrupoViaje grupoViaje, Usuario creador, Set<String> nombresEtiquetas, List<Itinerario> itinerariosPropuestos) {

        grupoViaje.setCreador(creador);

        validarLinkUnicoEntreActivos(grupoViaje);

        if (grupoViajeRepository.existeConflictoDeFechasParaCreador(creador, grupoViaje.getFechaInicio(), grupoViaje.getFechaFin()) ||
            participanteGrupoRepository.existeConflictoDeFechasParaParticipante(creador, grupoViaje.getFechaInicio(), grupoViaje.getFechaFin())) {
            throw new GrupoViajeException("¡Ups! Ya tienes otro grupo planificado para esas fechas. Intenta con otras fechas para que no se crucen. 🚫");
        }

        Set<Etiqueta> etiquetasAsociadas = etiquetaService.procesarEtiquetas(nombresEtiquetas);
        grupoViaje.setEtiquetas(etiquetasAsociadas);

        List<Itinerario> itinerariosDelGrupo = new ArrayList<>();

        if (itinerariosPropuestos != null && !itinerariosPropuestos.isEmpty()) {
            long duracionViaje = ChronoUnit.DAYS.between(grupoViaje.getFechaInicio(), grupoViaje.getFechaFin()) + 1;

            if (itinerariosPropuestos.size() != duracionViaje) {
                logger.warn("Itinerarios incorrectos: se esperaban {} días, pero se proporcionaron {}. Creador: {}",
                        duracionViaje, itinerariosPropuestos.size(), creador.getCorreo());
                throw new GrupoViajeException("La cantidad de días en el itinerario no coincide con la duración del viaje. 🗓️");
            }

            for (int i = 0; i < itinerariosPropuestos.size(); i++) {
                Itinerario itinerario = itinerariosPropuestos.get(i);
                itinerario.setDiaNumero(i + 1);
                itinerario.setGrupo(grupoViaje);
                itinerariosDelGrupo.add(itinerario);
            }
        }
        grupoViaje.setItinerarios(itinerariosDelGrupo);

        ParticipanteGrupo participacionDelCreador = new ParticipanteGrupo();
        participacionDelCreador.setUsuario(creador);
        participacionDelCreador.setGrupo(grupoViaje);
        participacionDelCreador.setEstado("ACTIVO");

        grupoViaje.getParticipantes().add(participacionDelCreador);

        return grupoViajeRepository.save(grupoViaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoViaje> obtenerTodosLosGruposActivos() {
        return grupoViajeRepository.findByEstado("activo");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrupoViaje> obtenerGrupoPorId(Long id) {
        return grupoViajeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoViaje> obtenerGruposParaUsuario(Usuario usuario) {
        return grupoViajeRepository.findGruposValidosByCreadorOrParticipante(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GrupoViaje> obtenerGruposParaUsuarioPaginado(Usuario usuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return grupoViajeRepository.findGruposValidosByCreadorOrParticipante(usuario, pageable);
    }

    @Override
    public List<GrupoViaje> obtenerGruposFinalizadosParaPasaporte(Usuario usuario) {
        return grupoViajeRepository.findGruposFinalizadosByCreadorOrParticipante(usuario);
    }

    @Override
    public List<GrupoViaje> obtenerGruposActivosParaUsuario(Usuario usuario) {
        return grupoViajeRepository.findGruposActivosByCreadorOrParticipante(usuario);
    }

    @Override
    @Transactional
    public GrupoViaje actualizarGrupoViaje(Long id, GrupoViaje grupoViajeDetails, List<Itinerario> itinerariosActualizados, Usuario usuarioActual) {

        GrupoViaje grupoExistente = grupoViajeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar un grupo inexistente. ID: {}", id);
                    return new EntityNotFoundException("Grupo de viaje con ID " + id + " no encontrado.");
                });
        
        if (!grupoExistente.getCreador().getId().equals(usuarioActual.getId())) {
            logger.warn("Usuario '{}' intentó editar el grupo ID {} sin permisos. Creador real: {}",
                    usuarioActual.getCorreo(), id, grupoExistente.getCreador().getCorreo());
            throw new SecurityException("No tienes permiso para editar este grupo de viaje.");
        }

        if (!"activo".equalsIgnoreCase(grupoExistente.getEstado())) {
            logger.warn("Intento de editar grupo ID {} que no está activo (estado actual: {}). Usuario: {}",
                    id, grupoExistente.getEstado(), usuarioActual.getCorreo());
            throw new GrupoViajeException("Solo puedes editar grupos que estén activos. Este está " + grupoExistente.getEstado().toUpperCase() + ". 🛑");
        }

        validarLinkUnicoEntreActivos(grupoViajeDetails);

        Integer nuevoMaxParticipantes = grupoViajeDetails.getMaxParticipantes();
        int numeroActualParticipantes = grupoExistente.getParticipantes().size();
        if (nuevoMaxParticipantes < numeroActualParticipantes) {
            logger.warn("Intento de reducir participantes máximos a {} cuando ya hay {} en el grupo ID {}. Usuario: {}",
                    nuevoMaxParticipantes, numeroActualParticipantes, id, usuarioActual.getCorreo());
            throw new GrupoViajeException("No puedes reducir el límite a " + nuevoMaxParticipantes + " porque ya hay " + numeroActualParticipantes + " participantes en el grupo. 👥");
        }
        
        grupoExistente.setNombreViaje(grupoViajeDetails.getNombreViaje());
        grupoExistente.setDestinoPrincipal(grupoViajeDetails.getDestinoPrincipal());
        grupoExistente.setRangoEdadMin(grupoViajeDetails.getRangoEdadMin());
        grupoExistente.setRangoEdadMax(grupoViajeDetails.getRangoEdadMax());
        grupoExistente.setTipoGrupo(grupoViajeDetails.getTipoGrupo());
        grupoExistente.setDescripcion(grupoViajeDetails.getDescripcion());
        grupoExistente.setPuntoEncuentro(grupoViajeDetails.getPuntoEncuentro());
        grupoExistente.setImagenDestacada(grupoViajeDetails.getImagenDestacada());
        grupoExistente.setMaxParticipantes(grupoViajeDetails.getMaxParticipantes());
        grupoExistente.setLinkGrupoWhatsapp(grupoViajeDetails.getLinkGrupoWhatsapp());

        List<Itinerario> itinerariosActuales = itinerarioService.obtenerItinerariosPorGrupo(grupoExistente);

        if (itinerariosActualizados != null && !itinerariosActualizados.isEmpty()) {

            if (itinerariosActuales.size() != itinerariosActualizados.size()) {
                logger.warn("Cantidad de itinerarios desajustada al actualizar grupo ID {}. Existentes: {}, Nuevos: {}. Usuario: {}",
                        id, itinerariosActuales.size(), itinerariosActualizados.size(), usuarioActual.getCorreo());
                throw new GrupoViajeException("La cantidad de días del itinerario no coincide con el plan original del viaje. 📆");
            }

            for (int i = 0; i < itinerariosActuales.size(); i++) {
                Itinerario itinerarioExistente = itinerariosActuales.get(i);
                Itinerario itinerarioDetalleNuevo = itinerariosActualizados.get(i);

                itinerarioExistente.setTitulo(itinerarioDetalleNuevo.getTitulo());
                itinerarioExistente.setDescripcion(itinerarioDetalleNuevo.getDescripcion());
            }
        }

        return grupoViajeRepository.save(grupoExistente);
    }

    @Override
    @Transactional
    public void actualizarEstadosDeGrupos() {
        LocalDate hoy = LocalDate.now();
        
        List<GrupoViaje> gruposParaIniciar = grupoViajeRepository.findByEstadoAndFechaInicioLessThanEqual("activo", hoy);
        for (GrupoViaje grupo : gruposParaIniciar) {
            grupo.setEstado("en_curso");
            grupoViajeRepository.save(grupo);
            System.out.println("Grupo ID " + grupo.getIdGrupo() + " actualizado a EN_CURSO.");

            solicitudGrupoRepository.rechazarSolicitudesPendientesPorGrupo(grupo);
            System.out.println("Solicitudes pendientes del grupo ID " + grupo.getIdGrupo() + " fueron rechazadas automáticamente.");
        }

        List<GrupoViaje> gruposParaFinalizar = grupoViajeRepository.findByEstadoAndFechaFinBefore("en_curso", hoy);
        for (GrupoViaje grupo : gruposParaFinalizar) {
            grupo.setEstado("finalizado");
            grupoViajeRepository.save(grupo);
            System.out.println("Grupo ID " + grupo.getIdGrupo() + " actualizado a FINALIZADO.");
        }
    }

    @Override
    @Transactional
    public void desactivarGrupoViaje(Long id, Usuario usuarioActual) {
        GrupoViaje grupoExistente = grupoViajeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de eliminar un grupo inexistente. ID: {}", id);
                    return new EntityNotFoundException("Grupo de viaje con ID " + id + " no encontrado.");
                });
        
        if (!grupoExistente.getCreador().getId().equals(usuarioActual.getId())) {
            logger.warn("Usuario '{}' intentó eliminar el grupo ID {} sin permisos. Creador real: {}",
                    usuarioActual.getCorreo(), id, grupoExistente.getCreador().getCorreo());
            throw new SecurityException("No tienes permiso para eliminar este grupo de viaje.");
        }

        if (!"activo".equalsIgnoreCase(grupoExistente.getEstado()) && !"finalizado".equalsIgnoreCase(grupoExistente.getEstado())) {
            logger.warn("El grupo ID {} está en estado '{}' y no se puede eliminar. Usuario: {}",
                    id, grupoExistente.getEstado(), usuarioActual.getCorreo());
            throw new GrupoViajeException("Solo puedes eliminar un viaje que esté activo o finalizado. ¡No te preocupes, pronto podrás hacerlo! 😊");
        }

        grupoViajeRepository.desactivarGrupoViaje(id);
    }

    private void validarLinkUnicoEntreActivos(GrupoViaje grupoViaje) {
        boolean existe;

        if (grupoViaje.getIdGrupo() == null) {
            existe = grupoViajeRepository.existsByLinkGrupoWhatsapp(grupoViaje.getLinkGrupoWhatsapp());
        } else {
            existe = grupoViajeRepository.existsByLinkGrupoWhatsappAndIdGrupoNot(grupoViaje.getLinkGrupoWhatsapp(), grupoViaje.getIdGrupo());
        }

        if (existe) {
            throw new GrupoViajeException("Ya existe un grupo activo con ese enlace de WhatsApp. Por favor, usa uno diferente. 🔗");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GrupoViaje> filtrarPorEstadosYFechas(LocalDate fechaInicio, LocalDate fechaFin, String estado, Usuario usuario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());

        if(fechaInicio != null && fechaFin != null && estado != null && !estado.trim().isEmpty()){
            return grupoViajeRepository.findGruposPorEstadoYRangoFechas(fechaInicio, fechaFin, estado, usuario, pageable);
        } else{
            return grupoViajeRepository.findGruposValidosByCreadorOrParticipante(usuario, pageable);
        }
    }

}
