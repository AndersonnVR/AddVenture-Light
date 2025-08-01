package com.addventure.AddVenture.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//Esta clase representa una tabla de usuarios en la base de datos.
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

    // Constructor, getters y setters
    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String nombreUsuario, String fotoPerfil, String descripcion,
            String pais, String ciudad, LocalDate fechaNacimiento, String correo, String contrasenia, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreUsuario = nombreUsuario;
        this.fotoPerfil = fotoPerfil;
        this.descripcion = descripcion;
        this.pais = pais;
        this.ciudad = ciudad;
        this.fechaNacimiento = fechaNacimiento;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public List<Logro> getLogros() {
        return logros;
    }

    public void setLogros(List<Logro> logros) {
        this.logros = logros;
    }

    public List<Pasaporte> getPasaportes() {
        return pasaportes;
    }

    public void setPasaportes(List<Pasaporte> pasaportes) {
        this.pasaportes = pasaportes;
    }

    public List<Opinion> getOpinionesRealizadas() {
        return opinionesRealizadas;
    }

    public void setOpinionesRealizadas(List<Opinion> opinionesRealizadas) {
        this.opinionesRealizadas = opinionesRealizadas;
    }

    public List<Opinion> getOpinionesRecibidas() {
        return opinionesRecibidas;
    }

    public void setOpinionesRecibidas(List<Opinion> opinionesRecibidas) {
        this.opinionesRecibidas = opinionesRecibidas;
    }

    public LocalDateTime getUltimoIngreso() {
        return ultimoIngreso;
    }

    public void setUltimoIngreso(LocalDateTime ultimoIngreso) {
        this.ultimoIngreso = ultimoIngreso;
    }
    
}
