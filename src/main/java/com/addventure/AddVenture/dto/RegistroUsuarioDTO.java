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
import lombok.Data;

@Data //Genera Automaticamente los Getters and Setters
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
}