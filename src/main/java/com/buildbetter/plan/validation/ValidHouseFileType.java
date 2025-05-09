package com.buildbetter.plan.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = HouseFileTypeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHouseFileType {

  String message() default "Invalid house file type: '${validatedValue}'. "
      + "Allowed: house_image_front, house_image_back, "
      + "house_image_side, house_object, pdf";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}