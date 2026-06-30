package com.aiops.platform.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MetricIngestRequest(
    @NotNull(message = "serviceId is required")
    Long serviceId,
    
    @NotBlank(message = "metricName is required")
    String metricName,
    
    @NotNull(message = "metricValue is required")
    Double metricValue,
    
    @NotNull(message = "recordedAt is required")
    Instant recordedAt
) {}
