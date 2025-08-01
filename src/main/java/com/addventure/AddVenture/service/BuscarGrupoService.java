package com.addventure.AddVenture.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.addventure.AddVenture.model.GrupoViaje;

public interface BuscarGrupoService {

    Page<GrupoViaje> obtenerGrupos(Pageable pageable);

    Page<GrupoViaje> buscarGrupos(String destinoPrincipal, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

}
