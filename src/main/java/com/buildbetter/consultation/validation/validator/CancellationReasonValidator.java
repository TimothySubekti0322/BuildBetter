// src/main/java/com/buildbetter/consult/validation/validator/ConsultTypeValidator.java
package com.buildbetter.consultation.validation.validator;

import java.util.Set;

import com.buildbetter.consultation.constant.CancellationReason;
import com.buildbetter.consultation.validation.annotation.ValidCancellationReason;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CancellationReasonValidator
        implements ConstraintValidator<ValidCancellationReason, String> {

    private static final Set<String> ALLOWED = Set.of(CancellationReason.ARCHITECT_UNAVAILABLE.getReason(),
            CancellationReason.INVALID_PAYMENT.getReason());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) {
            // let @NotNull handle nulls if you want to forbid them
            return true;
        }
        return ALLOWED.contains(value.toLowerCase());
    }
}
