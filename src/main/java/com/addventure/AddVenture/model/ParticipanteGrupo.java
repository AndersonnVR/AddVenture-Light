package com.addventure.AddVenture.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participante_grupo",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_grupo"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participacion")
    private Long idParticipacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoViaje grupo;

    @Column(name = "estado", length = 20)
    private String estado;
    
}
