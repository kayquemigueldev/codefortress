import {
    NavLink,
    Outlet,
} from 'react-router'

import { useAuth } from '../../lib/auth/useAuth'

export function AppShell() {
    const {
        user,
        logout,
    } = useAuth()

    return (
        <div className="app-shell">
            <aside className="app-sidebar">
                <div>
                    <NavLink
                        className="app-brand"
                        to="/app/dashboard"
                    >
            <span className="app-brand__mark">
              CF
            </span>

                        <span>
              CodeFortress
            </span>
                    </NavLink>

                    <nav
                        className="app-navigation"
                        aria-label="Main navigation"
                    >
                        <p className="app-navigation__label">
                            Workspace
                        </p>

                        <NavLink
                            className={
                                navigationClassName
                            }
                            to="/app/dashboard"
                        >
              <span className="app-navigation__icon">
                D
              </span>

                            Dashboard
                        </NavLink>

                        <NavLink
                            className={
                                navigationClassName
                            }
                            to="/app/projects"
                        >
              <span className="app-navigation__icon">
                P
              </span>

                            Projects
                        </NavLink>
                    </nav>
                </div>

                <div className="app-sidebar__footer">
                    <div className="app-user">
                        <div className="app-user__avatar">
                            {getInitials(
                                user?.displayName,
                            )}
                        </div>

                        <div className="app-user__information">
                            <strong>
                                {user?.displayName}
                            </strong>

                            <span>
                {user?.email}
              </span>
                        </div>
                    </div>

                    <button
                        className="app-logout"
                        type="button"
                        onClick={() => {
                            void logout()
                        }}
                    >
                        Sign out
                    </button>
                </div>
            </aside>

            <div className="app-shell__main">
                <header className="app-topbar">
                    <div>
            <span className="app-topbar__status">
              Secure workspace
            </span>
                    </div>

                    <div className="app-topbar__account">
            <span className="app-topbar__name">
              {user?.displayName}
            </span>

                        <div className="app-user__avatar app-user__avatar--small">
                            {getInitials(
                                user?.displayName,
                            )}
                        </div>
                    </div>
                </header>

                <div className="app-shell__content">
                    <Outlet />
                </div>
            </div>
        </div>
    )
}

function navigationClassName({
                                 isActive,
                             }: {
    isActive: boolean
}) {
    return isActive
        ? 'app-navigation__link app-navigation__link--active'
        : 'app-navigation__link'
}

function getInitials(
    displayName:
        | string
        | undefined,
) {
    if (!displayName) {
        return 'CF'
    }

    return displayName
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map((part) =>
            part.charAt(0),
        )
        .join('')
        .toUpperCase()
}