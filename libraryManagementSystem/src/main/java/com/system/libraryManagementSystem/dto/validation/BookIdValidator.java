package com.system.libraryManagementSystem.dto.validation;

import com.system.libraryManagementSystem.dto.validation.annotation.BookId;
import com.system.libraryManagementSystem.repository.BookRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookIdValidator implements ConstraintValidator<BookId, Long> {

    @Autowired
    BookRepository bookRepository;

    @Override
    public boolean isValid(Long bookId, ConstraintValidatorContext context) {
        if (bookId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The ID of a book cannot be null")
                    .addConstraintViolation();
            return false;
        }
        if (!bookRepository.existsById(bookId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Book with the ID " + bookId + " does not exist")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
