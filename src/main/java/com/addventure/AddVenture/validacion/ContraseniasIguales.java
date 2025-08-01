package com.addventure.AddVenture.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContraseniasIgualesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ContraseniasIguales {
    String message() default "Las contraseñas no coinciden";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
