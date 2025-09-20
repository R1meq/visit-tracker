package com.healthcare.visittracker.repository;

import java.util.List;

import com.healthcare.visittracker.dto.PatientPageResult;

public interface PatientRepositoryCustom {
    PatientPageResult findPatients(int page, int size, String search, List<Integer> doctorIds);
}
