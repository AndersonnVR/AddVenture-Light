package com.addventure.AddVenture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.Etiqueta;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
    
    Etiqueta findByNombreEtiqueta(String nombreEtiqueta);

}
