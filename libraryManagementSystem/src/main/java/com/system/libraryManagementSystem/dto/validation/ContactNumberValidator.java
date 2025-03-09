package com.system.libraryManagementSystem.dto.validation;

import com.system.libraryManagementSystem.dto.validation.annotation.ContactNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactNumberValidator implements ConstraintValidator<ContactNumber, String> {
    @Override
    public boolean isValid(String contactNumber, ConstraintValidatorContext context) {

        if (contactNumber == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Contact number cannot be null")
                    .addConstraintViolation();
            return false;
        }
        if (!contactNumber.matches("^\\+?\\d{10}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid contact number. Pattern must be: +1234567890")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
