package com.addventure.AddVenture.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.addventure.AddVenture.model.GrupoViaje;
import com.addventure.AddVenture.model.ParticipanteGrupo;
import com.addventure.AddVenture.model.Usuario;

@Repository
public interface ParticipanteGrupoRepository extends JpaRepository<ParticipanteGrupo, Long> {
    
    // Verificar si el usuario ya se encuentra participando en este grupo
    Optional<ParticipanteGrupo> findByUsuarioAndGrupo(Usuario usuario, GrupoViaje grupo);

    // Obtener las participaciones de un usuario
    List<ParticipanteGrupo> findByUsuario(Usuario usuario);

    // Obtener los participantes de un grupo
    List<ParticipanteGrupo> findByGrupo(GrupoViaje grupo);

    @Query("SELECT COUNT(p) > 0 FROM ParticipanteGrupo p WHERE p.usuario = :usuario AND p.grupo.estado IN ('activo', 'en_curso') AND ((:inicio BETWEEN p.grupo.fechaInicio AND p.grupo.fechaFin) OR (:fin BETWEEN p.grupo.fechaInicio AND p.grupo.fechaFin) OR (p.grupo.fechaInicio BETWEEN :inicio AND :fin))")
    boolean existeConflictoDeFechasParaParticipante(@Param("usuario") Usuario usuario, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
}
