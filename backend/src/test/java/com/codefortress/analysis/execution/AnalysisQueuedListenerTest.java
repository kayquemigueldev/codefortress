package com.codefortress.analysis.execution;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AnalysisQueuedListenerTest {

    @Test
    void shouldDelegateQueuedAnalysisToExecutor() {
        AnalysisExecutionService executionService =
                mock(AnalysisExecutionService.class);

        AnalysisQueuedListener listener =
                new AnalysisQueuedListener(
                        executionService
                );

        UUID analysisId = UUID.randomUUID();

        listener.handle(
                new AnalysisQueuedEvent(analysisId)
        );

        verify(executionService).execute(analysisId);
    }
}