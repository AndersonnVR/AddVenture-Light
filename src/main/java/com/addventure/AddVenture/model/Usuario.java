package com.addventure.AddVenture.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//Esta clase representa una tabla de usuarios en la base de datos.
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Usuario")
public class Usuario {

    //Declarar los campos o atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Datos personales ===
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String apellido;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 30)
    @Column(unique = true, nullable = false)
    private String nombreUsuario;

    @Size(max = 255)
    private String fotoPerfil; // Opcional → no se marca con @NotBlank

    @Size(max = 500)
    private String descripcion; // Opcional

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    // === Datos de autenticación ===
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo electrónico no válido")
    @Column(unique = true, nullable = false)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String contrasenia;

    @NotBlank(message = "El rol es obligatorio")
    private String rol; // Ej: ROLE_USER, ROLE_ADMIN

    private LocalDateTime ultimoIngreso;

    // RELACIONES EXISTENTES
    @ManyToMany
    @JoinTable(name = "usuario_logro", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "logro_id"))
    private List<Logro> logros = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pasaporte> pasaportes = new ArrayList<>();

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
    private List<Opinion> opinionesRealizadas = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opinion> opinionesRecibidas = new ArrayList<>();

    @Getter
    @Setter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "creador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrupoViaje> gruposCreados = new ArrayList<>();

    @Getter
    @Setter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipanteGrupo> participaciones = new ArrayList<>();

    @Getter
    @Setter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "solicitante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudGrupo> solicitudesEnviadas = new ArrayList<>();
}
