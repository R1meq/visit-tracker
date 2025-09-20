package com.healthcare.visittracker.service;

import com.healthcare.visittracker.dto.PatientPageResult;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientListDto findPatients(int page, int size, String search, List<Integer> doctorIds) {
        PatientPageResult result = patientRepository.findPatients(page, size, search, doctorIds);
        List<PatientVisitDto> rawData = result.getPatients();
        final long totalPatientCount = result.getTotalCount();
        if (rawData.isEmpty()) {
            return new PatientListDto(Collections.emptyList(), totalPatientCount);
        }

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
