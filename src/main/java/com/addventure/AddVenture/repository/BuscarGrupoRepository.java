package com.addventure.AddVenture.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.GrupoViaje;

@Repository
public interface BuscarGrupoRepository extends JpaRepository<GrupoViaje, Long> {

    @Query("SELECT g FROM GrupoViaje g WHERE g.estado = :estado")
    Page<GrupoViaje> findByEstado(@Param("estado") String estado, Pageable pageable);
    
    @Query("SELECT g FROM GrupoViaje g WHERE LOWER(g.destinoPrincipal) LIKE LOWER(CONCAT('%', :destinoPrincipal, '%')) AND g.estado = :estado")
    Page<GrupoViaje> findByDestinoPrincipalContainingIgnoreCaseAndEstado(@Param("destinoPrincipal") String destinoPrincipal, @Param("estado") String estado, Pageable pageable);
    
    @Query("SELECT g FROM GrupoViaje g WHERE g.fechaInicio >= :fechaInicio AND g.estado = :estado")
    Page<GrupoViaje> findByFechaInicioGreaterThanEqualAndEstado(@Param("fechaInicio") LocalDate fechaInicio, @Param("estado") String estado, Pageable pageable);

    @Query("SELECT g FROM GrupoViaje g WHERE g.fechaFin >= :fechaFin AND g.estado = :estado")
    Page<GrupoViaje> findByFechaFinGreaterThanEqualAndEstado(@Param("fechaFin") LocalDate fechaFin, @Param("estado") String estado, Pageable pageable);

    @Query("SELECT g FROM GrupoViaje g WHERE g.fechaInicio >= :fechaInicio AND g.fechaFin <= :fechaFin AND g.estado = :estado")
    Page<GrupoViaje> findByFechaInicioGreaterThanEqualAndFechaFinLessThanEqualAndEstado(LocalDate fechaInicio, LocalDate fechaFin, String estado, Pageable pageable);

    @Query("SELECT g FROM GrupoViaje g WHERE LOWER(g.destinoPrincipal) LIKE LOWER(CONCAT('%', :destinoPrincipal, '%')) AND g.fechaInicio >= :fechaInicio AND g.fechaFin <= :fechaFin AND g.estado = :estado")
    Page<GrupoViaje> findByDestinoPrincipalContainingIgnoreCaseAndFechaInicioGreaterThanEqualAndFechaFinLessThanEqualAndEstado(
        String destinoPrincipal,LocalDate fechaInicio, LocalDate fechaFin, String estado, Pageable pageable);

}
