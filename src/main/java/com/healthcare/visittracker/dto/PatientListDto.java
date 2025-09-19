package com.healthcare.visittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientListDto {
    private List<PatientData> data;
    private long count;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientData {
        private String firstName;
        private String lastName;
        private List<Visit> lastVisits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Doctor {
        private String firstName;
        private String lastName;
        private Long totalPatients;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Visit {
        private String start;
        private String end;
        private Doctor doctor;

        public static Visit fromDto(PatientVisitDto dto) {
            return Visit.builder()
                    .start(dto.getVisitStart().toString())
                    .end(dto.getVisitEnd().toString())
                    .doctor(Doctor.builder()
                            .firstName(dto.getDoctorFirstName())
                            .lastName(dto.getDoctorLastName())
                            .totalPatients(dto.getDoctorTotalPatients())
                            .build())
                    .build();
        }
    }
}