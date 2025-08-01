package com.addventure.AddVenture.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participante_grupo",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_grupo"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteGrupo {

    //Declarar el identificador único
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participacion")
    private Long idParticipacion;

    //Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    //Relación con GrupoViaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoViaje grupo;

    //Declarar el estado de la participación
    @Column(name = "estado", length = 20)
    private String estado;
    
}
