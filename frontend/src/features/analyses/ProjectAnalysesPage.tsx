import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import {
    useRef,
    useState,
} from 'react'
import {
    Link,
    useParams,
} from 'react-router'

import { ApiError } from '../../lib/api/api-error'

import {
    listAnalyses,
    uploadAnalysis,
} from './analysis-api'
import type {
    Analysis,
    AnalysisStatus,
} from './analysis-types'

const MAX_UPLOAD_SIZE =
    10 * 1024 * 1024

export function ProjectAnalysesPage() {
    const { projectId } =
        useParams<{
            projectId: string
        }>()

    const queryClient =
        useQueryClient()

    const fileInputRef =
        useRef<HTMLInputElement>(null)

    const [
        selectedFile,
        setSelectedFile,
    ] =
        useState<File | null>(null)

    const [
        clientError,
        setClientError,
    ] =
        useState<string | null>(null)

    const analysesQuery = useQuery({
        queryKey: [
            'projects',
            projectId,
            'analyses',
        ],

        queryFn: () =>
            listAnalyses(
                projectId ?? '',
            ),

        enabled:
            Boolean(projectId),

        refetchInterval:
            (query) => {
                const analyses =
                    query.state.data

                if (!analyses) {
                    return false
                }

                const processing =
                    analyses.some(
                        (analysis) =>
                            analysis.status
                            === 'QUEUED'
                            || analysis.status
                            === 'RUNNING',
                    )

                return processing
                    ? 1_500
                    : false
            },
    })

    const uploadMutation =
        useMutation({
            mutationFn:
                (file: File) =>
                    uploadAnalysis(
                        projectId ?? '',
                        file,
                    ),

            onSuccess: async () => {
                setSelectedFile(null)

                if (fileInputRef.current) {
                    fileInputRef.current.value =
                        ''
                }

                await queryClient.invalidateQueries({
                    queryKey: [
                        'projects',
                        projectId,
                        'analyses',
                    ],
                })
            },
        })

    if (!projectId) {
        return (
            <main className="analyses-page">
                <div className="analyses-container">
                    <h1>
                        Invalid project.
                    </h1>
                </div>
            </main>
        )
    }

    function selectFile(
        file: File | undefined,
    ) {
        setClientError(null)
        uploadMutation.reset()

        if (!file) {
            setSelectedFile(null)
            return
        }

        if (
            !file.name
                .toLowerCase()
                .endsWith('.zip')
        ) {
            setSelectedFile(null)
            setClientError(
                'Only ZIP source archives are supported.',
            )

            return
        }

        if (file.size === 0) {
            setSelectedFile(null)
            setClientError(
                'A non-empty source archive is required.',
            )

            return
        }

        if (
            file.size
            > MAX_UPLOAD_SIZE
        ) {
            setSelectedFile(null)
            setClientError(
                'The source archive exceeds the 10 MB limit.',
            )

            return
        }

        setSelectedFile(file)
    }

    async function submitAnalysis() {
        if (!selectedFile) {
            setClientError(
                'Select a ZIP archive first.',
            )

            return
        }

        setClientError(null)

        try {
            await uploadMutation.mutateAsync(
                selectedFile,
            )
        } catch {
            // Rendered from mutation error below.
        }
    }

    const serverError =
        uploadMutation.error
        instanceof ApiError
            ? uploadMutation.error.message
            : uploadMutation.isError
                ? 'Unable to upload source archive.'
                : null

    return (
        <main className="analyses-page">
            <div className="analyses-container">
                <Link
                    className="project-back-link"
                    to={
                        `/app/projects/${projectId}`
                    }
                >
                    ← Back to project
                </Link>

                <header className="analyses-header">
                    <div>
                        <p className="projects-eyebrow">
                            Security analysis
                        </p>

                        <h1>
                            Analyses
                        </h1>

                        <p>
                            Upload a source archive and
                            review its security score,
                            scanning metrics and findings.
                        </p>
                    </div>
                </header>

                <section className="analysis-upload-card">
                    <div>
                        <p className="analysis-section-label">
                            Run analysis
                        </p>

                        <h2>
                            Upload source code
                        </h2>

                        <p>
                            Upload a ZIP archive up to
                            10 MB. CodeFortress will
                            inspect and analyze it
                            asynchronously.
                        </p>
                    </div>

                    {(clientError
                        || serverError) && (
                        <div
                            className="project-form-error"
                            role="alert"
                        >
                            {clientError
                                || serverError}
                        </div>
                    )}

                    <div className="analysis-upload-controls">
                        <label
                            className="analysis-file-picker"
                            htmlFor="analysis-file"
                        >
              <span>
                {selectedFile
                    ? selectedFile.name
                    : 'Choose ZIP archive'}
              </span>

                            <small>
                                {selectedFile
                                    ? formatBytes(
                                        selectedFile.size,
                                    )
                                    : 'ZIP · max 10 MB'}
                            </small>
                        </label>

                        <input
                            ref={fileInputRef}
                            id="analysis-file"
                            className="analysis-file-input"
                            type="file"
                            accept=".zip,application/zip"
                            onChange={(event) => {
                                selectFile(
                                    event.target
                                        .files?.[0],
                                )
                            }}
                        />

                        <button
                            className="projects-primary-button"
                            type="button"
                            disabled={
                                !selectedFile
                                || uploadMutation.isPending
                            }
                            onClick={() => {
                                void submitAnalysis()
                            }}
                        >
                            {uploadMutation.isPending
                                ? 'Uploading...'
                                : 'Run analysis'}
                        </button>
                    </div>
                </section>

                <section className="analysis-history">
                    <div className="analysis-history__heading">
                        <div>
                            <p className="analysis-section-label">
                                History
                            </p>

                            <h2>
                                Previous analyses
                            </h2>
                        </div>

                        {analysesQuery.isFetching
                            && !analysesQuery.isPending
                            && (
                                <span className="analysis-refreshing">
                  Updating…
                </span>
                            )}
                    </div>

                    {analysesQuery.isPending && (
                        <div className="analysis-state">
                            Loading analyses...
                        </div>
                    )}

                    {analysesQuery.isError && (
                        <div className="analysis-state">
                            <p>
                                Unable to load analyses.
                            </p>

                            <button
                                className="project-secondary-button"
                                type="button"
                                onClick={() => {
                                    void analysesQuery.refetch()
                                }}
                            >
                                Try again
                            </button>
                        </div>
                    )}

                    {analysesQuery.data
                        && analysesQuery.data.length
                        === 0 && (
                            <div className="analysis-state">
                                <h3>
                                    No analyses yet.
                                </h3>

                                <p>
                                    Upload your first ZIP
                                    archive to start a
                                    security analysis.
                                </p>
                            </div>
                        )}

                    {analysesQuery.data
                        && analysesQuery.data.length
                        > 0 && (
                            <div className="analysis-list">
                                {analysesQuery.data.map(
                                    (analysis) => (
                                        <AnalysisCard
                                            key={analysis.id}
                                            projectId={
                                                projectId
                                            }
                                            analysis={
                                                analysis
                                            }
                                        />
                                    ),
                                )}
                            </div>
                        )}
                </section>
            </div>
        </main>
    )
}

function AnalysisCard({
                          projectId,
                          analysis,
                      }: {
    projectId: string
    analysis: Analysis
}) {
    const completed =
        analysis.status === 'COMPLETED'

    return (
        <article className="analysis-card">
            <div className="analysis-card__header">
                <div>
                    <p className="analysis-card__sequence">
                        Analysis #
                        {analysis.sequenceNumber}
                    </p>

                    <h3>
                        {analysis.sourceFilename}
                    </h3>
                </div>

                <AnalysisStatusBadge
                    status={analysis.status}
                />
            </div>

            <div className="analysis-card__metrics">
                <Metric
                    label="Security score"
                    value={
                        completed
                        && analysis.securityScore
                        !== null
                            ? String(
                                analysis.securityScore,
                            )
                            : '—'
                    }
                    emphasized={completed}
                />

                <Metric
                    label="Findings"
                    value={
                        analysis.findingsCount
                        !== null
                            ? String(
                                analysis.findingsCount,
                            )
                            : '—'
                    }
                />

                <Metric
                    label="Files"
                    value={
                        analysis.filesScanned
                        !== null
                            ? String(
                                analysis.filesScanned,
                            )
                            : '—'
                    }
                />

                <Metric
                    label="Lines"
                    value={
                        analysis.linesScanned
                        !== null
                            ? new Intl.NumberFormat(
                                'en',
                            ).format(
                                analysis.linesScanned,
                            )
                            : '—'
                    }
                />
            </div>

            <footer className="analysis-card__footer">
                <div>
          <span>
            Created
          </span>

                    <time
                        dateTime={
                            analysis.createdAt
                        }
                    >
                        {formatDate(
                            analysis.createdAt,
                        )}
                    </time>
                </div>

                {completed && (
                    <Link
                        className="analysis-findings-link"
                        to={
                            `/app/projects/${projectId}/analyses/${analysis.id}/findings`
                        }
                    >
                        View findings →
                    </Link>
                )}
            </footer>
        </article>
    )
}

function AnalysisStatusBadge({
                                 status,
                             }: {
    status: AnalysisStatus
}) {
    return (
        <span
            className={
                `analysis-status analysis-status--${status.toLowerCase()}`
            }
        >
      {status}
    </span>
    )
}

function Metric({
                    label,
                    value,
                    emphasized = false,
                }: {
    label: string
    value: string
    emphasized?: boolean
}) {
    return (
        <div className="analysis-metric">
      <span>
        {label}
      </span>

            <strong
                className={
                    emphasized
                        ? 'analysis-metric__score'
                        : undefined
                }
            >
                {value}
            </strong>
        </div>
    )
}

function formatDate(
    value: string,
) {
    return new Intl.DateTimeFormat(
        'en',
        {
            dateStyle: 'medium',
            timeStyle: 'short',
        },
    ).format(
        new Date(value),
    )
}

function formatBytes(
    bytes: number,
) {
    if (bytes < 1024) {
        return `${bytes} B`
    }

    const kilobytes =
        bytes / 1024

    if (kilobytes < 1024) {
        return `${kilobytes.toFixed(1)} KB`
    }

    return `${
        (
            kilobytes / 1024
        ).toFixed(1)
    } MB`
}