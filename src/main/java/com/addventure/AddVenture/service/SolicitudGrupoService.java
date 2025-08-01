package com.addventure.AddVenture.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.SolicitudGrupo;
import com.addventure.AddVenture.model.Usuario;

public interface SolicitudGrupoService {

    void enviarSolicitud(Usuario solicitante, GrupoViaje grupo);

    void cancelarSolicitud(Long idGrupo, Usuario usuarioActual);

    Long aceptarSolicitud(Long idSolicitud, Usuario creador);

    Long rechazarSolicitud(Long idSolicitud, Usuario creador);

    List<SolicitudGrupo> obtenerSolicitudesPendientesDeGrupo(GrupoViaje grupo);

    Optional<SolicitudGrupo> obtenerSolicitudPorUsuarioYGrupo(Usuario usuario, GrupoViaje grupo);

    Page<GrupoViaje> obtenerGruposConSolicitudesPendientesPaginado(Usuario usuario, int page, int size);
    
    //NUEVO MÉTODO
    Page<GrupoViaje> obtenerGruposFiltrados(Usuario usuario, String destino, LocalDate fechaInicio, LocalDate fechaFin, int page, int size);
}
