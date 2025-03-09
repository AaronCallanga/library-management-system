package com.system.libraryManagementSystem.dto.validation;

import com.system.libraryManagementSystem.dto.validation.annotation.MemberId;
import com.system.libraryManagementSystem.repository.MemberRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberIdValidator implements ConstraintValidator<MemberId, Long> {

    @Autowired
    MemberRepository memberRepository;

    @Override
    public boolean isValid(Long memberId, ConstraintValidatorContext context) {
        if (memberId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The ID of a member cannot be null")
                    .addConstraintViolation();
            return false;
        }
        if (!memberRepository.existsById(memberId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Member with the ID " + memberId + " does not exist")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
