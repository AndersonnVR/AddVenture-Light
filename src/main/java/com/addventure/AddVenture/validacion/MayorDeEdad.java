package com.addventure.AddVenture.validacion;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MayorDeEdadValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MayorDeEdad {
    String message() default "Debes tener al menos 18 años";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
