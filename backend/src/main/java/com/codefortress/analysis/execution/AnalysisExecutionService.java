package com.codefortress.analysis.execution;

import com.codefortress.analysis.discovery.DiscoveredSourceFile;
import com.codefortress.analysis.discovery.SourceFileDiscovery;
import com.codefortress.analysis.discovery.SourceFileDiscoveryException;
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

    private final AnalysisLifecycleService lifecycleService;
    private final SourceArchiveExtractor archiveExtractor;
    private final SourceFileDiscovery fileDiscovery;
    private final SourceMetricsCalculator metricsCalculator;
    private final LocalSourceArchiveStorage archiveStorage;

    public AnalysisExecutionService(
            AnalysisLifecycleService lifecycleService,
            SourceArchiveExtractor archiveExtractor,
            SourceFileDiscovery fileDiscovery,
            SourceMetricsCalculator metricsCalculator,
            LocalSourceArchiveStorage archiveStorage
    ) {
        this.lifecycleService = lifecycleService;
        this.archiveExtractor = archiveExtractor;
        this.fileDiscovery = fileDiscovery;
        this.metricsCalculator = metricsCalculator;
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

            removeSourceArtifacts(analysisId);

            return lifecycleService.complete(
                    analysisId,
                    INITIAL_SECURITY_SCORE,
                    metrics.filesScanned(),
                    metrics.linesScanned(),
                    0
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

    private void removeSourceArtifactsQuietly(
            UUID analysisId
    ) {
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
                instanceof SourceArchiveExtractionException
                extractionException) {
            return new FailureDescription(
                    extractionException.getCode(),
                    extractionException.getMessage()
            );
        }

        if (exception
                instanceof SourceFileDiscoveryException
                discoveryException) {
            return new FailureDescription(
                    discoveryException.getCode(),
                    discoveryException.getMessage()
            );
        }

        if (exception
                instanceof SourceMetricsCalculationException
                metricsException) {
            return new FailureDescription(
                    metricsException.getCode(),
                    metricsException.getMessage()
            );
        }

        if (exception
                instanceof SourceArchiveStorageException
                storageException) {
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