import {
    getAccessToken,
    refreshAccessToken,
} from '../auth/auth-session'

import { ApiError } from './api-error'

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL
    ?? '/api/v1'

type ErrorPayload = {
    code?: unknown
    message?: unknown
    details?: unknown
    fieldErrors?: unknown
}

export async function apiRequest<T>(
    path: string,
    options: RequestInit = {},
): Promise<T> {
    const response =
        await sendRequest(
            path,
            options,
            null,
        )

    return parseResponse<T>(
        response,
    )
}

export async function authenticatedApiRequest<T>(
    path: string,
    options: RequestInit = {},
): Promise<T> {
    let response =
        await sendRequest(
            path,
            options,
            getAccessToken(),
        )

    if (response.status === 401) {
        const refreshedAccessToken =
            await refreshAccessToken()

        if (refreshedAccessToken) {
            response =
                await sendRequest(
                    path,
                    options,
                    refreshedAccessToken,
                )
        }
    }

    return parseResponse<T>(
        response,
    )
}

async function sendRequest(
    path: string,
    options: RequestInit,
    accessToken: string | null,
) {
    const requestHeaders =
        new Headers(options.headers)

    requestHeaders.set(
        'Accept',
        'application/json',
    )

    if (accessToken) {
        requestHeaders.set(
            'Authorization',
            `Bearer ${accessToken}`,
        )
    }

    return fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options,
            headers: requestHeaders,
            credentials: 'include',
        },
    )
}

async function parseResponse<T>(
    response: Response,
): Promise<T> {
    if (!response.ok) {
        throw await createApiError(
            response,
        )
    }

    if (response.status === 204) {
        return undefined as T
    }

    return response.json() as Promise<T>
}

async function createApiError(
    response: Response,
): Promise<ApiError> {
    const payload =
        await readErrorPayload(response)

    const message =
        typeof payload?.message === 'string'
            ? payload.message
            : `Request failed with status ${response.status}`

    const code =
        typeof payload?.code === 'string'
            ? payload.code
            : null

    const details =
        payload?.fieldErrors
        ?? payload?.details
        ?? null

    return new ApiError(
        response.status,
        message,
        code,
        details,
    )
}

async function readErrorPayload(
    response: Response,
): Promise<ErrorPayload | null> {
    try {
        return await response.json() as ErrorPayload
    } catch {
        return null
    }
}