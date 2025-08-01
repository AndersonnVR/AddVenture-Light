package com.addventure.AddVenture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Itinerario;

@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {

    List<Itinerario> findByGrupoOrderByDiaNumeroAsc(GrupoViaje grupo);
    
}
