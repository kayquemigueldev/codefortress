package com.codefortress.analysis.api;

import com.codefortress.analysis.FindingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFindingStatusRequest(
        @NotNull(message = "status is required")
        FindingStatus status
) {
}