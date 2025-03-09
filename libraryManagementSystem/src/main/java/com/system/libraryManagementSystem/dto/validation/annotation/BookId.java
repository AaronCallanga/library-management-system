package com.system.libraryManagementSystem.dto.validation.annotation;

import com.system.libraryManagementSystem.dto.validation.BookIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookIdValidator.class)
public @interface BookId {
    String message() default "Invalid book's id";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
