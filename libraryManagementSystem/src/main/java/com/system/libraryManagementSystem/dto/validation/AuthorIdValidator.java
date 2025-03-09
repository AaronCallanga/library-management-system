package com.system.libraryManagementSystem.dto.validation;

import com.system.libraryManagementSystem.dto.validation.annotation.AuthorId;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorIdValidator implements ConstraintValidator<AuthorId, Long> {

    @Autowired
    AuthorRepository authorRepository;

    @Override
    public boolean isValid(Long authorId, ConstraintValidatorContext context) {
        if (authorId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The ID of the author cannot be null")
                    .addConstraintViolation();
            return false;
        }

        if (!authorRepository.existsById(authorId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Author with the ID " + authorId + " does not exist")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
