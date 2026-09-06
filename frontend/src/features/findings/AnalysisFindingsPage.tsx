import {
    useQuery,
} from '@tanstack/react-query'
import {
    Link,
    useParams,
} from 'react-router'

import { ApiError } from '../../lib/api/api-error'

import {
    listFindings,
} from './finding-api'
import type {
    Finding,
    FindingSeverity,
} from './finding-types'

export function AnalysisFindingsPage() {
    const {
        projectId,
        analysisId,
    } =
        useParams<{
            projectId: string
            analysisId: string
        }>()

    const findingsQuery = useQuery({
        queryKey: [
            'projects',
            projectId,
            'analyses',
            analysisId,
            'findings',
        ],

        queryFn: () =>
            listFindings(
                projectId ?? '',
                analysisId ?? '',
            ),

        enabled:
            Boolean(
                projectId
                && analysisId,
            ),
    })

    if (
        !projectId
        || !analysisId
    ) {
        return (
            <main className="findings-page">
                <div className="findings-container">
                    <h1>
                        Invalid analysis.
                    </h1>
                </div>
            </main>
        )
    }

    if (findingsQuery.isPending) {
        return (
            <main className="findings-page">
                <div className="findings-container">
                    <p className="projects-eyebrow">
                        Security findings
                    </p>

                    <h1>
                        Loading findings...
                    </h1>
                </div>
            </main>
        )
    }

    if (findingsQuery.isError) {
        const notFound =
            findingsQuery.error
            instanceof ApiError
            && findingsQuery.error.status
            === 404

        return (
            <main className="findings-page">
                <div className="findings-container">
                    <p className="projects-eyebrow">
                        Security findings
                    </p>

                    <h1>
                        {notFound
                            ? 'Analysis not found.'
                            : 'Unable to load findings.'}
                    </h1>

                    <p className="findings-description">
                        {notFound
                            ? 'This analysis does not exist or is not available for this project.'
                            : 'Something went wrong while loading the analysis findings.'}
                    </p>

                    {!notFound && (
                        <button
                            className="projects-primary-button"
                            type="button"
                            onClick={() => {
                                void findingsQuery.refetch()
                            }}
                        >
                            Try again
                        </button>
                    )}
                </div>
            </main>
        )
    }

    const findings =
        findingsQuery.data

    const criticalCount =
        countSeverity(
            findings,
            'CRITICAL',
        )

    const highCount =
        countSeverity(
            findings,
            'HIGH',
        )

    const mediumCount =
        countSeverity(
            findings,
            'MEDIUM',
        )

    const lowCount =
        countSeverity(
            findings,
            'LOW',
        )

    return (
        <main className="findings-page">
            <div className="findings-container">
                <Link
                    className="project-back-link"
                    to={
                        `/app/projects/${projectId}/analyses`
                    }
                >
                    ← Back to analyses
                </Link>

                <header className="findings-header">
                    <p className="projects-eyebrow">
                        Security findings
                    </p>

                    <h1>
                        Findings
                    </h1>

                    <p className="findings-description">
                        Review detected security
                        issues, affected source
                        locations and recommended
                        remediation.
                    </p>
                </header>

                <section className="findings-summary">
                    <SummaryMetric
                        label="Total"
                        value={findings.length}
                    />

                    <SummaryMetric
                        label="Critical"
                        value={criticalCount}
                        severity="CRITICAL"
                    />

                    <SummaryMetric
                        label="High"
                        value={highCount}
                        severity="HIGH"
                    />

                    <SummaryMetric
                        label="Medium"
                        value={mediumCount}
                        severity="MEDIUM"
                    />

                    <SummaryMetric
                        label="Low"
                        value={lowCount}
                        severity="LOW"
                    />
                </section>

                {findings.length === 0 ? (
                    <section className="findings-empty">
                        <div className="findings-empty__mark">
                            ✓
                        </div>

                        <h2>
                            No findings detected.
                        </h2>

                        <p>
                            This analysis did not
                            produce any security
                            findings.
                        </p>
                    </section>
                ) : (
                    <section className="findings-list">
                        {findings.map(
                            (finding) => (
                                <FindingCard
                                    key={finding.id}
                                    finding={finding}
                                />
                            ),
                        )}
                    </section>
                )}
            </div>
        </main>
    )
}

function FindingCard({
                         finding,
                     }: {
    finding: Finding
}) {
    return (
        <article className="finding-card">
            <header className="finding-card__header">
                <div>
                    <div className="finding-card__badges">
            <span
                className={
                    `finding-severity finding-severity--${finding.severity.toLowerCase()}`
                }
            >
              {finding.severity}
            </span>

                        <span className="finding-status">
              {formatEnum(
                  finding.status,
              )}
            </span>
                    </div>

                    <h2>
                        {finding.title}
                    </h2>

                    <p className="finding-rule">
                        {finding.ruleKey}
                        {' · '}
                        v{finding.ruleVersion}
                        {' · '}
                        {formatEnum(
                            finding.category,
                        )}
                    </p>
                </div>
            </header>

            <section className="finding-location">
                <div>
          <span>
            File
          </span>

                    <strong>
                        {finding.filePath}
                    </strong>
                </div>

                <div>
          <span>
            Lines
          </span>

                    <strong>
                        {formatLines(
                            finding.startLine,
                            finding.endLine,
                        )}
                    </strong>
                </div>
            </section>

            {finding.codeExcerpt && (
                <section className="finding-code">
                    <div className="finding-code__header">
                        Evidence
                    </div>

                    <pre>
            <code>
              {finding.codeExcerpt}
            </code>
          </pre>
                </section>
            )}

            <section className="finding-details">
                <FindingDetail
                    title="Description"
                    content={
                        finding.description
                    }
                />

                <FindingDetail
                    title="Impact"
                    content={
                        finding.impact
                    }
                />

                <FindingDetail
                    title="Recommendation"
                    content={
                        finding.recommendation
                    }
                />
            </section>

            <footer className="finding-card__footer">
        <span>
          Fingerprint
        </span>

                <code>
                    {finding.fingerprint}
                </code>
            </footer>
        </article>
    )
}

function FindingDetail({
                           title,
                           content,
                       }: {
    title: string
    content: string
}) {
    return (
        <div className="finding-detail">
      <span>
        {title}
      </span>

            <p>
                {content}
            </p>
        </div>
    )
}

function SummaryMetric({
                           label,
                           value,
                           severity,
                       }: {
    label: string
    value: number
    severity?: FindingSeverity
}) {
    return (
        <article className="findings-summary__item">
      <span>
        {label}
      </span>

            <strong
                className={
                    severity
                        ? `findings-summary__value findings-summary__value--${severity.toLowerCase()}`
                        : 'findings-summary__value'
                }
            >
                {value}
            </strong>
        </article>
    )
}

function countSeverity(
    findings: Finding[],
    severity: FindingSeverity,
) {
    return findings.filter(
        (finding) =>
            finding.severity === severity,
    ).length
}

function formatLines(
    startLine: number,
    endLine: number,
) {
    if (startLine === endLine) {
        return String(startLine)
    }

    return `${startLine}–${endLine}`
}

function formatEnum(
    value: string,
) {
    return value
        .toLowerCase()
        .split('_')
        .map(
            (part) =>
                part.charAt(0).toUpperCase()
                + part.slice(1),
        )
        .join(' ')
}