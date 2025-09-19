package com.healthcare.visittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDto {
    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private LocalDateTime start;
    private LocalDateTime end;
}
