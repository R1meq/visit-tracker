package com.healthcare.visittracker.dto;

import com.healthcare.visittracker.validation.ValidVisitTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidVisitTime
public class VisitRequest {
    @NotNull(message = "Start time is required")
    private LocalDateTime start;

    @NotNull(message = "End time is required")
    private LocalDateTime end;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    private Integer patientId;

    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Integer doctorId;
}

