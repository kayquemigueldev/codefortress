import { Route, Routes } from 'react-router'

export function AppRouter() {
    return (
        <Routes>
            <Route
                path="/"
                element={
                    <main className="foundation">
            <span className="foundation__eyebrow">
              CodeFortress
            </span>

                        <h1>
                            Security analysis starts here.
                        </h1>

                        <p>
                            React foundation is ready.
                            Authentication comes next.
                        </p>
                    </main>
                }
            />

            <Route
                path="*"
                element={
                    <main className="foundation">
            <span className="foundation__eyebrow">
              404
            </span>

                        <h1>
                            Page not found.
                        </h1>
                    </main>
                }
            />
        </Routes>
    )
}