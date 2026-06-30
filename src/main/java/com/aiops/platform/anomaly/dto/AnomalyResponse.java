package com.aiops.platform.anomaly.dto;

import java.time.Instant;

public record AnomalyResponse(
    Long id,
    Long serviceId,
    String metricName,
    double anomalyScore,
    String detectionMethod,
    String severity,
    Instant detectedAt
) {}
