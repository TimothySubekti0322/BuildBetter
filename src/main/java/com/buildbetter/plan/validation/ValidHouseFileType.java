package com.buildbetter.plan.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = HouseFileTypeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHouseFileType {

    String message() default
        "Invalid house file type: '${validatedValue}'. "
      + "Allowed: house_image_front, house_image_back, "
      + "house_image_side, house_object, floor_plans";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}