package com.healthcare.visittracker.dto;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PatientVisitDto {
    private Integer patientId;
    private String patientFirstName;
    private String patientLastName;
    private LocalDateTime visitStart;
    private LocalDateTime visitEnd;
    private Long doctorTotalPatients;
    private String doctorFirstName;
    private String doctorLastName;

    public boolean hasVisit() {
        return visitStart != null;
    }

    public static PatientVisitDto fromTuple(Tuple tuple) {
        String doctorTimezone = (String) tuple.get("doctor_timezone");
        return PatientVisitDto.builder()
                .patientId((Integer) tuple.get("patient_id"))
                .patientFirstName((String) tuple.get("patient_first_name"))
                .patientLastName((String) tuple.get("patient_last_name"))
                .doctorFirstName((String) tuple.get("doctor_first_name"))
                .doctorLastName((String) tuple.get("doctor_last_name"))
                .doctorTotalPatients((Long) tuple.get("doctor_total_patients"))
                .visitStart(convertToDoctorZone((Timestamp) tuple.get("latest_visit_start"), doctorTimezone))
                .visitEnd(convertToDoctorZone((Timestamp) tuple.get("latest_visit_end"), doctorTimezone))
                .build();
    }

    private static LocalDateTime convertToDoctorZone(Timestamp timestamp, String doctorTimezone) {
        if (timestamp == null || doctorTimezone == null) return null;
        return timestamp.toLocalDateTime()
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of(doctorTimezone))
                .toLocalDateTime();
    }
}
