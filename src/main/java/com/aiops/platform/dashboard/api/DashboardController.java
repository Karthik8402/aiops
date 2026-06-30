package com.aiops.platform.dashboard.api;

import com.aiops.platform.alert.infrastructure.AlertRepository;
import com.aiops.platform.anomaly.infrastructure.AnomalyRepository;
import com.aiops.platform.dashboard.dto.DashboardSummaryResponse;
import com.aiops.platform.incident.infrastructure.IncidentRepository;
import com.aiops.platform.ingestion.infrastructure.ServiceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IncidentRepository incidentRepository;
    private final AnomalyRepository anomalyRepository;
    private final ServiceRepository serviceRepository;
    private final AlertRepository alertRepository;
    private final MeterRegistry meterRegistry;

    public DashboardController(
        IncidentRepository incidentRepository,
        AnomalyRepository anomalyRepository,
        ServiceRepository serviceRepository,
        AlertRepository alertRepository,
        MeterRegistry meterRegistry
    ) {
        this.incidentRepository = incidentRepository;
        this.anomalyRepository = anomalyRepository;
        this.serviceRepository = serviceRepository;
        this.alertRepository = alertRepository;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        long openIncidents = incidentRepository.countByStatus("OPEN");
        
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long criticalAnomalies = anomalyRepository.countBySeverityAndDetectedAtAfter("CRITICAL", oneHourAgo);
        
        long servicesCount = serviceRepository.count();
        
        // Fetch 5 most recent unacknowledged alerts
        var alertPage = alertRepository.findByAcknowledged(
            false,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "sentAt"))
        );

        List<DashboardSummaryResponse.RecentAlertDto> recentAlerts = alertPage.getContent().stream()
            .map(a -> new DashboardSummaryResponse.RecentAlertDto(
                a.getId(),
                a.getIncidentId(),
                a.getMessage(),
                a.getSentAt()
            ))
            .toList();

        // Calculate dynamic max Kafka consumer lag across all active groups
        double lag = 0.0;
        try {
            var meters = meterRegistry.getMeters().stream()
                .filter(m -> m.getId().getName().contains("records-lag") || m.getId().getName().contains("records.lag"))
                .toList();
            if (!meters.isEmpty()) {
                lag = meters.stream()
                    .mapToDouble(m -> {
                        var measurement = m.measure().iterator();
                        return measurement.hasNext() ? measurement.next().getValue() : 0.0;
                    })
                    .filter(Double::isFinite)
                    .max()
                    .orElse(0.0);
            }
        } catch (Exception e) {
            // fallback if registry fails or metrics are not yet registered
        }

        return ResponseEntity.ok(new DashboardSummaryResponse(
            openIncidents,
            criticalAnomalies,
            servicesCount,
            lag,
            recentAlerts
        ));
    }
}
