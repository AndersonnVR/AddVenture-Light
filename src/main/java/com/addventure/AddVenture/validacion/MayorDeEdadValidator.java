package com.addventure.AddVenture.validacion;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {
    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            return false; // El campo es obligatorio
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.minusYears(18);

        return fechaNacimiento.isBefore(fechaLimite) || fechaNacimiento.isEqual(fechaLimite);
    }
}
