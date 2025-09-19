package com.healthcare.visittracker.validation;

import com.healthcare.visittracker.dto.VisitRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VisitTimeValidator implements ConstraintValidator<ValidVisitTime, VisitRequest> {
    @Override
    public boolean isValid(VisitRequest request, ConstraintValidatorContext context) {
        if (request.getStart() == null || request.getEnd() == null) {
            return true;
        }
        return request.getStart().isBefore(request.getEnd());
    }
}