package com.buildbetter.plan.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.buildbetter.plan.constant.HouseFileType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HouseFileTypeValidator
        implements ConstraintValidator<ValidHouseFileType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {       
            return true;
        }
        boolean ok = HouseFileType.isValid(value);
        if (!ok) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                    "Type '" + value + "' is not recognised. "
                  + "Allowed values: " +
                    Arrays.stream(HouseFileType.values())
                          .map(HouseFileType::getValue)
                          .collect(Collectors.joining(", ")))
               .addConstraintViolation();
        }
        return ok;
    }
}
