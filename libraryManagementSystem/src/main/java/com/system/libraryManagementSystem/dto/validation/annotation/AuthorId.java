package com.system.libraryManagementSystem.dto.validation.annotation;

import com.system.libraryManagementSystem.dto.validation.AuthorIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AuthorIdValidator.class)
public @interface AuthorId {
    String message() default "Invalid author's id";    //the message is from the context, but you can set it here if you dont provide it in the context (AuthorIdValidator)
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
