package com.codefortress.analysis.execution;

import java.util.UUID;

public record AnalysisQueuedEvent(
        UUID analysisId
) {
}