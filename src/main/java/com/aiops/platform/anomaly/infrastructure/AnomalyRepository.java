package com.aiops.platform.anomaly.infrastructure;

import com.aiops.platform.anomaly.domain.Anomaly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

    @Query("SELECT a FROM Anomaly a WHERE " +
           "(:serviceId IS NULL OR a.serviceId = :serviceId) AND " +
           "(:severity IS NULL OR a.severity = :severity)")
    Page<Anomaly> findByFilters(
        @Param("serviceId") Long serviceId,
        @Param("severity") String severity,
        Pageable pageable
    );

    long countBySeverityAndDetectedAtAfter(String severity, java.time.Instant timestamp);
}
