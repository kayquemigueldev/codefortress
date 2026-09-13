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
                <div className="app-sidebar__main">
                    <NavLink
                        className="app-brand"
                        to="/app/dashboard"
                    >
                        <span className="app-brand__mark">
                            <ShieldIcon />
                        </span>

                        <span className="app-brand__content">
                            <strong>
                                CodeFortress
                            </strong>

                            <small>
                                Security workspace
                            </small>
                        </span>
                    </NavLink>

                    <nav
                        className="app-navigation"
                        aria-label="Main navigation"
                    >
                        <NavigationSection
                            label="Overview"
                        >
                            <NavigationLink
                                to="/app/dashboard"
                                label="Dashboard"
                                icon={<DashboardIcon />}
                            />

                            <NavigationLink
                                to="/app/projects"
                                label="Projects"
                                icon={<ProjectsIcon />}
                            />
                        </NavigationSection>

                        <NavigationSection
                            label="Security"
                        >
                            <NavigationLink
                                to="/app/security-rules"
                                label="Security Rules"
                                icon={<RulesIcon />}
                            />
                        </NavigationSection>
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
                        <LogoutIcon />

                        <span>
                            Sign out
                        </span>
                    </button>
                </div>
            </aside>

            <div className="app-shell__main">
                <header className="app-topbar">
                    <div className="app-topbar__workspace">
                        <span className="app-topbar__indicator" />

                        <div>
                            <strong>
                                Secure workspace
                            </strong>

                            <span>
                                Security Engine operational
                            </span>
                        </div>
                    </div>

                    <div className="app-topbar__account">
                        <div className="app-topbar__identity">
                            <strong>
                                {user?.displayName}
                            </strong>

                            <span>
                                {user?.email}
                            </span>
                        </div>

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

function NavigationSection({
                               label,
                               children,
                           }: {
    label: string
    children: React.ReactNode
}) {
    return (
        <div className="app-navigation__section">
            <p className="app-navigation__label">
                {label}
            </p>

            <div className="app-navigation__links">
                {children}
            </div>
        </div>
    )
}

function NavigationLink({
                            to,
                            label,
                            icon,
                        }: {
    to: string
    label: string
    icon: React.ReactNode
}) {
    return (
        <NavLink
            className={navigationClassName}
            to={to}
        >
            <span className="app-navigation__icon">
                {icon}
            </span>

            <span className="app-navigation__text">
                {label}
            </span>
        </NavLink>
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

function ShieldIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path
                d="M12 3 19 6v5c0 4.8-2.8 8.1-7 10-4.2-1.9-7-5.2-7-10V6l7-3Z"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.7"
            />

            <path
                d="m9.5 12 1.6 1.6 3.6-3.8"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="1.7"
            />
        </svg>
    )
}

function DashboardIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <rect
                x="4"
                y="4"
                width="6"
                height="6"
                rx="1"
            />

            <rect
                x="14"
                y="4"
                width="6"
                height="6"
                rx="1"
            />

            <rect
                x="4"
                y="14"
                width="6"
                height="6"
                rx="1"
            />

            <rect
                x="14"
                y="14"
                width="6"
                height="6"
                rx="1"
            />
        </svg>
    )
}

function ProjectsIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M4 7.5h6l1.7 2H20v9.5H4Z" />

            <path d="M4 7.5V5h6l1.7 2H20v2.5" />
        </svg>
    )
}

function RulesIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M7 5h13" />
            <path d="M7 12h13" />
            <path d="M7 19h13" />

            <path d="m3.5 5 1 1 2-2" />
            <path d="m3.5 12 1 1 2-2" />
            <path d="m3.5 19 1 1 2-2" />
        </svg>
    )
}

function LogoutIcon() {
    return (
        <svg
            aria-hidden="true"
            viewBox="0 0 24 24"
        >
            <path d="M10 5H5v14h5" />
            <path d="M13 8l4 4-4 4" />
            <path d="M17 12H9" />
        </svg>
    )
}