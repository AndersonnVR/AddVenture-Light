package com.addventure.AddVenture.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "Etiqueta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiqueta {

    //Declarar los campos o atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etiqueta")
    private Integer idEtiqueta;

    @Column(name = "nombre_etiqueta", nullable = false, length = 50, unique = true)
    private String nombreEtiqueta;

    //Declarar las relaciones
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = "etiquetas")
    private Set<GrupoViaje> grupos = new HashSet<>();
}
