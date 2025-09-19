package com.healthcare.visittracker.repository;

import com.healthcare.visittracker.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface VisitRepository extends JpaRepository<Visit, Integer> {

    @Query("""
                SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
                FROM Visit v
                WHERE v.doctor.id = :doctorId
                  AND v.startDateTime < :end
                  AND v.endDateTime > :start
            """)
    boolean hasOverlappingVisit(Integer doctorId, Instant start, Instant end);

}
