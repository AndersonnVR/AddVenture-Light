package com.addventure.AddVenture.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Logro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logro")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_url", nullable = false)
    private String imagenUrl;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = "logros")
    private List<Usuario> usuarios = new ArrayList<>();

}
