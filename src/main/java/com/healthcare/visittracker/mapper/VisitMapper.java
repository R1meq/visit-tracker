package com.healthcare.visittracker.mapper;

import com.healthcare.visittracker.dto.VisitDto;
import com.healthcare.visittracker.entity.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class VisitMapper {
    public VisitDto toDto(Visit visit) {
        ZoneId doctorZone = ZoneId.of(visit.getDoctor().getTimezone());
        return VisitDto.builder()
                .id(visit.getId())
                .patientId(visit.getPatient().getId())
                .doctorId(visit.getDoctor().getId())
                .start(LocalDateTime.ofInstant(visit.getStartDateTime(), doctorZone))
                .end(LocalDateTime.ofInstant(visit.getEndDateTime(), doctorZone))
                .build();
    }
}
