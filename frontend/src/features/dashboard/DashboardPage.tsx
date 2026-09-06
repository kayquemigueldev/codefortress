import {
    Link,
} from 'react-router'

import { useAuth } from '../../lib/auth/useAuth'

export function DashboardPage() {
    const { user } = useAuth()

    return (
        <main className="dashboard-page">
            <div className="dashboard-container">
                <header className="dashboard-header">
                    <p className="projects-eyebrow">
                        Workspace
                    </p>

                    <h1>
                        Welcome back,
                        {' '}
                        {user?.displayName}.
                    </h1>

                    <p>
                        Review your projects and
                        continue securing your
                        codebase.
                    </p>
                </header>

                <section className="dashboard-actions">
                    <Link
                        className="dashboard-action-card"
                        to="/app/projects"
                    >
            <span>
              Projects
            </span>

                        <strong>
                            View your codebases
                        </strong>

                        <p>
                            Manage projects and
                            security analysis targets.
                        </p>
                    </Link>

                    <Link
                        className="dashboard-action-card"
                        to="/app/projects/new"
                    >
            <span>
              New project
            </span>

                        <strong>
                            Add a codebase
                        </strong>

                        <p>
                            Register a new project
                            inside CodeFortress.
                        </p>
                    </Link>
                </section>
            </div>
        </main>
    )
}