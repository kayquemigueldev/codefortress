package com.codefortress.analysis.upload;

import com.codefortress.analysis.queue.QueueAnalysisCommand;
import com.codefortress.analysis.queue.QueueAnalysisService;
import com.codefortress.analysis.queue.QueuedAnalysis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UploadAnalysisService {

    private final SourceArchiveInspector archiveInspector;
    private final LocalSourceArchiveStorage archiveStorage;
    private final QueueAnalysisService queueAnalysisService;

    public UploadAnalysisService(
            SourceArchiveInspector archiveInspector,
            LocalSourceArchiveStorage archiveStorage,
            QueueAnalysisService queueAnalysisService
    ) {
        this.archiveInspector = archiveInspector;
        this.archiveStorage = archiveStorage;
        this.queueAnalysisService = queueAnalysisService;
    }

    @Transactional
    public UploadedAnalysis upload(
            UUID ownerId,
            UUID projectId,
            MultipartFile file
    ) {
        InspectedSourceArchive inspectedArchive =
                archiveInspector.inspect(file);

        QueuedAnalysis queuedAnalysis =
                queueAnalysisService.queue(
                        ownerId,
                        projectId,
                        new QueueAnalysisCommand(
                                inspectedArchive.sourceReference(),
                                inspectedArchive.sourceFilename()
                        )
                );

        StoredSourceArchive storedArchive =
                archiveStorage.store(
                        queuedAnalysis.id(),
                        file
                );

        registerRollbackCleanup(queuedAnalysis.id());

        if (storedArchive.sizeBytes()
                != inspectedArchive.sizeBytes()) {
            throw new SourceArchiveStorageException(
                    "SOURCE_ARCHIVE_SIZE_MISMATCH",
                    "The stored source archive size is invalid"
            );
        }

        return UploadedAnalysis.from(
                queuedAnalysis,
                inspectedArchive
        );
    }

    private void registerRollbackCleanup(UUID analysisId) {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(int status) {
                                if (status
                                        == STATUS_COMMITTED) {
                                    return;
                                }

                                try {
                                    archiveStorage.delete(analysisId);
                                } catch (
                                        SourceArchiveStorageException ignored
                                ) {
                                }
                            }
                        }
                );
    }
}