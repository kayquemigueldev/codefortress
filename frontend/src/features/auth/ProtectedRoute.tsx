import {
    Navigate,
    Outlet,
    useLocation,
} from 'react-router'

import { useAuth } from '../../lib/auth/useAuth'

export function ProtectedRoute() {
    const { status } = useAuth()
    const location = useLocation()

    if (status === 'initializing') {
        return (
            <main className="session-loading">
                <span className="session-loading__spinner" />
                <p>Restoring secure session...</p>
            </main>
        )
    }

    if (status === 'unauthenticated') {
        return (
            <Navigate
                to="/login"
                replace
                state={{
                    from: location.pathname,
                }}
            />
        )
    }

    return <Outlet />
}