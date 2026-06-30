package com.aiops.platform.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record LogIngestRequest(
    @NotNull(message = "serviceId is required")
    Long serviceId,
    
    @NotBlank(message = "logLevel is required")
    String logLevel,
    
    @NotBlank(message = "message is required")
    String message,
    
    @NotNull(message = "occurredAt is required")
    Instant occurredAt
) {}
