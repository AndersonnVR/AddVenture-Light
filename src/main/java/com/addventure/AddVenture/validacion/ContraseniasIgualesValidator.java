package com.addventure.AddVenture.validacion;

import com.addventure.AddVenture.dto.RegistroUsuarioDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContraseniasIgualesValidator implements ConstraintValidator<ContraseniasIguales, RegistroUsuarioDTO> {
    @Override
    public boolean isValid(RegistroUsuarioDTO dto, ConstraintValidatorContext context) {
        if (dto.getContrasenia() == null || dto.getConfirmarContrasenia() == null) {
            return true;
        }

        boolean coinciden = dto.getContrasenia().equals(dto.getConfirmarContrasenia());

        if (!coinciden) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Las contraseñas no coinciden")
                    .addPropertyNode("confirmarContrasenia")
                    .addConstraintViolation();
        }

        return coinciden;
    }
}