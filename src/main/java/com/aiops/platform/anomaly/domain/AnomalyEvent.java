package com.aiops.platform.anomaly.domain;

import java.time.Instant;

public record AnomalyEvent(
    Long serviceId,
    String metricName,
    double anomalyScore,
    String detectionMethod,
    String severity,
    Instant detectedAt
) {}
