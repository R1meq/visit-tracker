package com.healthcare.visittracker.repository;

import com.healthcare.visittracker.dto.PatientVisitDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PatientRepositoryImpl implements PatientRepositoryCustom {
    private static final String CTE_PAGED_PATIENTS = """
                WITH paged_patients AS (
                    SELECT p.id, p.first_name, p.last_name
                    FROM patients p
                    %s
                    ORDER BY p.id
                    LIMIT :size
                    OFFSET :offset
                )
            """;

    private static final String CTE_VISITS_WITH_RANK = """
                , visits_with_rank AS (
                    SELECT v.id, v.patient_id, v.doctor_id, v.start_date_time, v.end_date_time,
                           ROW_NUMBER() OVER (
                               PARTITION BY v.patient_id, v.doctor_id
                               ORDER BY v.start_date_time DESC
                           ) AS rn
                    FROM visit v
                    JOIN paged_patients pp ON pp.id = v.patient_id
                    %s
                )
            """;

    private static final String CTE_LATEST_VISITS = """
                , latest_visits AS (
                    SELECT id, patient_id, doctor_id, start_date_time, end_date_time
                    FROM visits_with_rank
                    WHERE rn = 1
                )
            """;

    private static final String FINAL_SELECT = """
                SELECT pp.id               AS patient_id,
                       pp.first_name       AS patient_first_name,
                       pp.last_name        AS patient_last_name,
                       d.id                AS doctor_id,
                       d.first_name        AS doctor_first_name,
                       d.last_name         AS doctor_last_name,
                       d.timezone          AS doctor_timezone,
                       lv.start_date_time  AS latest_visit_start,
                       lv.end_date_time    AS latest_visit_end
                FROM paged_patients pp
                LEFT JOIN latest_visits lv ON lv.patient_id = pp.id
                LEFT JOIN doctor d ON d.id = lv.doctor_id
                ORDER BY pp.id, d.id
            """;

    private static final String DOCTOR_PATIENT_COUNTS_QUERY = """
                SELECT v.doctor_id, COUNT(DISTINCT v.patient_id) AS total_patients
                FROM visit v
                WHERE v.doctor_id IN (:doctorIds)
                GROUP BY v.doctor_id
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<PatientVisitDto> findPatients(int page, int size, String search, List<Integer> doctorsId) {
        String trimmedSearch = (search != null) ? search.trim() : null;
        boolean hasSearch = trimmedSearch != null && !trimmedSearch.isEmpty();
        boolean hasDoctorIds = doctorsId != null && !doctorsId.isEmpty();

        String patientWhere = hasSearch ? "WHERE LOWER(p.first_name) LIKE LOWER(:search)" : "";
        String visitWhere = hasDoctorIds ? "WHERE v.doctor_id IN (:doctorIds)" : "";

        String sql = buildSqlQuery(patientWhere, visitWhere);
        Query query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("size", size)
                .setParameter("offset", page * size);

        if (hasSearch) {
            query.setParameter("search", "%" + trimmedSearch + "%");
        }
        if (hasDoctorIds) {
            query.setParameter("doctorIds", doctorsId);
        }

        List<Tuple> results = query.getResultList();
        return results.stream().map(PatientVisitDto::fromTuple).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> getDoctorPatientCounts(Set<Integer> doctorIds) {
        if (doctorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> results = entityManager.createNativeQuery(DOCTOR_PATIENT_COUNTS_QUERY, Tuple.class)
                .setParameter("doctorIds", doctorIds)
                .getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> (Integer) tuple.get("doctor_id"),
                        tuple -> (Long) tuple.get("total_patients")
                ));
    }

    @Override
    public long countPatients(String search) {
        String trimmedSearch = (search != null) ? search.trim() : null;
        boolean hasSearch = trimmedSearch != null && !trimmedSearch.isEmpty();

        String whereClause = hasSearch ? "WHERE LOWER(p.first_name) LIKE LOWER(:search)" : "";
        String sql = "SELECT COUNT(*) FROM patients p " + whereClause;

        Query query = entityManager.createNativeQuery(sql);
        if (hasSearch) {
            query.setParameter("search", "%" + trimmedSearch + "%");
        }

        return ((Number) query.getSingleResult()).longValue();
    }

    private String buildSqlQuery(String patientWhere, String visitWhere) {
        return String.format(CTE_PAGED_PATIENTS, patientWhere) +
                String.format(CTE_VISITS_WITH_RANK, visitWhere) +
                CTE_LATEST_VISITS +
                FINAL_SELECT;
    }
}
