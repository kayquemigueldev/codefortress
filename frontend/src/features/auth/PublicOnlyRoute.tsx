import {
    Navigate,
    Outlet,
} from 'react-router'

import { useAuth } from '../../lib/auth/useAuth'

export function PublicOnlyRoute() {
    const { status } = useAuth()

    if (status === 'initializing') {
        return (
            <main className="session-loading">
                <span className="session-loading__spinner" />
                <p>Restoring secure session...</p>
            </main>
        )
    }

    if (status === 'authenticated') {
        return (
            <Navigate
                to="/app/dashboard"
                replace
            />
        )
    }

    return <Outlet />
}
