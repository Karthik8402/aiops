package com.aiops.platform.alert.domain;

import java.time.Instant;

public record AlertEvent(
    Long alertId,
    Long incidentId,
    String message,
    Instant sentAt
) {}
