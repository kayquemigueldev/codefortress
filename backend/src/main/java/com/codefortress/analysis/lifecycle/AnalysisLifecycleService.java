package com.codefortress.analysis.lifecycle;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AnalysisLifecycleService {

    private final AnalysisRepository analysisRepository;

    public AnalysisLifecycleService(
            AnalysisRepository analysisRepository
    ) {
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public AnalysisState start(UUID analysisId) {
        Analysis analysis = findForUpdate(analysisId);

        analysis.start();

        return save(analysis);
    }

    @Transactional
    public AnalysisState complete(
            UUID analysisId,
            int securityScore,
            int filesScanned,
            long linesScanned,
            int findingsCount
    ) {
        Analysis analysis = findForUpdate(analysisId);

        analysis.complete(
                securityScore,
                filesScanned,
                linesScanned,
                findingsCount
        );

        return save(analysis);
    }

    @Transactional
    public AnalysisState fail(
            UUID analysisId,
            String failureCode,
            String failureMessage
    ) {
        Analysis analysis = findForUpdate(analysisId);

        analysis.fail(
                failureCode,
                failureMessage
        );

        return save(analysis);
    }

    @Transactional
    public AnalysisState cancel(UUID analysisId) {
        Analysis analysis = findForUpdate(analysisId);

        analysis.cancel();

        return save(analysis);
    }

    private Analysis findForUpdate(UUID analysisId) {
        return analysisRepository
                .findByIdForUpdate(analysisId)
                .orElseThrow(
                        AnalysisNotFoundException::new
                );
    }

    private AnalysisState save(Analysis analysis) {
        Analysis savedAnalysis =
                analysisRepository.saveAndFlush(analysis);

        return AnalysisState.from(savedAnalysis);
    }
}