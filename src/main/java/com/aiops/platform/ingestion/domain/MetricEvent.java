package com.aiops.platform.ingestion.domain;

import java.time.Instant;

public record MetricEvent(Long serviceId, String metricName, double metricValue, Instant recordedAt) {}
