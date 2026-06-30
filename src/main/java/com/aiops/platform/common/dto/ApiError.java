package com.aiops.platform.common.dto;

import java.time.Instant;

public record ApiError(
    int status,
    String error,
    String message,
    Instant timestamp
) {
    public ApiError(int status, String error, String message) {
        this(status, error, message, Instant.now());
    }
}
