package com.codefortress.analysis.execution;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AnalysisQueuedListener {

    private final AnalysisExecutionService executionService;

    public AnalysisQueuedListener(
            AnalysisExecutionService executionService
    ) {
        this.executionService = executionService;
    }

    @Async("analysisTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(AnalysisQueuedEvent event) {
        executionService.execute(event.analysisId());
    }
}