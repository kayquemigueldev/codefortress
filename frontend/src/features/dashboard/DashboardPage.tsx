import {
    useQuery,
} from '@tanstack/react-query'
import {
    Link,
} from 'react-router'

import {
    getDashboardOverview,
} from './dashboard-api'

import {
    securityRules,
} from '../security-rules/security-rules-data'

import type {
    DashboardLatestAnalysis,
} from './dashboard-types'

export function DashboardPage() {
    const dashboardQuery = useQuery({
        queryKey: ['dashboard'],
        queryFn: getDashboardOverview,
    })

    if (dashboardQuery.isPending) {
        return (
            <main className="dashboard-v2">
                <div className="dashboard-v2__container">
                    <p className="dashboard-v2__eyebrow">
                        Dashboard
                    </p>

                    <h1 className="dashboard-v2__state-title">
                        Loading security overview...
                    </h1>
                </div>
            </main>
        )
    }

    if (dashboardQuery.isError) {
        return (
            <main className="dashboard-v2">
                <div className="dashboard-v2__container">
                    <p className="dashboard-v2__eyebrow">
                        Dashboard
                    </p>

                    <h1 className="dashboard-v2__state-title">
                        Unable to load security overview.
                    </h1>

                    <p className="dashboard-v2__state-description">
                        Something went wrong while loading
                        your workspace security data.
                    </p>

                    <button
                        className="dashboard-v2__primary-action"
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

    const activeRules =
        securityRules.filter(
            (rule) => rule.active,
        ).length

    const highRiskFindings =
        overview.criticalOpenFindings
        + overview.highOpenFindings

    const severityTotal =
        overview.criticalOpenFindings
        + overview.highOpenFindings
        + overview.mediumOpenFindings
        + overview.lowOpenFindings

    return (
        <main className="dashboard-v2">
            <div className="dashboard-v2__container">
                <header className="dashboard-v2__header">
                    <div>
                        <p className="dashboard-v2__eyebrow">
                            Dashboard
                        </p>

                        <h1>
                            Security Overview
                        </h1>

                        <p className="dashboard-v2__subtitle">
                            Monitor your workspace security
                            posture and track the results of
                            your analyses.
                        </p>
                    </div>

                    <Link
                        className="dashboard-v2__primary-action"
                        to="/app/projects"
                    >
                        <PlayIcon />

                        Run analysis
                    </Link>
                </header>

                <section className="dashboard-v2__hero-grid">
                    <SecurityScoreCard
                        score={
                            overview.averageSecurityScore
                        }
                        activeRules={activeRules}
                        openFindings={
                            overview.openFindings
                        }
                        highRiskFindings={
                            highRiskFindings
                        }
                        activeProjects={
                            overview.activeProjects
                        }
                    />

                    <div className="dashboard-v2__metric-grid">
                        <MetricCard
                            icon={<ProjectsIcon />}
                            label="Projects"
                            value={
                                overview.activeProjects
                            }
                            description="Active projects"
                            tone="green"
                        />

                        <MetricCard
                            icon={<RulesIcon />}
                            label="Rules"
                            value={activeRules}
                            description="Active security rules"
                            tone="blue"
                        />

                        <MetricCard
                            icon={<FindingsIcon />}
                            label="Open findings"
                            value={
                                overview.openFindings
                            }
                            description="Require review"
                            tone="amber"
                        />

                        <MetricCard
                            icon={<RiskIcon />}
                            label="High risk"
                            value={
                                highRiskFindings
                            }
                            description="Critical and high"
                            tone="red"
                        />
                    </div>
                </section>

                <section className="dashboard-v2__insights-grid">
                    <SeverityCard
                        critical={
                            overview
                                .criticalOpenFindings
                        }
                        high={
                            overview
                                .highOpenFindings
                        }
                        medium={
                            overview
                                .mediumOpenFindings
                        }
                        low={
                            overview
                                .lowOpenFindings
                        }
                        total={severityTotal}
                    />

                    <ScoreHistoryCard
                        analyses={
                            overview.recentAnalyses
                        }
                    />
                </section>

                <section className="dashboard-v2__activity-grid">
                    <RecentAnalysesCard
                        analyses={
                            overview.recentAnalyses
                        }
                    />

                    <div className="dashboard-v2__side-stack">
                        <LatestAnalysisCard
                            analysis={latest}
                        />

                        <EngineCard
                            activeRules={activeRules}
                            latest={latest}
                        />
                    </div>
                </section>

            </div>
        </main>
    )
}

function SecurityScoreCard({
                               score,
                               activeRules,
                               openFindings,
                               highRiskFindings,
                               activeProjects,
                           }: {
    score: number | null
    activeRules: number
    openFindings: number
    highRiskFindings: number
    activeProjects: number
}) {
    const scoreValue =
        score ?? 0

    return (
        <article className="security-score-card">
            <div className="security-score-card__heading">
                <span>
                    Security score
                </span>

                <InfoIcon />
            </div>

            <div className="security-score-card__content">
                <div
                    className="security-score-ring"
                    style={{
                        '--score':
                            `${scoreValue * 3.6}deg`,
                    } as React.CSSProperties}
                >
                    <div className="security-score-ring__inner">
                        <strong>
                            {score ?? '—'}
                        </strong>

                        <span>
                            Workspace score
                        </span>
                    </div>
                </div>

                <div className="security-score-card__summary">
                    <div>
                        <h2>
                            Your current security posture
                        </h2>

                        <p>
                            CodeFortress is monitoring your
                            projects with active static
                            analysis rules and highlighting
                            findings that require review.
                        </p>
                    </div>

                    <div className="security-score-card__facts">
                        <SecurityFact
                            icon={<ShieldIcon />}
                            value={activeRules}
                            label="active security rules"
                            tone="green"
                        />

                        <SecurityFact
                            icon={<FindingsIcon />}
                            value={openFindings}
                            label="open findings"
                            tone="amber"
                        />

                        <SecurityFact
                            icon={<RiskIcon />}
                            value={highRiskFindings}
                            label="high risk findings"
                            tone="red"
                        />

                        <SecurityFact
                            icon={<ProjectsIcon />}
                            value={activeProjects}
                            label={
                                activeProjects === 1
                                    ? 'active project'
                                    : 'active projects'
                            }
                            tone="neutral"
                        />
                    </div>
                </div>
            </div>
        </article>
    )
}

function SecurityFact({
                          icon,
                          value,
                          label,
                          tone,
                      }: {
    icon: React.ReactNode
    value: number
    label: string
    tone:
        | 'green'
        | 'amber'
        | 'red'
        | 'neutral'
}) {
    return (
        <div className="security-score-fact">
            <span
                className={
                    `security-score-fact__icon security-score-fact__icon--${tone}`
                }
            >
                {icon}
            </span>

            <p>
                <strong>
                    {value}
                </strong>

                {' '}
                {label}
            </p>
        </div>
    )
}

function MetricCard({
                        icon,
                        label,
                        value,
                        description,
                        tone,
                    }: {
    icon: React.ReactNode
    label: string
    value: number
    description: string
    tone:
        | 'green'
        | 'blue'
        | 'amber'
        | 'red'
}) {
    return (
        <article
            className={
                `dashboard-v2-metric dashboard-v2-metric--${tone}`
            }
        >
            <span className="dashboard-v2-metric__icon">
                {icon}
            </span>

            <span className="dashboard-v2-metric__label">
                {label}
            </span>

            <strong>
                {value}
            </strong>

            <p>
                {description}
            </p>
        </article>
    )
}

function SeverityCard({
                          critical,
                          high,
                          medium,
                          low,
                          total,
                      }: {
    critical: number
    high: number
    medium: number
    low: number
    total: number
}) {
    const criticalPercent =
        percentage(critical, total)

    const highPercent =
        percentage(high, total)

    const mediumPercent =
        percentage(medium, total)

    const criticalEnd =
        criticalPercent

    const highEnd =
        criticalPercent + highPercent

    const mediumEnd =
        highEnd + mediumPercent

    const donutBackground =
        total === 0
            ? 'var(--cf-surface-raised)'
            : `conic-gradient(
                var(--cf-critical) 0% ${criticalEnd}%,
                var(--cf-high) ${criticalEnd}% ${highEnd}%,
                var(--cf-medium) ${highEnd}% ${mediumEnd}%,
                var(--cf-low) ${mediumEnd}% 100%
            )`

    return (
        <article className="dashboard-v2-card severity-card">
            <CardHeader
                title="Findings by severity"
                eyebrow="Risk distribution"
            />

            <div className="severity-card__content">
                <div
                    className="severity-donut"
                    style={{
                        background:
                        donutBackground,
                    }}
                >
                    <div className="severity-donut__inner">
                        <strong>
                            {total}
                        </strong>

                        <span>
                            findings
                        </span>
                    </div>
                </div>

                <div className="severity-legend">
                    <SeverityRow
                        label="Critical"
                        value={critical}
                        total={total}
                        severity="critical"
                    />

                    <SeverityRow
                        label="High"
                        value={high}
                        total={total}
                        severity="high"
                    />

                    <SeverityRow
                        label="Medium"
                        value={medium}
                        total={total}
                        severity="medium"
                    />

                    <SeverityRow
                        label="Low"
                        value={low}
                        total={total}
                        severity="low"
                    />
                </div>
            </div>
        </article>
    )
}

function SeverityRow({
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
    return (
        <div className="severity-row">
            <div>
                <span
                    className={
                        `severity-row__dot severity-row__dot--${severity}`
                    }
                />

                <span>
                    {label}
                </span>
            </div>

            <strong>
                {value}
            </strong>

            <span>
                {Math.round(
                    percentage(
                        value,
                        total,
                    ),
                )}
                %
            </span>
        </div>
    )
}

function ScoreHistoryCard({
                              analyses,
                          }: {
    analyses: DashboardLatestAnalysis[]
}) {
    const scoredAnalyses =
        analyses
            .filter(
                (analysis) =>
                    analysis.status === 'COMPLETED'
                    && analysis.securityScore !== null,
            )
            .slice()
            .reverse()

    const chartWidth = 700
    const chartHeight = 240

    const paddingLeft = 46
    const paddingRight = 24
    const paddingTop = 24
    const paddingBottom = 42

    const usableWidth =
        chartWidth
        - paddingLeft
        - paddingRight

    const usableHeight =
        chartHeight
        - paddingTop
        - paddingBottom

    const points =
        scoredAnalyses.map(
            (analysis, index) => {
                const score =
                    analysis.securityScore
                    ?? 0

                const x =
                    scoredAnalyses.length === 1
                        ? paddingLeft
                        + usableWidth / 2
                        : paddingLeft
                        + (
                            index
                            / (
                                scoredAnalyses.length
                                - 1
                            )
                        )
                        * usableWidth

                const y =
                    paddingTop
                    + (
                        (100 - score)
                        / 100
                    )
                    * usableHeight

                return {
                    analysis,
                    x,
                    y,
                    score,
                }
            },
        )

    const polylinePoints =
        points
            .map(
                (point) =>
                    `${point.x},${point.y}`,
            )
            .join(' ')

    return (
        <article className="dashboard-v2-card score-history-card">
            <CardHeader
                title="Security score history"
                eyebrow="Score trend"
            />

            {points.length === 0 ? (
                <div className="score-history-card__empty">
                    <ShieldIcon />

                    <h3>
                        No score history yet
                    </h3>

                    <p>
                        Completed analyses with a
                        security score will appear
                        here.
                    </p>
                </div>
            ) : (
                <div className="score-history-chart">
                    <svg
                        role="img"
                        aria-label="Security scores from recent completed analyses"
                        viewBox={
                            `0 0 ${chartWidth} ${chartHeight}`
                        }
                    >
                        {[100, 75, 50, 25, 0].map(
                            (value) => {
                                const y =
                                    paddingTop
                                    + (
                                        (100 - value)
                                        / 100
                                    )
                                    * usableHeight

                                return (
                                    <g key={value}>
                                        <line
                                            className="score-history-grid-line"
                                            x1={paddingLeft}
                                            x2={
                                                chartWidth
                                                - paddingRight
                                            }
                                            y1={y}
                                            y2={y}
                                        />

                                        <text
                                            className="score-history-axis-label"
                                            x="8"
                                            y={y + 4}
                                        >
                                            {value}
                                        </text>
                                    </g>
                                )
                            },
                        )}

                        {points.length > 1 && (
                            <polyline
                                className="score-history-line"
                                fill="none"
                                points={polylinePoints}
                            />
                        )}

                        {points.map(
                            (point) => (
                                <g
                                    key={
                                        point.analysis.id
                                    }
                                >
                                    <circle
                                        className="score-history-point-glow"
                                        cx={point.x}
                                        cy={point.y}
                                        r="9"
                                    />

                                    <circle
                                        className="score-history-point"
                                        cx={point.x}
                                        cy={point.y}
                                        r="4.5"
                                    />

                                    <text
                                        className="score-history-score-label"
                                        textAnchor="middle"
                                        x={point.x}
                                        y={point.y - 14}
                                    >
                                        {point.score}
                                    </text>

                                    <text
                                        className="score-history-analysis-label"
                                        textAnchor="middle"
                                        x={point.x}
                                        y={
                                            chartHeight
                                            - 12
                                        }
                                    >
                                        #
                                        {
                                            point
                                                .analysis
                                                .sequenceNumber
                                        }
                                    </text>
                                </g>
                            ),
                        )}
                    </svg>
                </div>
            )}
        </article>
    )
}

function LatestAnalysisCard({
                                analysis,
                            }: {
    analysis:
        | DashboardLatestAnalysis
        | null
}) {
    return (
        <article className="dashboard-v2-card latest-analysis-card">
            <CardHeader
                title="Latest analysis"
                eyebrow="Recent scan"
            />

            {!analysis ? (
                <div className="latest-analysis-card__empty">
                    <ShieldIcon />

                    <h3>
                        No analyses yet
                    </h3>

                    <p>
                        Run your first analysis to see
                        security results here.
                    </p>

                    <Link to="/app/projects">
                        View projects →
                    </Link>
                </div>
            ) : (
                <div className="latest-analysis-card__content">
                    <div className="latest-analysis-card__top">
                        <div>
                            <span>
                                Analysis #
                                {analysis.sequenceNumber}
                            </span>

                            <h3>
                                {analysis.projectName}
                            </h3>

                            <p>
                                {analysis.sourceFilename}
                            </p>
                        </div>

                        <AnalysisStatus
                            status={analysis.status}
                        />
                    </div>

                    <div className="latest-analysis-card__metrics">
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

                    <div className="latest-analysis-card__footer">
                        <span>
                            {formatDate(
                                analysis.createdAt,
                            )}
                        </span>

                        <div>
                            <Link
                                to={
                                    `/app/projects/${analysis.projectId}`
                                }
                            >
                                Project
                            </Link>

                            {analysis.status
                                === 'COMPLETED' && (
                                    <Link
                                        to={
                                            `/app/projects/${analysis.projectId}/analyses/${analysis.id}/findings`
                                        }
                                    >
                                        Findings →
                                    </Link>
                                )}
                        </div>
                    </div>
                </div>
            )}
        </article>
    )
}

function RecentAnalysesCard({
                                analyses,
                            }: {
    analyses: DashboardLatestAnalysis[]
}) {
    return (
        <article className="dashboard-v2-card recent-analyses-card">
            <CardHeader
                title="Recent analyses"
                eyebrow="Analysis activity"
                action={
                    <Link to="/app/projects">
                        View projects →
                    </Link>
                }
            />

            {analyses.length === 0 ? (
                <div className="recent-analyses-card__empty">
                    No recent analyses.
                </div>
            ) : (
                <div className="recent-analyses-table">
                    <div className="recent-analyses-table__header">
                        <span>
                            Analysis
                        </span>

                        <span>
                            Project
                        </span>

                        <span>
                            Score
                        </span>

                        <span>
                            Findings
                        </span>

                        <span>
                            Status
                        </span>

                        <span>
                            Date
                        </span>

                        <span />
                    </div>

                    {analyses.map(
                        (analysis) => (
                            <div
                                className="recent-analysis-row"
                                key={analysis.id}
                            >
                                <span>
                                    #
                                    {
                                        analysis
                                            .sequenceNumber
                                    }
                                </span>

                                <div>
                                    <strong>
                                        {
                                            analysis
                                                .projectName
                                        }
                                    </strong>

                                    <small>
                                        {
                                            analysis
                                                .sourceFilename
                                        }
                                    </small>
                                </div>

                                <strong className="recent-analysis-row__score">
                                    {
                                        analysis
                                            .securityScore
                                        ?? '—'
                                    }
                                </strong>

                                <span>
                                    {
                                        analysis
                                            .findingsCount
                                        ?? '—'
                                    }
                                </span>

                                <AnalysisStatus
                                    status={
                                        analysis.status
                                    }
                                />

                                <span>
                                    {formatShortDate(
                                        analysis.createdAt,
                                    )}
                                </span>

                                <Link
                                    to={
                                        analysis.status
                                        === 'COMPLETED'
                                            ? `/app/projects/${analysis.projectId}/analyses/${analysis.id}/findings`
                                            : `/app/projects/${analysis.projectId}/analyses`
                                    }
                                >
                                    →
                                </Link>
                            </div>
                        ),
                    )}
                </div>
            )}
        </article>
    )
}

function EngineCard({
                        activeRules,
                        latest,
                    }: {
    activeRules: number
    latest:
        | DashboardLatestAnalysis
        | null
}) {
    return (
        <article className="dashboard-v2-card engine-card">
            <CardHeader
                title="Security Engine"
                eyebrow="Protection"
                action={
                    <Link to="/app/security-rules">
                        View rules →
                    </Link>
                }
            />

            <div className="engine-card__status">
                <div className="engine-card__shield">
                    <ShieldIcon />
                </div>

                <div>
                    <span>
                        Engine status
                    </span>

                    <strong>
                        Operational
                    </strong>

                    <p>
                        All configured security rules
                        are active and ready to analyze
                        supported source files.
                    </p>
                </div>
            </div>

            <div className="engine-card__metrics">
                <div>
                    <strong>
                        {activeRules}
                    </strong>

                    <span>
                        Active rules
                    </span>
                </div>

                <div>
                    <strong>
                        {latest?.sequenceNumber
                            ?? '—'}
                    </strong>

                    <span>
                        Latest analysis
                    </span>
                </div>
            </div>

            <Link
                className="engine-card__action"
                to="/app/security-rules"
            >
                Explore Security Engine

                <span>
                    →
                </span>
            </Link>
        </article>
    )
}

function CardHeader({
                        eyebrow,
                        title,
                        action,
                    }: {
    eyebrow: string
    title: string
    action?: React.ReactNode
}) {
    return (
        <header className="dashboard-v2-card__header">
            <div>
                <span>
                    {eyebrow}
                </span>

                <h2>
                    {title}
                </h2>
            </div>

            {action}
        </header>
    )
}

function AnalysisStatus({
                            status,
                        }: {
    status: DashboardLatestAnalysis['status']
}) {
    return (
        <span
            className={
                `dashboard-v2-status dashboard-v2-status--${status.toLowerCase()}`
            }
        >
            {status}
        </span>
    )
}

function percentage(
    value: number,
    total: number,
) {
    if (total === 0) {
        return 0
    }

    return (value / total) * 100
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

function formatShortDate(
    value: string,
) {
    return new Intl.DateTimeFormat(
        'en',
        {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
        },
    ).format(
        new Date(value),
    )
}

function PlayIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="m8 5 11 7-11 7Z" />
        </svg>
    )
}

function InfoIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <circle
                cx="12"
                cy="12"
                r="9"
            />

            <path d="M12 11v5" />
            <path d="M12 8h.01" />
        </svg>
    )
}

function ShieldIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M12 3 19 6v5c0 4.8-2.8 8.1-7 10-4.2-1.9-7-5.2-7-10V6l7-3Z" />
            <path d="m9.5 12 1.6 1.6 3.6-3.8" />
        </svg>
    )
}

function ProjectsIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M4 7.5h6l1.7 2H20v9.5H4Z" />
            <path d="M4 7.5V5h6l1.7 2H20v2.5" />
        </svg>
    )
}

function RulesIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M7 5h13" />
            <path d="M7 12h13" />
            <path d="M7 19h13" />
            <path d="m3.5 5 1 1 2-2" />
            <path d="m3.5 12 1 1 2-2" />
            <path d="m3.5 19 1 1 2-2" />
        </svg>
    )
}

function FindingsIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M12 3 4 7v5c0 4.2 2.5 7.2 8 9 5.5-1.8 8-4.8 8-9V7Z" />
            <path d="M12 8v5" />
            <path d="M12 16h.01" />
        </svg>
    )
}

function RiskIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M12 4 21 20H3Z" />
            <path d="M12 9v5" />
            <path d="M12 17h.01" />
        </svg>
    )
}