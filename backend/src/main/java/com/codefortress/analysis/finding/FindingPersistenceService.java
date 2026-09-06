package com.codefortress.analysis.finding;

import com.codefortress.analysis.Analysis;
import com.codefortress.analysis.AnalysisRepository;
import com.codefortress.analysis.Finding;
import com.codefortress.analysis.FindingRepository;
import com.codefortress.analysis.engine.EvaluatedRuleMatch;
import com.codefortress.analysis.engine.FindingFactory;
import com.codefortress.analysis.lifecycle.AnalysisNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FindingPersistenceService {

    private final AnalysisRepository analysisRepository;
    private final FindingRepository findingRepository;
    private final FindingFactory findingFactory;

    public FindingPersistenceService(
            AnalysisRepository analysisRepository,
            FindingRepository findingRepository,
            FindingFactory findingFactory
    ) {
        this.analysisRepository =
                Objects.requireNonNull(
                        analysisRepository,
                        "analysisRepository must not be null"
                );

        this.findingRepository =
                Objects.requireNonNull(
                        findingRepository,
                        "findingRepository must not be null"
                );

        this.findingFactory =
                Objects.requireNonNull(
                        findingFactory,
                        "findingFactory must not be null"
                );
    }

    @Transactional
    public int persist(
            UUID analysisId,
            List<EvaluatedRuleMatch> matches
    ) {
        Objects.requireNonNull(
                analysisId,
                "analysisId must not be null"
        );

        Objects.requireNonNull(
                matches,
                "matches must not be null"
        );

        Analysis analysis =
                analysisRepository
                        .findById(analysisId)
                        .orElseThrow(
                                AnalysisNotFoundException::new
                        );

        List<Finding> findings =
                matches.stream()
                        .map(match ->
                                findingFactory.create(
                                        analysis,
                                        match
                                )
                        )
                        .toList();

        if (findings.isEmpty()) {
            return 0;
        }

        List<Finding> savedFindings =
                findingRepository
                        .saveAllAndFlush(
                                findings
                        );

        return savedFindings.size();
    }
}