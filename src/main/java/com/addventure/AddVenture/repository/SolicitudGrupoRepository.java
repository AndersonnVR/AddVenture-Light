package com.addventure.AddVenture.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.EstadoSolicitud;
import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.SolicitudGrupo;
import com.addventure.AddVenture.model.Usuario;

@Repository
public interface SolicitudGrupoRepository extends JpaRepository<SolicitudGrupo, Long> {

    // Verificar si el usuario ya tiene una solicitud en este grupo
    Optional<SolicitudGrupo> findBySolicitanteAndGrupo(Usuario solicitante, GrupoViaje grupo);

    // Obtener todas las solicitudes pendientes de un grupo (o de cualquier otro estado)
    List<SolicitudGrupo> findByGrupoAndEstado(GrupoViaje grupo, EstadoSolicitud estado);

    // Obtener todas las solicitudes enviadas por un usuario
    List<SolicitudGrupo> findBySolicitante(Usuario solicitante);

    @Modifying
    @Query("UPDATE SolicitudGrupo s SET s.estado = 'RECHAZADA' WHERE s.grupo = :grupo AND s.estado = 'PENDIENTE'")
    void rechazarSolicitudesPendientesPorGrupo(@Param("grupo") GrupoViaje grupo);

    @Query("SELECT s.grupo FROM SolicitudGrupo s JOIN s.grupo g WHERE s.solicitante = :usuario AND s.estado = 'PENDIENTE' ORDER BY g.fechaInicio DESC")
    Page<GrupoViaje> findGruposConSolicitudPendiente(@Param("usuario") Usuario usuario, Pageable pageable);
    
    //Método para filtrado -- por ver
    @Query("SELECT s.grupo FROM SolicitudGrupo s JOIN s.grupo g WHERE s.solicitante = :usuario AND s.estado = 'PENDIENTE' AND LOWER(g.destinoPrincipal) LIKE LOWER(CONCAT('%', :destinoPrincipal, '%')) AND g.fechaInicio <= :fechaFin AND g.fechaFin >= :fechaInicio")
    Page<GrupoViaje> findGruposConSolicitudesPendienteFiltrado(@Param("usuario") Usuario usuario, @Param("destinoPrincipal") String destinoPrincipal, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin, Pageable pageable);
}
