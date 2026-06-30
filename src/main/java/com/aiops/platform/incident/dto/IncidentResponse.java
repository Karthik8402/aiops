package com.aiops.platform.incident.dto;

import java.time.Instant;
import java.util.List;

public record IncidentResponse(
    Long id,
    Long serviceId,
    String title,
    String status,
    String severity,
    String rootCauseSummary,
    Instant openedAt,
    Instant resolvedAt,
    List<Long> anomalies
) {}
