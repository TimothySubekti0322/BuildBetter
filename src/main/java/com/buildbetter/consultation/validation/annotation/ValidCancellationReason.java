// src/main/java/com/buildbetter/consult/validation/annotation/ValidConsultType.java
package com.buildbetter.consultation.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.buildbetter.consultation.validation.validator.CancellationReasonValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CancellationReasonValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidCancellationReason {
    String message() default "type must be either 'architect is unavailable' or 'proof of payment is invalid'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
