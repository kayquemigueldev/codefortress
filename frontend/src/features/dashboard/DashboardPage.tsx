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
                            Monitor your projects,
                            security findings and latest
                            analysis activity.
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
                        label="Active projects"
                        value={
                            overview.activeProjects
                        }
                        description="Codebases currently monitored"
                    />

                    <DashboardMetric
                        label="Latest score"
                        value={
                            latest?.securityScore
                            ?? '—'
                        }
                        description={
                            latest
                                ? latest.projectName
                                : 'No analyses yet'
                        }
                        score
                    />

                    <DashboardMetric
                        label="Open findings"
                        value={
                            overview.openFindings
                        }
                        description="Issues requiring review"
                    />

                    <DashboardMetric
                        label="Critical findings"
                        value={
                            overview
                                .criticalOpenFindings
                        }
                        description="Highest severity issues"
                        critical={
                            overview
                                .criticalOpenFindings
                            > 0
                        }
                    />
                </section>

                <section className="dashboard-activity">
                    <div className="dashboard-section-heading">
                        <div>
                            <p className="projects-eyebrow">
                                Recent security activity
                            </p>

                            <h2>
                                Latest analysis
                            </h2>
                        </div>

                        <Link
                            to="/app/projects"
                        >
                            View projects →
                        </Link>
                    </div>

                    {!latest ? (
                        <div className="dashboard-empty">
                            <div>
                                <h3>
                                    No analyses yet.
                                </h3>

                                <p>
                                    Create a project and
                                    upload a source archive
                                    to run your first
                                    security analysis.
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
                        <article className="dashboard-latest-analysis">
                            <div className="dashboard-latest-analysis__header">
                                <div>
                                    <p className="dashboard-analysis-sequence">
                                        Analysis #
                                        {latest.sequenceNumber}
                                    </p>

                                    <h3>
                                        {latest.projectName}
                                    </h3>

                                    <span>
                    {latest.sourceFilename}
                  </span>
                                </div>

                                <span
                                    className={
                                        `dashboard-analysis-status dashboard-analysis-status--${latest.status.toLowerCase()}`
                                    }
                                >
                  {latest.status}
                </span>
                            </div>

                            <div className="dashboard-latest-analysis__metrics">
                                <div>
                  <span>
                    Security score
                  </span>

                                    <strong className="dashboard-latest-score">
                                        {latest.securityScore
                                            ?? '—'}
                                    </strong>
                                </div>

                                <div>
                  <span>
                    Findings
                  </span>

                                    <strong>
                                        {latest.findingsCount
                                            ?? '—'}
                                    </strong>
                                </div>

                                <div>
                  <span>
                    Created
                  </span>

                                    <strong className="dashboard-date-value">
                                        {formatDate(
                                            latest.createdAt,
                                        )}
                                    </strong>
                                </div>
                            </div>

                            <footer className="dashboard-latest-analysis__footer">
                                <Link
                                    to={
                                        `/app/projects/${latest.projectId}`
                                    }
                                >
                                    Project overview →
                                </Link>

                                <Link
                                    to={
                                        `/app/projects/${latest.projectId}/analyses`
                                    }
                                >
                                    Analysis history →
                                </Link>

                                {latest.status
                                    === 'COMPLETED' && (
                                        <Link
                                            to={
                                                `/app/projects/${latest.projectId}/analyses/${latest.id}/findings`
                                            }
                                        >
                                            View findings →
                                        </Link>
                                    )}
                            </footer>
                        </article>
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