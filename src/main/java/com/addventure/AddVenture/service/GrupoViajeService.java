package com.addventure.AddVenture.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;
import com.addventure.AddVenture.model.Usuario;

public interface GrupoViajeService {

    GrupoViaje crearGrupoViaje(GrupoViaje grupoViaje, Usuario creador, Set<String> nombresEtiquetas, List<Itinerario> itinerariosPropuestos);

    List<GrupoViaje> obtenerTodosLosGruposActivos();

    Optional<GrupoViaje> obtenerGrupoPorId(Long id);

    List<GrupoViaje> obtenerGruposParaUsuario(Usuario usuario);

    Page<GrupoViaje> obtenerGruposParaUsuarioPaginado(Usuario usuario, int page, int size);
    
    List<GrupoViaje> obtenerGruposActivosParaUsuario(Usuario usuario);

    List<GrupoViaje> obtenerGruposFinalizadosParaPasaporte(Usuario usuario);

    GrupoViaje actualizarGrupoViaje(Long id, GrupoViaje grupoViajeDetails, List<Itinerario> itinerariosActualizados, Usuario usuarioActual);

    void actualizarEstadosDeGrupos();
    
    void desactivarGrupoViaje(Long id, Usuario usuarioActual);
    
    //Filtro
    Page<GrupoViaje> filtrarPorEstadosYFechas(LocalDate fechaInicio, LocalDate fechaFin, String estado, Usuario usuario, int page, int size);
}
