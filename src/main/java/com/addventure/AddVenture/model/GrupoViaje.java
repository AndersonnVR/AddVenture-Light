package com.addventure.AddVenture.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.addventure.AddVenture.validacion.ValidacionUsuario;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "GrupoViaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoViaje {

    //Declarar los campos o atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long idGrupo;

    @NotBlank(message = "El nombre del viaje es obligatorio.")
    @Size(max = 100, message = "El nombre del viaje no puede superar los 100 caracteres.")
    @Column(name = "nombre_viaje", nullable = false, length = 100)
    private String nombreViaje;

    @NotBlank(message = "El destino principal es obligatorio.")
    @Size(max = 100, message = "El destino principal no puede superar los 100 caracteres.")
    @Column(name = "destino_principal", nullable = false, length = 100)
    private String destinoPrincipal;

    @NotNull(message = "La fecha de inicio es obligatoria.", groups = ValidacionUsuario.class)
    @Future(message = "La fecha de inicio debe ser una fecha futura (mínimo el día de mañana).", groups = ValidacionUsuario.class)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.", groups = ValidacionUsuario.class)
    @Future(message = "La fecha de fin debe ser una fecha futura.", groups = ValidacionUsuario.class)
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @NotNull(message = "La edad mínima es obligatoria.")
    @Min(value = 18, message = "La edad mínima debe ser al menos 18 años.")
    @Column(name = "rango_edad_min", nullable = false)
    private Integer rangoEdadMin;

    @NotNull(message = "La edad máxima es obligatoria.")
    @Max(value = 65, message = "La edad máxima no puede superar los 65 años.")
    @Column(name = "rango_edad_max", nullable = false)
    private Integer rangoEdadMax;

    @NotBlank(message = "El tipo de grupo es obligatorio.")
    @Size(max = 50, message = "El tipo de grupo no puede superar los 50 caracteres.")
    @Column(name = "tipo_grupo", nullable = false)
    private String tipoGrupo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank(message = "El punto de encuentro es obligatorio.")
    @Column(name = "punto_encuentro", columnDefinition = "TEXT", nullable = false)
    private String puntoEncuentro;

    @NotBlank(message = "La imagen destacada es obligatoria.")
    @Column(name = "imagen_destacada", columnDefinition = "TEXT", nullable = false)
    private String imagenDestacada;

    @NotNull(message = "Debe especificarse el número máximo de participantes.")
    @Min(value = 2, message = "Debe haber al menos dos participantes.")
    @Column(name = "num_participantes", nullable = false)
    private Integer maxParticipantes;

    @NotBlank(message = "El enlace del grupo de WhatsApp es obligatorio.")
    @Pattern(
        regexp = "^https://chat\\.whatsapp\\.com/[A-Za-z0-9]{22}/?$",
        message = "El enlace debe ser un link válido de grupo de WhatsApp.")
    @Column(name = "link_grupo_whatsapp", nullable = false)
    private String linkGrupoWhatsapp;

    @Column(name = "es_verificado", nullable = false)
    private Boolean esVerificado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(length = 20, nullable = false)
    private String estado = "activo";

    //Declarar las relaciones
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("diaNumero ASC")
    private List<Itinerario> itinerarios = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "GrupoEtiqueta", 
        joinColumns = @JoinColumn(name = "id_grupo"), 
        inverseJoinColumns = @JoinColumn(name = "id_etiqueta")
    )
    private Set<Etiqueta> etiquetas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador", nullable = false)
    private Usuario creador;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipanteGrupo> participantes = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudGrupo> solicitudes = new ArrayList<>();

}