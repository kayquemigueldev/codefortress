import {
    useQuery,
} from '@tanstack/react-query'
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