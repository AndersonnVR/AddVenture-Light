package com.addventure.AddVenture.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Etiqueta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etiqueta")
    private Long idEtiqueta;

    @Column(name = "nombre_etiqueta", nullable = false, length = 50, unique = true)
    private String nombreEtiqueta;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = "etiquetas")
    private Set<GrupoViaje> grupos = new HashSet<>();
    
}
