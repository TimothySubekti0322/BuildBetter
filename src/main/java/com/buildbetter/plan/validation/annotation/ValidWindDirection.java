// src/main/java/com/buildbetter/plan/validation/ValidWindDirection.java
package com.buildbetter.plan.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.buildbetter.plan.validation.validator.WindDirectionValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = WindDirectionValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidWindDirection {
    String message() default "Each windDirection must be one of [east, south, west, north] with no duplicates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
