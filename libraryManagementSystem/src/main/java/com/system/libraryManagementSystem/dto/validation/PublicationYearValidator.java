package com.system.libraryManagementSystem.dto.validation;

import com.system.libraryManagementSystem.dto.validation.annotation.PublicationYear;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class PublicationYearValidator implements ConstraintValidator<PublicationYear, Integer> {

    @Override
    public boolean isValid(Integer publicationYear, ConstraintValidatorContext context) {
        if (publicationYear == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The publication year of a book cannot be null")
                    .addConstraintViolation();
            return false;
        }
        if (!(publicationYear <= Year.now().getValue() && publicationYear >= 0)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid publication year. Enter only between 0 to " + Year.now().getValue())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
