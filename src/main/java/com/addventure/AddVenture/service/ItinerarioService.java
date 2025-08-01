package com.addventure.AddVenture.service;

import java.util.List;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;

public interface ItinerarioService {

    List<Itinerario> obtenerItinerariosPorGrupo(GrupoViaje grupoViaje);
    
}
