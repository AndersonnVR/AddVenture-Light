package com.addventure.AddVenture.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "Itinerario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itinerario")
    private Long idItinerario;

    @NotNull(message = "El número de día es obligatorio.")
    @Min(value = 1, message = "El número de día debe ser mayor a 0.")
    @Column(name = "dia_numero", nullable = false)
    private Integer diaNumero;

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres.")
    @Column(length = 100, nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoViaje grupo;
    
}
