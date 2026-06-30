package com.aiops.platform.incident.dto;

import jakarta.validation.constraints.NotBlank;

public record IncidentStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
