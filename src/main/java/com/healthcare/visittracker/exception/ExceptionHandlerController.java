package com.healthcare.visittracker.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DoctorNotFoundException.class)
    public ErrorResponse handleDoctorNotFound(DoctorNotFoundException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PatientNotFoundException.class)
    public ErrorResponse handlePatientNotFound(PatientNotFoundException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(VisitConflictException.class)
    public ErrorResponse handleVisitConflict(VisitConflictException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT)
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidVisitTimeException.class)
    public ErrorResponse handleInvalidVisitTime(InvalidVisitTimeException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse invalidArgumentTypeHandler(MethodArgumentTypeMismatchException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse invalidArgumentHandler(MethodArgumentNotValidException ex) {
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        StringBuilder errorsString = new StringBuilder();
        for (String error : errors) {
            errorsString.append(error);
            errorsString.append(", ");
        }
        if (!errorsString.isEmpty()) {
            errorsString.setLength(errorsString.length() - 2);
        }
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errorsString.toString())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorResponse {
        private String message;
        private HttpStatus status;
        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();
        private List<String> details;
    }
}
