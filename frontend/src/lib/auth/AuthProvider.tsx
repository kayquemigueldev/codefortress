import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from 'react'
import type {
    PropsWithChildren,
} from 'react'

import {
    login as loginRequest,
    logout as logoutRequest,
    refreshSession,
    register as registerRequest,
} from '../../features/auth/auth-api'
import type {
    AuthUser,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
} from '../../features/auth/auth-types'

import {
    AuthContext,
    type AuthStatus,
} from './AuthContext'
import {
    setAccessToken as setSessionAccessToken,
    setRefreshHandler,
} from './auth-session'

export function AuthProvider({
                                 children,
                             }: PropsWithChildren) {
    const [user, setUser] =
        useState<AuthUser | null>(null)

    const [
        accessToken,
        setAccessToken,
    ] =
        useState<string | null>(null)

    const [status, setStatus] =
        useState<AuthStatus>(
            'initializing',
        )

    const applySession =
        useCallback(
            (
                session: LoginResponse,
            ) => {
                setSessionAccessToken(
                    session.accessToken,
                )

                setAccessToken(
                    session.accessToken,
                )

                setUser(
                    session.user,
                )

                setStatus(
                    'authenticated',
                )
            },
            [],
        )

    const clearSession =
        useCallback(
            () => {
                setSessionAccessToken(null)

                setAccessToken(null)
                setUser(null)

                setStatus(
                    'unauthenticated',
                )
            },
            [],
        )

    useEffect(() => {
        let active = true

        async function renewSession() {
            try {
                const session =
                    await refreshSession()

                if (!active) {
                    return null
                }

                applySession(session)

                return session.accessToken
            } catch {
                if (active) {
                    clearSession()
                }

                return null
            }
        }

        setRefreshHandler(
            renewSession,
        )

        void renewSession()

        return () => {
            active = false

            setRefreshHandler(null)
        }
    }, [
        applySession,
        clearSession,
    ])

    const login =
        useCallback(
            async (
                request: LoginRequest,
            ) => {
                const session =
                    await loginRequest(request)

                applySession(session)
            },
            [applySession],
        )

    const logout =
        useCallback(
            async () => {
                try {
                    await logoutRequest()
                } finally {
                    clearSession()
                }
            },
            [clearSession],
        )

    const register =
        useCallback(
            (
                request:
                RegisterRequest,
            ) => {
                return registerRequest(
                    request,
                )
            },
            [],
        )

    const value =
        useMemo(
            () => ({
                user,
                accessToken,
                status,
                login,
                logout,
                register,
            }),
            [
                user,
                accessToken,
                status,
                login,
                logout,
                register,
            ],
        )

    return (
        <AuthContext.Provider
            value={value}
        >
            {children}
        </AuthContext.Provider>
    )
}