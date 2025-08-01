package com.addventure.AddVenture.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.Usuario;

@Repository
public interface GrupoViajeRepository extends JpaRepository<GrupoViaje, Long>{
    
    List<GrupoViaje> findByEstado(String estado);

    @Query("SELECT DISTINCT g FROM GrupoViaje g LEFT JOIN g.participantes p WHERE g.estado IN ('activo', 'en_curso', 'finalizado') AND (g.creador = :usuario OR p.usuario = :usuario)")
    List<GrupoViaje> findGruposValidosByCreadorOrParticipante(@Param("usuario") Usuario usuario);

    @Query("SELECT DISTINCT g FROM GrupoViaje g LEFT JOIN g.participantes p WHERE g.estado IN ('activo', 'en_curso', 'finalizado') AND (g.creador = :usuario OR p.usuario = :usuario)")
    Page<GrupoViaje> findGruposValidosByCreadorOrParticipante(@Param("usuario") Usuario usuario, Pageable pageable);

    @Query("SELECT DISTINCT g FROM GrupoViaje g LEFT JOIN g.participantes p WHERE g.estado = 'activo' AND (g.creador = :usuario OR p.usuario = :usuario)")
    List<GrupoViaje> findGruposActivosByCreadorOrParticipante(@Param("usuario") Usuario usuario);

    @Query("SELECT DISTINCT g FROM GrupoViaje g LEFT JOIN g.participantes p WHERE g.estado = 'finalizado' AND (g.creador = :usuario OR p.usuario = :usuario)")
    List<GrupoViaje> findGruposFinalizadosByCreadorOrParticipante(@Param("usuario") Usuario usuario);

    List<GrupoViaje> findByEstadoAndFechaInicioLessThanEqual(String estado, LocalDate fecha);

    List<GrupoViaje> findByEstadoAndFechaFinBefore(String estado, LocalDate fecha);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GrupoViaje g SET g.estado = 'desactivado' WHERE g.idGrupo = :idGrupo")
    void desactivarGrupoViaje(@Param("idGrupo") Long idGrupo);

    boolean existsByLinkGrupoWhatsapp(String linkGrupoWhatsapp);

    boolean existsByLinkGrupoWhatsappAndIdGrupoNot(String linkGrupoWhatsapp, Long idGrupo);

    @Query("SELECT COUNT(g) > 0 FROM GrupoViaje g WHERE g.creador = :creador AND g.estado IN ('activo', 'en_curso') AND ((:inicio BETWEEN g.fechaInicio AND g.fechaFin) OR (:fin BETWEEN g.fechaInicio AND g.fechaFin) OR (g.fechaInicio BETWEEN :inicio AND :fin))")
    boolean existeConflictoDeFechasParaCreador(@Param("creador") Usuario creador, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // Útil para el filtro de grupos (unión entre módulos: búsqueda de grupos + gestión de grupos)
    @Query("SELECT DISTINCT g FROM GrupoViaje g LEFT JOIN g.participantes p WHERE g.estado = :estado AND g.fechaInicio >= :fechaInicio AND g.fechaFin <= :fechaFin AND (g.creador = :usuario OR p.usuario = :usuario)")
    Page<GrupoViaje> findGruposPorEstadoYRangoFechas(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin, @Param("estado") String estado, @Param("usuario") Usuario usuario, Pageable pageable);
    
}
