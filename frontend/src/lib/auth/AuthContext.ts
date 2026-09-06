import { createContext } from 'react'

import type {
    AuthUser,
    LoginRequest,
    RegisterRequest,
    RegisterResponse,
} from '../../features/auth/auth-types'

export type AuthStatus =
    | 'initializing'
    | 'authenticated'
    | 'unauthenticated'

export type AuthContextValue = {
    user: AuthUser | null
    accessToken: string | null
    status: AuthStatus
    login: (
        request: LoginRequest,
    ) => Promise<void>
    logout: () => Promise<void>
    register: (
        request: RegisterRequest,
    ) => Promise<RegisterResponse>
}

export const AuthContext =
    createContext<AuthContextValue | null>(
        null,
    )
