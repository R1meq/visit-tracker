package com.healthcare.visittracker.repository;

import com.healthcare.visittracker.dto.PatientPageResult;
import com.healthcare.visittracker.dto.PatientVisitDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientRepositoryImpl implements PatientRepositoryCustom {
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SEARCH = "search";
    private static final String PARAM_DOCTOR_IDS = "doctorIds";
    private static final String PATIENT_WHERE_PLACEHOLDER = "{{PATIENT_WHERE}}";
    private static final String VISIT_WHERE_PLACEHOLDER   = "{{VISIT_WHERE}}";

    private static final String CTE_TOTAL_COUNT = """
                WITH total_patients_count AS (
                    SELECT COUNT(*) as total_patients
                    FROM patients p
                    {{PATIENT_WHERE}}
                )
            """;

    private static final String CTE_PAGED_PATIENTS = """
                , paged_patients AS (
                    SELECT p.id, p.first_name, p.last_name
                    FROM patients p
                    {{PATIENT_WHERE}}
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
                    {{VISIT_WHERE}}
                )
            """;

    private static final String CTE_LATEST_VISITS = """
                , latest_visits AS (
                    SELECT id, patient_id, doctor_id, start_date_time, end_date_time
                    FROM visits_with_rank
                    WHERE rn = 1
                )
            """;

    private static final String CTE_DOCTOR_PATIENT_COUNTS = """
                , doctor_patient_counts AS (
                    SELECT
                        v.doctor_id,
                        COUNT(DISTINCT v.patient_id) AS total_patients
                    FROM visit v
                    WHERE v.doctor_id IN (SELECT DISTINCT doctor_id FROM latest_visits WHERE doctor_id IS NOT NULL)
                    GROUP BY v.doctor_id
                )
            """;

    private static final String FINAL_SELECT = """
                SELECT tpc.total_patients  AS total_count,
                       pp.id               AS patient_id,
                       pp.first_name       AS patient_first_name,
                       pp.last_name        AS patient_last_name,
                       d.first_name        AS doctor_first_name,
                       d.last_name         AS doctor_last_name,
                       d.timezone          AS doctor_timezone,
                       lv.start_date_time  AS latest_visit_start,
                       lv.end_date_time    AS latest_visit_end,
                       dpc.total_patients  AS doctor_total_patients
                FROM paged_patients pp
                CROSS JOIN total_patients_count tpc
                LEFT JOIN latest_visits lv ON lv.patient_id = pp.id
                LEFT JOIN doctor d ON d.id = lv.doctor_id
                LEFT JOIN doctor_patient_counts dpc ON dpc.doctor_id = d.id
                ORDER BY pp.id, d.id
            """;

    private static final String MAIN_QUERY = buildMainQuery();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public PatientPageResult findPatients(int page, int size, String search, List<Integer> doctorIds) {
        String trimmedSearch = (search != null) ? search.trim() : null;
        boolean hasSearch = trimmedSearch != null && !trimmedSearch.isEmpty();
        boolean hasDoctorIds = doctorIds != null && !doctorIds.isEmpty();
        String patientWhere = hasSearch ? "WHERE LOWER(p.first_name) LIKE LOWER(:search)" : "";
        String visitWhere = hasDoctorIds ? "WHERE v.doctor_id IN (:doctorIds)" : "";

        String sql = buildCompleteQuery(patientWhere, visitWhere);
        Query query = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter(PARAM_SIZE, size)
                .setParameter(PARAM_OFFSET, page * size);

        if (hasSearch) {
            query.setParameter(PARAM_SEARCH, "%" + trimmedSearch + "%");
        }
        if (hasDoctorIds) {
            query.setParameter(PARAM_DOCTOR_IDS, doctorIds);
        }

        List<Tuple> results = query.getResultList();

        long totalCount = results.isEmpty() ? 0 : ((Number) results.get(0).get("total_count")).longValue();
        List<PatientVisitDto> patients = results.stream().map(PatientVisitDto::fromTuple).toList();
        return new PatientPageResult(patients, totalCount);
    }

    private String buildCompleteQuery(String patientWhere, String visitWhere) {
        return MAIN_QUERY
                .replace(PATIENT_WHERE_PLACEHOLDER, patientWhere)
                .replace(VISIT_WHERE_PLACEHOLDER, visitWhere);
    }

    private static String buildMainQuery() {
        return String.join("",
                CTE_TOTAL_COUNT,
                CTE_PAGED_PATIENTS,
                CTE_VISITS_WITH_RANK,
                CTE_LATEST_VISITS,
                CTE_DOCTOR_PATIENT_COUNTS,
                FINAL_SELECT
        );
    }
}
