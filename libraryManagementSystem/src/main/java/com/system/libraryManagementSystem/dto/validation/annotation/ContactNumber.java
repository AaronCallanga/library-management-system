package com.system.libraryManagementSystem.dto.validation.annotation;

import com.system.libraryManagementSystem.dto.validation.ContactNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })    //what type of field we will use the annotation
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContactNumberValidator.class)
public @interface ContactNumber {
    String message() default "Invalid contact number";
    Class<?>[] groups() default {}; // allows grouping of constraints (e.g., for different validation scenarios)
    Class<? extends Payload>[] payload() default {}; // Used to attach metadata to the constraint (e.g., severity level).
}
