package com.aiops.platform.alert.dto;

import java.time.Instant;

public record AlertResponse(
    Long id,
    Long incidentId,
    String channel,
    String message,
    boolean acknowledged,
    Instant sentAt
) {}
