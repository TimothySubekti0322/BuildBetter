// src/main/java/com/buildbetter/consult/validation/annotation/ValidConsultType.java
package com.buildbetter.consult.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.buildbetter.consult.validation.validator.ConsultTypeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ConsultTypeValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidConsultType {
    String message() default "type must be either 'offline' or 'online'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
