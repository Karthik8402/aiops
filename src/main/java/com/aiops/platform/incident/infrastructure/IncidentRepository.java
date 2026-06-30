package com.aiops.platform.incident.infrastructure;

import com.aiops.platform.incident.domain.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("SELECT i FROM Incident i WHERE i.serviceId = :serviceId AND i.status = 'OPEN' AND i.openedAt >= :windowStart ORDER BY i.openedAt DESC")
    List<Incident> findOpenByServiceWithinWindowRaw(
        @Param("serviceId") Long serviceId,
        @Param("windowStart") Instant windowStart
    );

    default Optional<Incident> findOpenByServiceWithinWindow(Long serviceId, Duration window) {
        Instant windowStart = Instant.now().minus(window);
        List<Incident> incidents = findOpenByServiceWithinWindowRaw(serviceId, windowStart);
        return incidents.isEmpty() ? Optional.empty() : Optional.of(incidents.get(0));
    }

    @Query("SELECT i FROM Incident i WHERE (:status IS NULL OR i.status = :status)")
    Page<Incident> findByStatus(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);
}
