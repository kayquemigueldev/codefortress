import {
    useMemo,
    useState,
} from 'react'

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
    const [search, setSearch] =
        useState('')

    const {
        data: projects,
        isPending,
        isError,
        refetch,
    } = useQuery({
        queryKey: ['projects'],
        queryFn: listProjects,
    })

    const filteredProjects =
        useMemo(() => {
            if (!projects) {
                return []
            }

            const normalizedSearch =
                search
                    .trim()
                    .toLowerCase()

            if (!normalizedSearch) {
                return projects
            }

            return projects.filter(
                (project) => {
                    const name =
                        project.name
                            .toLowerCase()

                    const description =
                        project.description
                            ?.toLowerCase()
                        ?? ''

                    return (
                        name.includes(
                            normalizedSearch,
                        )
                        || description.includes(
                            normalizedSearch,
                        )
                    )
                },
            )
        }, [
            projects,
            search,
        ])

    if (isPending) {
        return (
            <main className="projects-v2">
                <div className="projects-v2__container">
                    <p className="projects-v2__eyebrow">
                        Workspace
                    </p>

                    <h1 className="projects-v2__state-title">
                        Loading projects...
                    </h1>
                </div>
            </main>
        )
    }

    if (isError) {
        return (
            <main className="projects-v2">
                <div className="projects-v2__container">
                    <p className="projects-v2__eyebrow">
                        Workspace
                    </p>

                    <h1 className="projects-v2__state-title">
                        Unable to load projects.
                    </h1>

                    <p className="projects-v2__state-description">
                        Something went wrong while
                        loading your workspace.
                    </p>

                    <button
                        className="projects-v2__primary-button"
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

    const activeProjects =
        projects.filter(
            (project) =>
                project.status === 'ACTIVE',
        ).length

    const latestUpdatedAt =
        projects.length === 0
            ? null
            : projects.reduce(
                (latest, project) =>
                    new Date(
                        project.updatedAt,
                    ).getTime()
                    > new Date(
                        latest,
                    ).getTime()
                        ? project.updatedAt
                        : latest,
                projects[0].updatedAt,
            )

    return (
        <main className="projects-v2">
            <div className="projects-v2__container">
                <header className="projects-v2__header">
                    <div>
                        <p className="projects-v2__eyebrow">
                            Workspace
                        </p>

                        <h1>
                            Projects
                        </h1>

                        <p className="projects-v2__description">
                            Manage the codebases monitored
                            by the CodeFortress Security
                            Engine.
                        </p>
                    </div>

                    <Link
                        className="projects-v2__primary-button"
                        to="/app/projects/new"
                    >
                        <PlusIcon />

                        New project
                    </Link>
                </header>

                {projects.length > 0 && (
                    <>
                        <section
                            className="projects-v2__summary"
                            aria-label="Project summary"
                        >
                            <SummaryCard
                                label="Total projects"
                                value={
                                    projects.length
                                }
                                description="Projects in this workspace"
                                icon={
                                    <FolderIcon />
                                }
                                tone="green"
                            />

                            <SummaryCard
                                label="Active projects"
                                value={
                                    activeProjects
                                }
                                description="Available for security analysis"
                                icon={
                                    <ActivityIcon />
                                }
                                tone="blue"
                            />

                            <SummaryCard
                                label="Last update"
                                value={
                                    latestUpdatedAt
                                        ? formatDate(
                                            latestUpdatedAt,
                                        )
                                        : '—'
                                }
                                description="Most recently updated project"
                                icon={
                                    <ClockIcon />
                                }
                                tone="neutral"
                                compact
                            />
                        </section>

                        <section className="projects-v2__workspace">
                            <div className="projects-v2__toolbar">
                                <div>
                                    <p className="projects-v2__section-eyebrow">
                                        Project inventory
                                    </p>

                                    <h2>
                                        Your projects
                                    </h2>
                                </div>

                                <span className="projects-v2__count">
                                    {
                                        filteredProjects.length
                                    }
                                    {' '}
                                    {
                                        filteredProjects.length === 1
                                            ? 'project'
                                            : 'projects'
                                    }
                                </span>
                            </div>

                            <div className="projects-v2__search">
                                <SearchIcon />

                                <input
                                    aria-label="Search projects"
                                    placeholder="Search projects..."
                                    type="search"
                                    value={search}
                                    onChange={
                                        (event) => {
                                            setSearch(
                                                event.target.value,
                                            )
                                        }
                                    }
                                />
                            </div>

                            {filteredProjects.length === 0 ? (
                                <div className="projects-v2__search-empty">
                                    <SearchIcon />

                                    <h3>
                                        No projects found.
                                    </h3>

                                    <p>
                                        Try another project
                                        name or description.
                                    </p>
                                </div>
                            ) : (
                                <div className="projects-v2__list">
                                    {filteredProjects.map(
                                        (project) => (
                                            <Link
                                                className="projects-v2-card"
                                                key={
                                                    project.id
                                                }
                                                to={
                                                    `/app/projects/${project.id}`
                                                }
                                            >
                                                <div className="projects-v2-card__identity">
                                                    <div className="projects-v2-card__icon">
                                                        {
                                                            getProjectInitials(
                                                                project.name,
                                                            )
                                                        }
                                                    </div>

                                                    <div className="projects-v2-card__main">
                                                        <div className="projects-v2-card__heading">
                                                            <h3>
                                                                {
                                                                    project.name
                                                                }
                                                            </h3>

                                                            <span
                                                                className={
                                                                    `projects-v2-card__status projects-v2-card__status--${project.status.toLowerCase()}`
                                                                }
                                                            >
                                                                <span />

                                                                {
                                                                    project.status
                                                                }
                                                            </span>
                                                        </div>

                                                        <p>
                                                            {
                                                                project.description
                                                                || 'No description provided.'
                                                            }
                                                        </p>
                                                    </div>
                                                </div>

                                                <div className="projects-v2-card__metadata">
                                                    <ProjectMetadata
                                                        label="Created"
                                                        value={
                                                            formatDate(
                                                                project.createdAt,
                                                            )
                                                        }
                                                    />

                                                    <ProjectMetadata
                                                        label="Updated"
                                                        value={
                                                            formatDate(
                                                                project.updatedAt,
                                                            )
                                                        }
                                                    />
                                                </div>

                                                <div className="projects-v2-card__action">
                                                    <span>
                                                        Open project
                                                    </span>

                                                    <ArrowRightIcon />
                                                </div>
                                            </Link>
                                        ),
                                    )}
                                </div>
                            )}
                        </section>
                    </>
                )}

                {projects.length === 0 && (
                    <section className="projects-v2__empty">
                        <div className="projects-v2__empty-icon">
                            <FolderIcon />
                        </div>

                        <p className="projects-v2__section-eyebrow">
                            Project inventory
                        </p>

                        <h2>
                            No projects yet.
                        </h2>

                        <p>
                            Create your first project
                            to start running static
                            security analyses.
                        </p>

                        <Link
                            className="projects-v2__primary-button"
                            to="/app/projects/new"
                        >
                            <PlusIcon />

                            Create first project
                        </Link>
                    </section>
                )}
            </div>
        </main>
    )
}

function SummaryCard({
    label,
    value,
    description,
    icon,
    tone,
    compact = false,
}: {
    label: string
    value: number | string
    description: string
    icon: React.ReactNode
    tone:
        | 'green'
        | 'blue'
        | 'neutral'
    compact?: boolean
}) {
    return (
        <article
            className={
                `projects-v2-summary projects-v2-summary--${tone}`
            }
        >
            <div className="projects-v2-summary__top">
                <div className="projects-v2-summary__icon">
                    {icon}
                </div>

                <span>
                    {label}
                </span>
            </div>

            <strong
                className={
                    compact
                        ? 'projects-v2-summary__value projects-v2-summary__value--compact'
                        : 'projects-v2-summary__value'
                }
            >
                {value}
            </strong>

            <p>
                {description}
            </p>
        </article>
    )
}

function ProjectMetadata({
    label,
    value,
}: {
    label: string
    value: string
}) {
    return (
        <div>
            <span>
                {label}
            </span>

            <strong>
                {value}
            </strong>
        </div>
    )
}

function getProjectInitials(
    name: string,
) {
    return name
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map(
            (part) =>
                part.charAt(0),
        )
        .join('')
        .toUpperCase()
}

function formatDate(
    value: string,
) {
    return new Intl.DateTimeFormat(
        'en',
        {
            dateStyle: 'medium',
        },
    ).format(
        new Date(value),
    )
}

function PlusIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M12 5v14M5 12h14" />
        </svg>
    )
}

function FolderIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M3.5 7.5h6l2-2h9v13h-17z" />
        </svg>
    )
}

function ActivityIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M4 12h3l2-5 4 10 2-5h5" />
        </svg>
    )
}

function ClockIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <circle
                cx="12"
                cy="12"
                r="8"
            />

            <path d="M12 8v4l3 2" />
        </svg>
    )
}

function SearchIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <circle
                cx="11"
                cy="11"
                r="6"
            />

            <path d="m16 16 4 4" />
        </svg>
    )
}

function ArrowRightIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M5 12h14M14 7l5 5-5 5" />
        </svg>
    )
}
