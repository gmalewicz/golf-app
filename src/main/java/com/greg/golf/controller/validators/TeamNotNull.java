package com.greg.golf.controller.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TeamNotNullValidator.class)
@Documented
public @interface TeamNotNull {

    String message() default "b must not be null when a is 1";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
