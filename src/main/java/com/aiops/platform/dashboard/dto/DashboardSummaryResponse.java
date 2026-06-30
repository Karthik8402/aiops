package com.aiops.platform.dashboard.dto;

import java.time.Instant;
import java.util.List;

public record DashboardSummaryResponse(
    long openIncidents,
    long criticalAnomaliesLast1h,
    long servicesMonitored,
    double kafkaConsumerLagSeconds,
    List<RecentAlertDto> recentAlerts
) {
    public record RecentAlertDto(
        Long id,
        Long incidentId,
        String message,
        Instant sentAt
    ) {}
}
