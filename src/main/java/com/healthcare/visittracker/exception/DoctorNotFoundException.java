package com.healthcare.visittracker.exception;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(Integer doctorId) {
        super("Doctor with id " + doctorId + " not found");
    }
}
