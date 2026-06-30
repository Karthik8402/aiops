package com.aiops.platform.incident.domain;

import java.time.Instant;

public record IncidentEvent(
    Long incidentId,
    Long serviceId,
    String title,
    String severity,
    Instant openedAt
) {}
