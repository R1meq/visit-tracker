package com.healthcare.visittracker.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PatientPageResult {
    private final List<PatientVisitDto> patients;
    private final long totalCount;
}
