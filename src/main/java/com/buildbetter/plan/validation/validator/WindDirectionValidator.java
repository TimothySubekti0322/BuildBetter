package com.buildbetter.plan.validation.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buildbetter.plan.validation.annotation.ValidWindDirection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WindDirectionValidator
        implements ConstraintValidator<ValidWindDirection, List<String>> {

    private static final Set<String> ALLOWED = Set.of("east", "south", "west", "north");

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext ctx) {
        if (values == null) {
            // let @NotNull/@Size handle null or empty
            return true;
        }
        // check no duplicates
        Set<String> unique = new HashSet<>(values);
        if (unique.size() != values.size()) {
            return false;
        }
        // check all values are allowed
        return ALLOWED.containsAll(values);
    }
}
