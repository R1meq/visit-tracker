package com.healthcare.visittracker.service;

import com.healthcare.visittracker.dto.VisitRequest;
import com.healthcare.visittracker.dto.VisitDto;
import com.healthcare.visittracker.entity.Doctor;
import com.healthcare.visittracker.entity.Patient;
import com.healthcare.visittracker.entity.Visit;
import com.healthcare.visittracker.exception.DoctorNotFoundException;
import com.healthcare.visittracker.exception.InvalidVisitTimeException;
import com.healthcare.visittracker.exception.PatientNotFoundException;
import com.healthcare.visittracker.exception.VisitConflictException;
import com.healthcare.visittracker.mapper.VisitMapper;
import com.healthcare.visittracker.repository.DoctorRepository;
import com.healthcare.visittracker.repository.PatientRepository;
import com.healthcare.visittracker.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;

    @Transactional
    public VisitDto createVisit(VisitRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(request.getDoctorId()));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        ZoneId doctorZone = ZoneId.of(doctor.getTimezone());
        validateFutureTimes(request, doctorZone);

        Instant startInstant = request.getStart().atZone(doctorZone).toInstant();
        Instant endInstant = request.getEnd().atZone(doctorZone).toInstant();
        if (visitRepository.hasVisitConflict(doctor.getId(), patient.getId(), startInstant, endInstant)) {
            throw new VisitConflictException("Doctor or patient already has a visit at this time");
        }

        Visit visit = Visit.builder()
                .doctor(doctor)
                .patient(patient)
                .startDateTime(startInstant)
                .endDateTime(endInstant)
                .build();

        Visit savedVisit = visitRepository.save(visit);
        return visitMapper.toDto(savedVisit);
    }

    private void validateFutureTimes(VisitRequest request, ZoneId doctorZone) {
        LocalDateTime nowInDoctorZone = LocalDateTime.now(doctorZone);
        if (request.getStart().isBefore(nowInDoctorZone)) {
            throw new InvalidVisitTimeException("Start time must be in the future");
        }
        if (request.getEnd().isBefore(nowInDoctorZone)) {
            throw new InvalidVisitTimeException("End time must be in the future");
        }
    }
}
