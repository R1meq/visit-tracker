package com.healthcare.visittracker.service;

import com.healthcare.visittracker.dto.PatientVisitDto;
import com.healthcare.visittracker.dto.PatientListDto;
import com.healthcare.visittracker.repository.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientListDto findPatients(int page, int size, String search, List<Integer> doctorIds) {
        List<PatientVisitDto> rawData = patientRepository.findPatients(page, size, search, doctorIds);
        long totalPatientCount = patientRepository.countPatients(search);
        if (rawData.isEmpty()) {
            return new PatientListDto(Collections.emptyList(), totalPatientCount);
        }
        addDoctorTotalPatients(rawData);

        Map<PatientKey, List<PatientListDto.Visit>> groupedPatients = rawData.stream()
                .collect(Collectors.groupingBy(
                        PatientKey::fromDto,
                        Collectors.filtering(PatientVisitDto::hasVisit,
                                Collectors.mapping(PatientListDto.Visit::fromDto, Collectors.toList()))
                ));

        List<PatientListDto.PatientData> patients = groupedPatients.entrySet().stream()
                .map(entry -> PatientListDto.PatientData.builder()
                        .firstName(entry.getKey().getFirstName())
                        .lastName(entry.getKey().getLastName())
                        .lastVisits(entry.getValue())
                        .build())
                .toList();

        return new PatientListDto(patients, totalPatientCount);
    }

    private void addDoctorTotalPatients(List<PatientVisitDto> rawData) {
        Set<Integer> uniqueDoctorIds = rawData.stream()
                .map(PatientVisitDto::getDoctorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueDoctorIds.isEmpty()) {
            return;
        }
        Map<Integer, Long> doctorCounts = patientRepository.getDoctorPatientCounts(uniqueDoctorIds);
        rawData.stream()
                .filter(p -> p.getDoctorId() != null)
                .forEach(p -> p.setDoctorTotalPatients(doctorCounts.getOrDefault(p.getDoctorId(), 0L)));
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PatientKey {
        @EqualsAndHashCode.Include
        private Integer patientId;
        private String firstName;
        private String lastName;

        public static PatientKey fromDto(PatientVisitDto dto) {
            return new PatientKey(dto.getPatientId(), dto.getPatientFirstName(), dto.getPatientLastName());
        }
    }

}
