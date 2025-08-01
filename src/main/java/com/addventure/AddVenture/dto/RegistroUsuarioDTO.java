package com.addventure.AddVenture.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.addventure.AddVenture.validacion.ContraseniasIguales;
import com.addventure.AddVenture.validacion.MayorDeEdad;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//Esta clase representa el DTO (Data Transfer Object) para el registro de un usuario.
@ContraseniasIguales
public class RegistroUsuarioDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 30)
    private String nombreUsuario;

    @Email(message = "Correo electrónico no válido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasenia;

    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmarContrasenia;

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @MayorDeEdad(message = "Debes tener al menos 18 años")
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @Size(max = 500)
    private String descripcion;

    private MultipartFile fotoPerfil;


    // Getters y setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getConfirmarContrasenia() {
        return confirmarContrasenia;
    }

    public void setConfirmarContrasenia(String confirmarContrasenia) {
        this.confirmarContrasenia = confirmarContrasenia;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public MultipartFile getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(MultipartFile fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
    
}