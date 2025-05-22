// src/main/java/com/buildbetter/consult/validation/validator/ConsultTypeValidator.java
package com.buildbetter.consult.validation.validator;

import java.util.Set;

import com.buildbetter.consult.validation.annotation.ValidConsultType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConsultTypeValidator
        implements ConstraintValidator<ValidConsultType, String> {

    private static final Set<String> ALLOWED = Set.of("offline", "online");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) {
            // let @NotNull handle nulls if you want to forbid them
            return true;
        }
        return ALLOWED.contains(value.toLowerCase());
    }
}
