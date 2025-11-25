package com.addventure.AddVenture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.Logro;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Long> {

    Optional<Logro> findByNombre(String nombre);
    
}