package com.addventure.AddVenture.service.impl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.repository.BuscarGrupoRepository;
import com.addventure.AddVenture.service.BuscarGrupoService;

@Service
public class BuscarGrupoServiceImpl implements BuscarGrupoService {

    private final BuscarGrupoRepository buscarGrupoRepository;

    @Autowired
    public BuscarGrupoServiceImpl(BuscarGrupoRepository buscarGrupoRepository) {
        this.buscarGrupoRepository = buscarGrupoRepository;
    }

    @Override
    public Page<GrupoViaje> obtenerGrupos(Pageable pageable) {
        return buscarGrupoRepository.findByEstado("activo", pageable);
    }

    @Override
    public Page<GrupoViaje> buscarGrupos(String destinoPrincipal, LocalDate fechaInicio, LocalDate fechaFin,
            Pageable pageable) {
        if (destinoPrincipal != null && !destinoPrincipal.isBlank() && fechaInicio != null && fechaFin != null) {
            return buscarGrupoRepository
                    .findByDestinoPrincipalContainingIgnoreCaseAndFechaInicioGreaterThanEqualAndFechaFinLessThanEqualAndEstado(
                            destinoPrincipal, fechaInicio, fechaFin, "activo", pageable);
        } else if (destinoPrincipal != null && !destinoPrincipal.isBlank()) {
            return buscarGrupoRepository.findByDestinoPrincipalContainingIgnoreCaseAndEstado(destinoPrincipal, "activo",
                    pageable);
        } else if (fechaInicio != null && fechaFin != null) {
            return buscarGrupoRepository.findByFechaInicioGreaterThanEqualAndFechaFinLessThanEqualAndEstado(fechaInicio,
                    fechaFin, "activo", pageable);
        } else if (fechaInicio != null) {
            return buscarGrupoRepository.findByFechaInicioGreaterThanEqualAndEstado(fechaInicio, "activo", pageable);
        } else if (fechaFin != null) {
            return buscarGrupoRepository.findByFechaFinGreaterThanEqualAndEstado(fechaFin, "activo", pageable);
        } else {
            return buscarGrupoRepository.findByEstado("activo", pageable);
        }
    }

}
