import { apiRequest } from '../../lib/api/api-client'

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
    return apiRequest<LoginResponse>(
        '/auth/refresh',
        {
            method: 'POST',
        },
    )
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

export function getCurrentUser(
    accessToken: string,
) {
    return apiRequest<AuthUser>(
        '/auth/me',
        {
            accessToken,
        },
    )
}