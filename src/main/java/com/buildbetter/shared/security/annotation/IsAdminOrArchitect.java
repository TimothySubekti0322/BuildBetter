// src/main/java/com/buildbetter/shared/security/annotation/IsAdminOrArchitect.java
package com.buildbetter.shared.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ADMIN','ARCHITECT')")
public @interface IsAdminOrArchitect {
}
