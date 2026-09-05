package com.codefortress.analysis.execution;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import com.codefortress.analysis.discovery.SourceFileDiscovery;
import com.codefortress.analysis.discovery.SourceFileDiscoveryException;
import com.codefortress.analysis.engine.AnalysisContext;
import com.codefortress.analysis.engine.RuleMatch;
import com.codefortress.analysis.engine.ScannableFile;
import com.codefortress.analysis.engine.SecurityRuleExecutor;
import com.codefortress.analysis.engine.SourceFileLoader;
import com.codefortress.analysis.engine.SourceFileLoadingException;
import com.codefortress.analysis.extraction.ExtractedSourceArchive;
import com.codefortress.analysis.extraction.SourceArchiveExtractionException;
import com.codefortress.analysis.extraction.SourceArchiveExtractor;
import com.codefortress.analysis.lifecycle.AnalysisLifecycleService;
import com.codefortress.analysis.lifecycle.AnalysisState;
import com.codefortress.analysis.upload.LocalSourceArchiveStorage;
import com.codefortress.analysis.upload.SourceArchiveStorageException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisExecutionService {

    private static final int INITIAL_SECURITY_SCORE = 100;

    private static final String RULE_SET_VERSION =
            "ruleset-java-v1";

    private final AnalysisLifecycleService lifecycleService;
    private final SourceArchiveExtractor archiveExtractor;
    private final SourceFileDiscovery fileDiscovery;
    private final SourceMetricsCalculator metricsCalculator;
    private final SourceFileLoader fileLoader;
    private final SecurityRuleExecutor ruleExecutor;
    private final LocalSourceArchiveStorage archiveStorage;

    public AnalysisExecutionService(
            AnalysisLifecycleService lifecycleService,
            SourceArchiveExtractor archiveExtractor,
            SourceFileDiscovery fileDiscovery,
            SourceMetricsCalculator metricsCalculator,
            SourceFileLoader fileLoader,
            SecurityRuleExecutor ruleExecutor,
            LocalSourceArchiveStorage archiveStorage
    ) {
        this.lifecycleService = lifecycleService;
        this.archiveExtractor = archiveExtractor;
        this.fileDiscovery = fileDiscovery;
        this.metricsCalculator = metricsCalculator;
        this.fileLoader = fileLoader;
        this.ruleExecutor = ruleExecutor;
        this.archiveStorage = archiveStorage;
    }

    public AnalysisState execute(UUID analysisId) {
        lifecycleService.start(analysisId);

        try {
            ExtractedSourceArchive extractedArchive =
                    archiveExtractor.extract(analysisId);

            List<DiscoveredSourceFile> sourceFiles =
                    fileDiscovery.discover(
                            extractedArchive.workspacePath()
                    );

            SourceMetrics metrics =
                    metricsCalculator.calculate(
                            sourceFiles
                    );

            List<ScannableFile> scannableFiles =
                    sourceFiles.stream()
                            .map(fileLoader::load)
                            .toList();

            AnalysisContext context =
                    new AnalysisContext(
                            analysisId,
                            RULE_SET_VERSION
                    );

            List<RuleMatch> matches =
                    ruleExecutor.execute(
                            scannableFiles,
                            context
                    );

            removeSourceArtifacts(analysisId);

            return lifecycleService.complete(
                    analysisId,
                    INITIAL_SECURITY_SCORE,
                    metrics.filesScanned(),
                    metrics.linesScanned(),
                    matches.size()
            );
        } catch (RuntimeException exception) {
            removeSourceArtifactsQuietly(analysisId);

            FailureDescription failure =
                    describeFailure(exception);

            return lifecycleService.fail(
                    analysisId,
                    failure.code(),
                    failure.message()
            );
        }
    }

    private void removeSourceArtifacts(UUID analysisId) {
        archiveExtractor.deleteWorkspace(analysisId);
        archiveStorage.delete(analysisId);
    }

    private void removeSourceArtifactsQuietly(UUID analysisId) {
        try {
            archiveExtractor.deleteWorkspace(analysisId);
        } catch (RuntimeException ignored) {
        }

        try {
            archiveStorage.delete(analysisId);
        } catch (RuntimeException ignored) {
        }
    }

    private FailureDescription describeFailure(
            RuntimeException exception
    ) {
        if (exception
                instanceof SourceArchiveExtractionException extractionException) {
            return new FailureDescription(
                    extractionException.getCode(),
                    extractionException.getMessage()
            );
        }

        if (exception
                instanceof SourceFileDiscoveryException discoveryException) {
            return new FailureDescription(
                    discoveryException.getCode(),
                    discoveryException.getMessage()
            );
        }

        if (exception
                instanceof SourceMetricsCalculationException metricsException) {
            return new FailureDescription(
                    metricsException.getCode(),
                    metricsException.getMessage()
            );
        }

        if (exception
                instanceof SourceFileLoadingException loadingException) {
            return new FailureDescription(
                    loadingException.getCode(),
                    loadingException.getMessage()
            );
        }

        if (exception
                instanceof SourceArchiveStorageException storageException) {
            return new FailureDescription(
                    storageException.getCode(),
                    storageException.getMessage()
            );
        }

        return new FailureDescription(
                "ANALYSIS_EXECUTION_FAILED",
                "The analysis could not be completed"
        );
    }

    private record FailureDescription(
            String code,
            String message
    ) {
    }
}