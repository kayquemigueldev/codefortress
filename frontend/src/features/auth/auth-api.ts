import {
    apiRequest,
    authenticatedApiRequest,
} from '../../lib/api/api-client'
let refreshPromise: Promise<LoginResponse> | null = null

import type {
    AuthUser,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    RegisterResponse,
} from './auth-types'

export function login(
    request: LoginRequest,
) {
    return apiRequest<LoginResponse>(
        '/auth/login',
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        },
    )
}

export function refreshSession() {
    if (!refreshPromise) {
        refreshPromise = apiRequest<LoginResponse>(
            '/auth/refresh',
            {
                method: 'POST',
            },
        ).finally(() => {
            refreshPromise = null
        })
    }

    return refreshPromise
}

export function logout() {
    return apiRequest<void>(
        '/auth/logout',
        {
            method: 'POST',
        },
    )
}

export function register(
    request: RegisterRequest,
) {
    return apiRequest<RegisterResponse>(
        '/auth/register',
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        },
    )
}

export function getCurrentUser() {
    return authenticatedApiRequest<AuthUser>(
        '/users/me',
    )
}