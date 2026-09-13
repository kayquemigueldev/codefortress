import {
    useQuery,
} from '@tanstack/react-query'
import {
    Link,
} from 'react-router'

import { useAuth } from '../../lib/auth/useAuth'

import {
    getDashboardOverview,
} from './dashboard-api'

import type {
    DashboardLatestAnalysis,
} from './dashboard-types'

export function DashboardPage() {
    const { user } = useAuth()

    const dashboardQuery = useQuery({
        queryKey: ['dashboard'],
        queryFn: getDashboardOverview,
    })

    if (dashboardQuery.isPending) {
        return (
            <main className="dashboard-page">
                <div className="dashboard-container">
                    <p className="projects-eyebrow">
                        Workspace
                    </p>

                    <h1 className="dashboard-state-title">
                        Loading dashboard...
                    </h1>
                </div>
            </main>
        )
    }

    if (dashboardQuery.isError) {
        return (
            <main className="dashboard-page">
                <div className="dashboard-container">
                    <p className="projects-eyebrow">
                        Workspace
                    </p>

                    <h1 className="dashboard-state-title">
                        Unable to load dashboard.
                    </h1>

                    <p className="dashboard-state-description">
                        Something went wrong while
                        loading your security overview.
                    </p>

                    <button
                        className="projects-primary-button"
                        type="button"
                        onClick={() => {
                            void dashboardQuery.refetch()
                        }}
                    >
                        Try again
                    </button>
                </div>
            </main>
        )
    }

    const overview =
        dashboardQuery.data

    const latest =
        overview.latestAnalysis

    const highRiskFindings =
        overview.criticalOpenFindings
        + overview.highOpenFindings

    const postureTotal =
        overview.criticalOpenFindings
        + overview.highOpenFindings
        + overview.mediumOpenFindings
        + overview.lowOpenFindings

    return (
        <main className="dashboard-page">
            <div className="dashboard-container">
                <header className="dashboard-header">
                    <div>
                        <p className="projects-eyebrow">
                            Workspace overview
                        </p>

                        <h1>
                            Welcome back,
                            {' '}
                            {user?.displayName}.
                        </h1>

                        <p>
                            Monitor the current security
                            posture of your projects and
                            review recent analysis activity.
                        </p>
                    </div>

                    <Link
                        className="projects-primary-button"
                        to="/app/projects/new"
                    >
                        New project
                    </Link>
                </header>

                <section className="dashboard-metrics">
                    <DashboardMetric
                        label="Average score"
                        value={
                            overview.averageSecurityScore
                            ?? '—'
                        }
                        description={
                            overview.averageSecurityScore
                            === null
                                ? 'No completed analyses yet'
                                : 'Latest completed score per project'
                        }
                        score
                    />

                    <DashboardMetric
                        label="Active projects"
                        value={
                            overview.activeProjects
                        }
                        description="Codebases currently monitored"
                    />

                    <DashboardMetric
                        label="Open findings"
                        value={
                            overview.openFindings
                        }
                        description="Issues requiring review"
                    />

                    <DashboardMetric
                        label="High risk"
                        value={
                            highRiskFindings
                        }
                        description="Critical and high severity findings"
                        critical={
                            highRiskFindings > 0
                        }
                    />
                </section>

                <section className="dashboard-overview-grid">
                    <article className="dashboard-panel dashboard-posture">
                        <div className="dashboard-panel__heading">
                            <div>
                                <p className="projects-eyebrow">
                                    Security posture
                                </p>

                                <h2>
                                    Open findings by severity
                                </h2>
                            </div>

                            <span className="dashboard-panel-total">
                                {overview.openFindings}
                                {' '}
                                open
                            </span>
                        </div>

                        <div className="dashboard-posture-list">
                            <PostureRow
                                label="Critical"
                                value={
                                    overview
                                        .criticalOpenFindings
                                }
                                total={postureTotal}
                                severity="critical"
                            />

                            <PostureRow
                                label="High"
                                value={
                                    overview
                                        .highOpenFindings
                                }
                                total={postureTotal}
                                severity="high"
                            />

                            <PostureRow
                                label="Medium"
                                value={
                                    overview
                                        .mediumOpenFindings
                                }
                                total={postureTotal}
                                severity="medium"
                            />

                            <PostureRow
                                label="Low"
                                value={
                                    overview
                                        .lowOpenFindings
                                }
                                total={postureTotal}
                                severity="low"
                            />
                        </div>

                        {postureTotal === 0 && (
                            <p className="dashboard-posture-empty">
                                No open security findings.
                            </p>
                        )}
                    </article>

                    <article className="dashboard-panel dashboard-latest-panel">
                        <div className="dashboard-panel__heading">
                            <div>
                                <p className="projects-eyebrow">
                                    Latest activity
                                </p>

                                <h2>
                                    Latest analysis
                                </h2>
                            </div>

                            <Link to="/app/projects">
                                View projects →
                            </Link>
                        </div>

                        {!latest ? (
                            <div className="dashboard-latest-empty">
                                <h3>
                                    No analyses yet.
                                </h3>

                                <p>
                                    Run your first security
                                    analysis to populate this
                                    workspace.
                                </p>

                                <Link
                                    to="/app/projects"
                                >
                                    View projects →
                                </Link>
                            </div>
                        ) : (
                            <LatestAnalysisSummary
                                analysis={latest}
                            />
                        )}
                    </article>
                </section>

                <section className="dashboard-recent-section">
                    <div className="dashboard-section-heading">
                        <div>
                            <p className="projects-eyebrow">
                                Analysis activity
                            </p>

                            <h2>
                                Recent analyses
                            </h2>
                        </div>

                        <span className="dashboard-section-caption">
                            Latest 5 analyses across
                            active projects
                        </span>
                    </div>

                    {overview.recentAnalyses.length === 0 ? (
                        <div className="dashboard-empty">
                            <div>
                                <h3>
                                    No recent analyses.
                                </h3>

                                <p>
                                    Your latest project
                                    analyses will appear here.
                                </p>
                            </div>

                            <Link
                                className="projects-primary-button"
                                to="/app/projects"
                            >
                                View projects
                            </Link>
                        </div>
                    ) : (
                        <div className="dashboard-recent-list">
                            {overview.recentAnalyses.map(
                                (analysis) => (
                                    <RecentAnalysisRow
                                        key={analysis.id}
                                        analysis={analysis}
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

function DashboardMetric({
                             label,
                             value,
                             description,
                             score = false,
                             critical = false,
                         }: {
    label: string
    value: number | string
    description: string
    score?: boolean
    critical?: boolean
}) {
    const valueClassName = [
        'dashboard-metric__value',
        score
            ? 'dashboard-metric__value--score'
            : '',
        critical
            ? 'dashboard-metric__value--critical'
            : '',
    ]
        .filter(Boolean)
        .join(' ')

    return (
        <article className="dashboard-metric">
            <span className="dashboard-metric__label">
                {label}
            </span>

            <strong className={valueClassName}>
                {value}
            </strong>

            <p>
                {description}
            </p>
        </article>
    )
}

function PostureRow({
                        label,
                        value,
                        total,
                        severity,
                    }: {
    label: string
    value: number
    total: number
    severity:
        | 'critical'
        | 'high'
        | 'medium'
        | 'low'
}) {
    const percentage =
        total === 0
            ? 0
            : (value / total) * 100

    return (
        <div className="dashboard-posture-row">
            <div className="dashboard-posture-row__header">
                <span>
                    {label}
                </span>

                <strong>
                    {value}
                </strong>
            </div>

            <div className="dashboard-posture-track">
                <span
                    className={
                        `dashboard-posture-fill dashboard-posture-fill--${severity}`
                    }
                    style={{
                        width: `${percentage}%`,
                    }}
                />
            </div>
        </div>
    )
}

function LatestAnalysisSummary({
                                   analysis,
                               }: {
    analysis: DashboardLatestAnalysis
}) {
    return (
        <div className="dashboard-latest-summary">
            <div className="dashboard-latest-summary__header">
                <div>
                    <p className="dashboard-analysis-sequence">
                        Analysis #
                        {analysis.sequenceNumber}
                    </p>

                    <h3>
                        {analysis.projectName}
                    </h3>

                    <span>
                        {analysis.sourceFilename}
                    </span>
                </div>

                <span
                    className={
                        `dashboard-analysis-status dashboard-analysis-status--${analysis.status.toLowerCase()}`
                    }
                >
                    {analysis.status}
                </span>
            </div>

            <div className="dashboard-latest-summary__score">
                <div>
                    <span>
                        Security score
                    </span>

                    <strong>
                        {analysis.securityScore
                            ?? '—'}
                    </strong>
                </div>

                <div>
                    <span>
                        Findings
                    </span>

                    <strong>
                        {analysis.findingsCount
                            ?? '—'}
                    </strong>
                </div>
            </div>

            <p className="dashboard-latest-summary__date">
                Created
                {' '}
                {formatDate(
                    analysis.createdAt,
                )}
            </p>

            <div className="dashboard-latest-summary__links">
                <Link
                    to={
                        `/app/projects/${analysis.projectId}`
                    }
                >
                    Project overview →
                </Link>

                {analysis.status === 'COMPLETED' && (
                    <Link
                        to={
                            `/app/projects/${analysis.projectId}/analyses/${analysis.id}/findings`
                        }
                    >
                        View findings →
                    </Link>
                )}
            </div>
        </div>
    )
}

function RecentAnalysisRow({
                               analysis,
                           }: {
    analysis: DashboardLatestAnalysis
}) {
    return (
        <article className="dashboard-recent-row">
            <div className="dashboard-recent-row__identity">
                <span>
                    Analysis #
                    {analysis.sequenceNumber}
                </span>

                <strong>
                    {analysis.projectName}
                </strong>

                <p>
                    {analysis.sourceFilename}
                </p>
            </div>

            <div className="dashboard-recent-row__score">
                <span>
                    Score
                </span>

                <strong>
                    {analysis.securityScore
                        ?? '—'}
                </strong>
            </div>

            <div className="dashboard-recent-row__findings">
                <span>
                    Findings
                </span>

                <strong>
                    {analysis.findingsCount
                        ?? '—'}
                </strong>
            </div>

            <div className="dashboard-recent-row__status">
                <span
                    className={
                        `dashboard-analysis-status dashboard-analysis-status--${analysis.status.toLowerCase()}`
                    }
                >
                    {analysis.status}
                </span>

                <small>
                    {formatDate(
                        analysis.createdAt,
                    )}
                </small>
            </div>

            <Link
                className="dashboard-recent-row__link"
                to={
                    analysis.status === 'COMPLETED'
                        ? `/app/projects/${analysis.projectId}/analyses/${analysis.id}/findings`
                        : `/app/projects/${analysis.projectId}/analyses`
                }
            >
                View →
            </Link>
        </article>
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