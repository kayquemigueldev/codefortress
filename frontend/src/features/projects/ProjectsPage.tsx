import {
    useQuery,
} from '@tanstack/react-query'
import {
    Link,
} from 'react-router'

import {
    listProjects,
} from './project-api'

export function ProjectsPage() {
    const {
        data: projects,
        isPending,
        isError,
        refetch,
    } = useQuery({
        queryKey: ['projects'],
        queryFn: listProjects,
    })

    if (isPending) {
        return (
            <main className="projects-page">
                <div className="projects-container">
                    <p className="projects-eyebrow">
                        Projects
                    </p>

                    <h1>
                        Loading projects...
                    </h1>
                </div>
            </main>
        )
    }

    if (isError) {
        return (
            <main className="projects-page">
                <div className="projects-container">
                    <p className="projects-eyebrow">
                        Projects
                    </p>

                    <h1>
                        Unable to load projects.
                    </h1>

                    <p className="projects-description">
                        Something went wrong while
                        loading your workspace.
                    </p>

                    <button
                        className="projects-primary-button"
                        type="button"
                        onClick={() => {
                            void refetch()
                        }}
                    >
                        Try again
                    </button>
                </div>
            </main>
        )
    }

    return (
        <main className="projects-page">
            <div className="projects-container">
                <header className="projects-header">
                    <div>
                        <p className="projects-eyebrow">
                            Workspace
                        </p>

                        <h1>
                            Projects
                        </h1>

                        <p className="projects-description">
                            Manage the codebases you
                            want to analyze with
                            CodeFortress.
                        </p>
                    </div>

                    <Link
                        className="projects-primary-button"
                        to="/app/projects/new"
                    >
                        New project
                    </Link>
                </header>

                {projects.length === 0 ? (
                    <section className="projects-empty">
                        <div className="projects-empty__mark">
                            CF
                        </div>

                        <h2>
                            No projects yet.
                        </h2>

                        <p>
                            Create your first project
                            to start running security
                            analyses.
                        </p>

                        <Link
                            className="projects-primary-button"
                            to="/app/projects/new"
                        >
                            Create first project
                        </Link>
                    </section>
                ) : (
                    <section className="projects-grid">
                        {projects.map(
                            (project) => (
                                <Link
                                    className="project-card"
                                    key={project.id}
                                    to={
                                        `/app/projects/${project.id}`
                                    }
                                >
                                    <div className="project-card__top">
                                        <div className="project-card__icon">
                                            {project.name
                                                .slice(0, 2)
                                                .toUpperCase()}
                                        </div>

                                        <span className="project-status">
                      {project.status}
                    </span>
                                    </div>

                                    <div>
                                        <h2>
                                            {project.name}
                                        </h2>

                                        <p>
                                            {project.description
                                                || 'No description provided.'}
                                        </p>
                                    </div>

                                    <div className="project-card__footer">
                    <span>
                      Created
                    </span>

                                        <time
                                            dateTime={
                                                project.createdAt
                                            }
                                        >
                                            {new Intl.DateTimeFormat(
                                                'en',
                                                {
                                                    dateStyle:
                                                        'medium',
                                                },
                                            ).format(
                                                new Date(
                                                    project.createdAt,
                                                ),
                                            )}
                                        </time>
                                    </div>
                                </Link>
                            ),
                        )}
                    </section>
                )}
            </div>
        </main>
    )
}