package com.addventure.AddVenture.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.addventure.AddVenture.model.Opinion;
import com.addventure.AddVenture.model.Usuario;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    List<Opinion> findByDestinatario(Usuario destinatario);
    
}