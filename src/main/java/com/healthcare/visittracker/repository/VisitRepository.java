package com.healthcare.visittracker.repository;

import com.healthcare.visittracker.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface VisitRepository extends JpaRepository<Visit, Integer> {

    @Query("""
            SELECT EXISTS (
              SELECT 1 FROM Visit v
              WHERE (v.doctor.id = :doctorId OR v.patient.id = :patientId)
                AND v.startDateTime < :end
                AND v.endDateTime > :start
            )""")
    boolean hasVisitConflict(Integer doctorId, Integer patientId, Instant start, Instant end);

}
