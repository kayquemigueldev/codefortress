import {
    useQuery,
} from '@tanstack/react-query'
import {
    listAnalyses,
} from '../analyses/analysis-api'
import {
    Link,
    useParams,
} from 'react-router'

import { ApiError } from '../../lib/api/api-error'

import { getProject } from './project-api'

export function ProjectDetailsPage() {
    const { projectId } =
        useParams<{
            projectId: string
        }>()

    const {
        data: project,
        isPending,
        error,
        refetch,
    } = useQuery({
        queryKey: [
            'projects',
            projectId,
        ],

        queryFn: () =>
            getProject(
                projectId ?? '',
            ),

        enabled:
            Boolean(projectId),
    })

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
    })

    if (!projectId) {
        return (
            <main className="projects-page">
                <div className="project-details-container">
                    <h1>
                        Invalid project.
                    </h1>
                </div>
            </main>
        )
    }

    if (isPending) {
        return (
            <main className="projects-page">
                <div className="project-details-container">
                    <p className="projects-eyebrow">
                        Project
                    </p>

                    <h1>
                        Loading project...
                    </h1>
                </div>
            </main>
        )
    }

    if (error) {
        const projectNotFound =
            error instanceof ApiError
            && error.status === 404

        return (
            <main className="projects-page">
                <div className="project-details-container">
                    <p className="projects-eyebrow">
                        Project
                    </p>

                    <h1>
                        {projectNotFound
                            ? 'Project not found.'
                            : 'Unable to load project.'}
                    </h1>

                    <p className="projects-description">
                        {projectNotFound
                            ? 'This project does not exist or does not belong to your account.'
                            : 'Something went wrong while loading this project.'}
                    </p>

                    {!projectNotFound && (
                        <button
                            className="projects-primary-button"
                            type="button"
                            onClick={() => {
                                void refetch()
                            }}
                        >
                            Try again
                        </button>
                    )}

                    <Link
                        className="project-back-link project-details-back"
                        to="/app/projects"
                    >
                        ← Back to projects
                    </Link>
                </div>
            </main>
        )
    }

    const latestAnalysis =
        analysesQuery.data?.[0]
        ?? null

    return (
        <main className="projects-page">
            <div className="project-details-container">
                <Link
                    className="project-back-link"
                    to="/app/projects"
                >
                    ← Back to projects
                </Link>

                <header className="project-details-header">
                    <div>
                        <div className="project-details-heading">
                            <div className="project-card__icon project-details-icon">
                                {project.name
                                    .slice(0, 2)
                                    .toUpperCase()}
                            </div>

                            <span className="project-status">
                {project.status}
              </span>
                        </div>

                        <p className="projects-eyebrow">
                            Project
                        </p>

                        <h1>
                            {project.name}
                        </h1>

                        <p className="project-details-description">
                            {project.description
                                || 'No description provided.'}
                        </p>
                    </div>
                </header>

                <nav
                    className="project-tabs"
                    aria-label="Project sections"
                >
          <span
              className="project-tab project-tab--active"
              aria-current="page"
          >
            Overview
          </span>

                    <Link
                        className="project-tab"
                        to={
                            `/app/projects/${project.id}/analyses`
                        }
                    >
                        Analyses
                    </Link>

                    <span className="project-tab">
            Findings
          </span>
                </nav>

                <section className="project-security-overview">
                    <div className="project-security-overview__heading">
                        <div>
                            <p className="projects-eyebrow">
                                Latest security analysis
                            </p>

                            <h2>
                                Security posture
                            </h2>
                        </div>

                        <Link
                            className="project-security-link"
                            to={
                                `/app/projects/${project.id}/analyses`
                            }
                        >
                            View analysis history →
                        </Link>
                    </div>

                    {analysesQuery.isPending && (
                        <div className="project-security-state">
                            Loading security data...
                        </div>
                    )}

                    {analysesQuery.isError && (
                        <div className="project-security-state">
                            Unable to load the latest
                            security analysis.
                        </div>
                    )}

                    {!analysesQuery.isPending
                        && !analysesQuery.isError
                        && !latestAnalysis && (
                            <div className="project-security-empty">
                                <div>
                                    <h3>
                                        No security analysis yet.
                                    </h3>

                                    <p>
                                        Upload a source archive to
                                        calculate the first security
                                        score for this project.
                                    </p>
                                </div>

                                <Link
                                    className="projects-primary-button"
                                    to={
                                        `/app/projects/${project.id}/analyses`
                                    }
                                >
                                    Run first analysis
                                </Link>
                            </div>
                        )}

                    {latestAnalysis && (
                        <div className="project-security-card">
                            <div className="project-security-card__top">
                                <div>
                                    <p className="project-security-analysis-number">
                                        Analysis #
                                        {latestAnalysis.sequenceNumber}
                                    </p>

                                    <h3>
                                        {latestAnalysis.sourceFilename}
                                    </h3>
                                </div>

                                <span
                                    className={
                                        `project-security-status project-security-status--${latestAnalysis.status.toLowerCase()}`
                                    }
                                >
          {latestAnalysis.status}
        </span>
                            </div>

                            <div className="project-security-metrics">
                                <div>
          <span>
            Security score
          </span>

                                    <strong className="project-security-score">
                                        {latestAnalysis.securityScore
                                            ?? '—'}
                                    </strong>
                                </div>

                                <div>
          <span>
            Findings
          </span>

                                    <strong>
                                        {latestAnalysis.findingsCount
                                            ?? '—'}
                                    </strong>
                                </div>

                                <div>
          <span>
            Files scanned
          </span>

                                    <strong>
                                        {latestAnalysis.filesScanned
                                            ?? '—'}
                                    </strong>
                                </div>

                                <div>
          <span>
            Lines scanned
          </span>

                                    <strong>
                                        {latestAnalysis.linesScanned
                                        !== null
                                            ? new Intl.NumberFormat(
                                                'en',
                                            ).format(
                                                latestAnalysis.linesScanned,
                                            )
                                            : '—'}
                                    </strong>
                                </div>
                            </div>

                            <footer className="project-security-card__footer">
        <span>
          Latest analysis
        </span>

                                {latestAnalysis.status
                                    === 'COMPLETED' && (
                                        <Link
                                            to={
                                                `/app/projects/${project.id}/analyses/${latestAnalysis.id}/findings`
                                            }
                                        >
                                            View findings →
                                        </Link>
                                    )}
                            </footer>
                        </div>
                    )}
                </section>

                <section className="project-overview-grid">
                    <article className="project-overview-card">
                        <p className="project-overview-label">
                            Status
                        </p>

                        <strong>
                            {project.status}
                        </strong>

                        <p>
                            This project is currently
                            available for security
                            analysis.
                        </p>
                    </article>

                    <article className="project-overview-card">
                        <p className="project-overview-label">
                            Created
                        </p>

                        <strong>
                            {formatDate(
                                project.createdAt,
                            )}
                        </strong>

                        <p>
                            Project creation date in
                            CodeFortress.
                        </p>
                    </article>

                    <article className="project-overview-card">
                        <p className="project-overview-label">
                            Last updated
                        </p>

                        <strong>
                            {formatDate(
                                project.updatedAt,
                            )}
                        </strong>

                        <p>
                            Last change to project
                            information.
                        </p>
                    </article>
                </section>

                <section className="project-information">
                    <div>
                        <p className="projects-eyebrow">
                            Project information
                        </p>

                        <h2>
                            About this codebase
                        </h2>
                    </div>

                    <dl className="project-information-list">
                        <div>
                            <dt>
                                Project ID
                            </dt>

                            <dd>
                                {project.id}
                            </dd>
                        </div>

                        <div>
                            <dt>
                                Name
                            </dt>

                            <dd>
                                {project.name}
                            </dd>
                        </div>

                        <div>
                            <dt>
                                Description
                            </dt>

                            <dd>
                                {project.description
                                    || 'No description provided.'}
                            </dd>
                        </div>
                    </dl>
                </section>
            </div>
        </main>
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