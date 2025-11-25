package com.addventure.AddVenture.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Pasaporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pasaporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pasaporte")
    private Long id;

    @Column(name = "pais", nullable = false)
    private String pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

}
