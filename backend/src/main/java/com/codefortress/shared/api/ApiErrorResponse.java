package com.codefortress.shared.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        String code,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
}