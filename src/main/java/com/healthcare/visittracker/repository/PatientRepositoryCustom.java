package com.healthcare.visittracker.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.healthcare.visittracker.dto.PatientVisitDto;

public interface PatientRepositoryCustom {
    List<PatientVisitDto> findPatients(int page, int size, String search, List<Integer> doctorIds);
    Map<Integer, Long> getDoctorPatientCounts(Set<Integer> doctorIds);
    long countPatients(String search);
}
