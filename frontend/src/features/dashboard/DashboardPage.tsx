import { useAuth } from '../../lib/auth/useAuth'

export function DashboardPage() {
    const {
        user,
        logout,
    } = useAuth()

    return (
        <main className="dashboard-preview">
            <div>
                <p className="auth-eyebrow">
                    CodeFortress
                </p>

                <h1>
                    Welcome, {user?.displayName}.
                </h1>

                <p>
                    Your authenticated workspace
                    is ready.
                </p>

                <button
                    className="auth-submit"
                    type="button"
                    onClick={() => {
                        void logout()
                    }}
                >
                    Sign out
                </button>
            </div>
        </main>
    )
}