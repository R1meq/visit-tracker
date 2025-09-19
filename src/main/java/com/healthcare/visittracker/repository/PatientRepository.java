package com.healthcare.visittracker.repository;

import com.healthcare.visittracker.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Integer>, PatientRepositoryCustom {
}
