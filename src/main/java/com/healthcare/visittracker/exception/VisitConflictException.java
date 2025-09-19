package com.healthcare.visittracker.exception;

public class VisitConflictException extends RuntimeException {
    public VisitConflictException(String message) {
        super(message);
    }
}