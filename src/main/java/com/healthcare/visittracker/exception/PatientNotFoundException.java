package com.healthcare.visittracker.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Integer patientId) {
        super("Patient with id " + patientId + " not found");
    }
}