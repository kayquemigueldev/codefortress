package com.codefortress.shared.api;

import java.time.Instant;

public record SystemStatusResponse(
        String application,
        String status,
        String stage,
        Instant timestamp
) {
}