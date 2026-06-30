package com.aiops.platform.ingestion.domain;

import java.time.Instant;

public record LogEvent(Long serviceId, String logLevel, String message, Instant occurredAt) {}
