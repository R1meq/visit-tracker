package com.healthcare.visittracker.exception;

public class InvalidVisitTimeException extends RuntimeException {
    public InvalidVisitTimeException(String message) {
        super(message);
    }
}
