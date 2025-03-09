package com.system.libraryManagementSystem.dto.validation.annotation;

import com.system.libraryManagementSystem.dto.validation.PublicationYearValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PublicationYearValidator.class)
public @interface PublicationYear {
    String message() default "Invalid publication year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
