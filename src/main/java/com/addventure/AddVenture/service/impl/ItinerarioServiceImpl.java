package com.addventure.AddVenture.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;
import com.addventure.AddVenture.repository.ItinerarioRepository;
import com.addventure.AddVenture.service.ItinerarioService;

@Service
public class ItinerarioServiceImpl implements ItinerarioService {

    private final ItinerarioRepository itinerarioRepository;

    @Autowired
    public ItinerarioServiceImpl(ItinerarioRepository itinerarioRepository) {
        this.itinerarioRepository = itinerarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Itinerario> obtenerItinerariosPorGrupo(GrupoViaje grupoViaje) {
        return itinerarioRepository.findByGrupoOrderByDiaNumeroAsc(grupoViaje);
    }
    
}
