import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from 'react'
import type { PropsWithChildren } from 'react'

import {
    login as loginRequest,
    logout as logoutRequest,
    refreshSession,
    register as registerRequest,
} from '../../features/auth/auth-api'
import type {
    AuthUser,
    LoginRequest,
    RegisterRequest,
} from '../../features/auth/auth-types'
import { ApiError } from '../api/api-error'

import {
    AuthContext,
    type AuthStatus,
} from './AuthContext'

export function AuthProvider({
                                 children,
                             }: PropsWithChildren) {
    const [user, setUser] =
        useState<AuthUser | null>(null)

    const [accessToken, setAccessToken] =
        useState<string | null>(null)

    const [status, setStatus] =
        useState<AuthStatus>('initializing')

    useEffect(() => {
        let active = true

        async function initializeSession() {
            try {
                const session =
                    await refreshSession()

                if (!active) {
                    return
                }

                setAccessToken(
                    session.accessToken,
                )

                setUser(
                    session.user,
                )

                setStatus(
                    'authenticated',
                )
            } catch (error) {
                if (!active) {
                    return
                }

                if (
                    error instanceof ApiError
                    && error.status === 401
                ) {
                    setAccessToken(null)
                    setUser(null)
                    setStatus(
                        'unauthenticated',
                    )

                    return
                }

                setAccessToken(null)
                setUser(null)
                setStatus(
                    'unauthenticated',
                )
            }
        }

        void initializeSession()

        return () => {
            active = false
        }
    }, [])

    const login =
        useCallback(
            async (
                request: LoginRequest,
            ) => {
                const session =
                    await loginRequest(request)

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

    const logout =
        useCallback(
            async () => {
                try {
                    await logoutRequest()
                } finally {
                    setAccessToken(null)
                    setUser(null)
                    setStatus(
                        'unauthenticated',
                    )
                }
            },
            [],
        )

    const register =
        useCallback(
            (
                request: RegisterRequest,
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
