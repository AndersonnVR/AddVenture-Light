package com.addventure.AddVenture.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Opinion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opinion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(name = "fecha", nullable = false)
    private String fecha;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_estrellas", nullable = false)
    private String imagenEstrellas;

}
