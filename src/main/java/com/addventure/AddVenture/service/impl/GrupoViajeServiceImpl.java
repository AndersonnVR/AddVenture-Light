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

        // Asignar el creador al grupo de viaje
        grupoViaje.setCreador(creador);

        // Verificar que el link de WhatsApp no sea duplicado
        validarLinkUnicoEntreActivos(grupoViaje);

        // Verificar que no exista conflicto entre fechas tanto como creador, como participante
        if (grupoViajeRepository.existeConflictoDeFechasParaCreador(creador, grupoViaje.getFechaInicio(), grupoViaje.getFechaFin()) ||
            participanteGrupoRepository.existeConflictoDeFechasParaParticipante(creador, grupoViaje.getFechaInicio(), grupoViaje.getFechaFin())) {
            throw new GrupoViajeException("¡Ups! Ya tienes otro grupo planificado para esas fechas. Intenta con otras fechas para que no se crucen. 🚫");
        }

        // Procesar las etiquetas y asociarlas al grupo de viaje
        Set<Etiqueta> etiquetasAsociadas = etiquetaService.procesarEtiquetas(nombresEtiquetas);
        grupoViaje.setEtiquetas(etiquetasAsociadas);

        // Inicializar una lista para almacenar los itinerarios del grupo
        List<Itinerario> itinerariosDelGrupo = new ArrayList<>();

        // Verificar que los itinerarios propuestos no sean nulos o vacíos
        if (itinerariosPropuestos != null && !itinerariosPropuestos.isEmpty()) {
            // Calcular la duración del viaje en días (incluyendo el día de inicio y el de fin)
            long duracionViaje = ChronoUnit.DAYS.between(grupoViaje.getFechaInicio(), grupoViaje.getFechaFin()) + 1;

            // Verificar que el número de itinerarios propuestos coincida con la duración del viaje
            if (itinerariosPropuestos.size() != duracionViaje) {
                logger.warn("Itinerarios incorrectos: se esperaban {} días, pero se proporcionaron {}. Creador: {}",
                        duracionViaje, itinerariosPropuestos.size(), creador.getCorreo());
                throw new GrupoViajeException("La cantidad de días en el itinerario no coincide con la duración del viaje. 🗓️");
            }

            // Iterar sobre los itinerarios propuestos
            for (int i = 0; i < itinerariosPropuestos.size(); i++) {
                // Obtener el itinerario propuesto en la posición actual
                Itinerario itinerario = itinerariosPropuestos.get(i);
                // Asignar el número de día al itinerario
                itinerario.setDiaNumero(i + 1);
                // Establecer la relación con el grupo de viaje
                itinerario.setGrupo(grupoViaje);
                // Agregar el itinerario a la lista de itinerarios del grupo
                itinerariosDelGrupo.add(itinerario);
            }
        }
        grupoViaje.setItinerarios(itinerariosDelGrupo);

        // Establecer al creador como el primer participante del grupo de viaje
        ParticipanteGrupo participacionDelCreador = new ParticipanteGrupo();
        participacionDelCreador.setUsuario(creador);
        participacionDelCreador.setGrupo(grupoViaje);
        participacionDelCreador.setEstado("ACTIVO");

        grupoViaje.getParticipantes().add(participacionDelCreador);

        // Guardar el grupo de viaje
        return grupoViajeRepository.save(grupoViaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoViaje> obtenerTodosLosGruposActivos() {
        // Obtener todos los grupos de viaje activos
        return grupoViajeRepository.findByEstado("activo");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrupoViaje> obtenerGrupoPorId(Long id) {
        // Buscar el grupo de viaje por su ID
        return grupoViajeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoViaje> obtenerGruposParaUsuario(Usuario usuario) {
        // Buscar grupos de viaje en estado 'activo', 'en_curso' o 'finalizado', según el usuario
        return grupoViajeRepository.findGruposValidosByCreadorOrParticipante(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GrupoViaje> obtenerGruposParaUsuarioPaginado(Usuario usuario, int page, int size) {
        // Buscar grupos de viaje en estado 'activo', 'en_curso' o 'finalizado', según el usuario y en versión paginada
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return grupoViajeRepository.findGruposValidosByCreadorOrParticipante(usuario, pageable);
    }

    @Override
    public List<GrupoViaje> obtenerGruposFinalizadosParaPasaporte(Usuario usuario) {
        // Buscar grupos de viaje en estado 'finalizado' para mostrar en Perfil, según el usuario
        return grupoViajeRepository.findGruposFinalizadosByCreadorOrParticipante(usuario);
    }

    @Override
    public List<GrupoViaje> obtenerGruposActivosParaUsuario(Usuario usuario) {
        // Buscar grupos de viaje en estado 'activo' para mostrar en Perfil, según el usuario
        return grupoViajeRepository.findGruposActivosByCreadorOrParticipante(usuario);
    }

    @Override
    @Transactional
    public GrupoViaje actualizarGrupoViaje(Long id, GrupoViaje grupoViajeDetails, List<Itinerario> itinerariosActualizados, Usuario usuarioActual) {

        // Buscar el grupo de viaje existente, puede no existir
        GrupoViaje grupoExistente = grupoViajeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de actualizar un grupo inexistente. ID: {}", id);
                    return new EntityNotFoundException("Grupo de viaje con ID " + id + " no encontrado.");
                });

        // Comprobar si el ID del usuario que intenta editar el grupo es el mismo que el del creador
        if (!grupoExistente.getCreador().getId().equals(usuarioActual.getId())) {
            logger.warn("Usuario '{}' intentó editar el grupo ID {} sin permisos. Creador real: {}",
                    usuarioActual.getCorreo(), id, grupoExistente.getCreador().getCorreo());
            throw new SecurityException("No tienes permiso para editar este grupo de viaje.");
        }

        // Comprobar si está en estado "ACTIVO".
        if (!"activo".equalsIgnoreCase(grupoExistente.getEstado())) {
            logger.warn("Intento de editar grupo ID {} que no está activo (estado actual: {}). Usuario: {}",
                    id, grupoExistente.getEstado(), usuarioActual.getCorreo());
            throw new GrupoViajeException("Solo puedes editar grupos que estén activos. Este está " + grupoExistente.getEstado().toUpperCase() + ". 🛑");
        }

        // Comprobar si se está ingresando un link diferente al de los demás grupos activos
        validarLinkUnicoEntreActivos(grupoViajeDetails);

        Integer nuevoMaxParticipantes = grupoViajeDetails.getMaxParticipantes();
        int numeroActualParticipantes = grupoExistente.getParticipantes().size();
        if (nuevoMaxParticipantes < numeroActualParticipantes) {
            logger.warn("Intento de reducir participantes máximos a {} cuando ya hay {} en el grupo ID {}. Usuario: {}",
                    nuevoMaxParticipantes, numeroActualParticipantes, id, usuarioActual.getCorreo());
            throw new GrupoViajeException("No puedes reducir el límite a " + nuevoMaxParticipantes + " porque ya hay " + numeroActualParticipantes + " participantes en el grupo. 👥");
        }

        // Actualizar los campos del grupo de viaje que se pueden modificar
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

        // Obtener los itinerarios actuales del grupo
        List<Itinerario> itinerariosActuales = itinerarioService.obtenerItinerariosPorGrupo(grupoExistente);

        // Verificar que los itinerarios actualizados no sean nulos o vacíos
        if (itinerariosActualizados != null && !itinerariosActualizados.isEmpty()) {

            // Verificar que exista la misma cantidad de itinerarios actualizados que los existentes
            if (itinerariosActuales.size() != itinerariosActualizados.size()) {
                logger.warn("Cantidad de itinerarios desajustada al actualizar grupo ID {}. Existentes: {}, Nuevos: {}. Usuario: {}",
                        id, itinerariosActuales.size(), itinerariosActualizados.size(), usuarioActual.getCorreo());
                throw new GrupoViajeException("La cantidad de días del itinerario no coincide con el plan original del viaje. 📆");
            }

            // Iterar sobre los itinerarios actuales
            for (int i = 0; i < itinerariosActuales.size(); i++) {
                // Obtener el itinerario actual en la posición actual
                Itinerario itinerarioExistente = itinerariosActuales.get(i);
                // Obtener el itinerario actualizado en la posición actual
                Itinerario itinerarioDetalleNuevo = itinerariosActualizados.get(i);

                // Actualizar los campos del itinerario actual
                itinerarioExistente.setTitulo(itinerarioDetalleNuevo.getTitulo());
                itinerarioExistente.setDescripcion(itinerarioDetalleNuevo.getDescripcion());
            }
        }

        // Guardar el grupo de viaje actualizado
        return grupoViajeRepository.save(grupoExistente);
    }

    @Override
    @Transactional
    public void actualizarEstadosDeGrupos() {
        LocalDate hoy = LocalDate.now();
        
        // Buscar grupos ACTIVOS que ya deberían estar EN CURSO
        List<GrupoViaje> gruposParaIniciar = grupoViajeRepository.findByEstadoAndFechaInicioLessThanEqual("activo", hoy);
        for (GrupoViaje grupo : gruposParaIniciar) {
            grupo.setEstado("en_curso");
            grupoViajeRepository.save(grupo);
            System.out.println("Grupo ID " + grupo.getIdGrupo() + " actualizado a EN_CURSO.");

            // Cancelar solicitudes pendientes
            solicitudGrupoRepository.rechazarSolicitudesPendientesPorGrupo(grupo);
            System.out.println("Solicitudes pendientes del grupo ID " + grupo.getIdGrupo() + " fueron rechazadas automáticamente.");
        }

        // Buscar grupos EN CURSO que ya deberían estar FINALIZADOS
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

        // Buscar el grupo de viaje existente, puede no existir
        GrupoViaje grupoExistente = grupoViajeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de eliminar un grupo inexistente. ID: {}", id);
                    return new EntityNotFoundException("Grupo de viaje con ID " + id + " no encontrado.");
                });

        // Comprobar si el ID del usuario que intenta desactivar el grupo es el mismo que el del creador
        if (!grupoExistente.getCreador().getId().equals(usuarioActual.getId())) {
            logger.warn("Usuario '{}' intentó eliminar el grupo ID {} sin permisos. Creador real: {}",
                    usuarioActual.getCorreo(), id, grupoExistente.getCreador().getCorreo());
            throw new SecurityException("No tienes permiso para eliminar este grupo de viaje.");
        }

        // Solo se puede desactivar un grupo si está en estado "ACTIVO" o "FINALIZADO".
        if (!"activo".equalsIgnoreCase(grupoExistente.getEstado()) && !"finalizado".equalsIgnoreCase(grupoExistente.getEstado())) {
            logger.warn("El grupo ID {} está en estado '{}' y no se puede eliminar. Usuario: {}",
                    id, grupoExistente.getEstado(), usuarioActual.getCorreo());
            throw new GrupoViajeException("Solo puedes eliminar un viaje que esté activo o finalizado. ¡No te preocupes, pronto podrás hacerlo! 😊");
        }

        // Desactivar el grupo de viaje por su ID
        grupoViajeRepository.desactivarGrupoViaje(id);
    }

    // Método auxiliar para la validación de grupos de WhatsApp
    private void validarLinkUnicoEntreActivos(GrupoViaje grupoViaje) {
        boolean existe;

        if (grupoViaje.getIdGrupo() == null) {
            // Si es creación, buscar cualquier grupo activo con ese link
            existe = grupoViajeRepository.existsByLinkGrupoWhatsapp(grupoViaje.getLinkGrupoWhatsapp());
        } else {
            // Si es edición, ignorar su propio ID
            existe = grupoViajeRepository.existsByLinkGrupoWhatsappAndIdGrupoNot(grupoViaje.getLinkGrupoWhatsapp(), grupoViaje.getIdGrupo());
        }

        if (existe) {
            throw new GrupoViajeException("Ya existe un grupo activo con ese enlace de WhatsApp. Por favor, usa uno diferente. 🔗");
        }
    }

    //MÉTODO PARA FILTRAR LOS GRUPOS POR RANGOS DE FECHAS Y ESTADO (UNIÓN ENTRE MÓDULOS: BÚSQUEDA + GESTIÓN DE GRUPOS)
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
