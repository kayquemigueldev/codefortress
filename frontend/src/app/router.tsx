import {
    Route,
    Routes,
} from 'react-router'

import { ProtectedRoute } from '../features/auth/ProtectedRoute'
import { PublicOnlyRoute } from '../features/auth/PublicOnlyRoute'
import { LoginPage } from '../features/auth/LoginPage'
import { DashboardPage } from '../features/dashboard/DashboardPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { ProjectsPage } from '../features/projects/ProjectsPage'
import { NewProjectPage } from '../features/projects/NewProjectPage'
import { ProjectDetailsPage } from '../features/projects/ProjectDetailsPage'

import '../styles/projects.css'
import '../styles/auth.css'

function HomePage() {
    return (
        <main className="foundation">
            <p className="foundation__eyebrow">
                CodeFortress
            </p>

            <h1>
                Security analysis starts
                here.
            </h1>

            <p>
                Frontend foundation is
                running.
            </p>
        </main>
    )
}



function NotFoundPage() {
    return (
        <main className="foundation">
            <p className="foundation__eyebrow">
                404
            </p>

            <h1>
                Page not found.
            </h1>
        </main>
    )
}

export function AppRouter() {
    return (
        <Routes>
            <Route
                path="/"
                element={<HomePage />}
            />

            <Route
                element={
                    <PublicOnlyRoute />
                }
            >
                <Route
                    path="/login"
                    element={<LoginPage />}
                />

                <Route
                    path="/register"
                    element={<RegisterPage />}
                />
            </Route>

            <Route
                element={
                    <ProtectedRoute />
                }
            >
                <Route
                    path="/app/projects"
                    element={<ProjectsPage />}
                />

                <Route
                    path="/app/projects/new"
                    element={<NewProjectPage />}
                />

                <Route
                    path="/app/projects/:projectId"
                    element={<ProjectDetailsPage />}
                />

                <Route
                    path="/app/dashboard"
                    element={
                        <DashboardPage />
                    }
                />
            </Route>

            <Route
                path="*"
                element={<NotFoundPage />}
            />
        </Routes>
    )
}