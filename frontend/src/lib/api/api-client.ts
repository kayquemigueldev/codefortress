import { ApiError } from './api-error'

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

type ApiRequestOptions = RequestInit & {
    accessToken?: string | null
}

type ErrorPayload = {
    code?: unknown
    message?: unknown
    details?: unknown
}

export async function apiRequest<T>(
    path: string,
    options: ApiRequestOptions = {},
): Promise<T> {
    const {
        accessToken,
        headers,
        ...requestOptions
    } = options

    const requestHeaders = new Headers(headers)

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

    const response = await fetch(
        `${API_BASE_URL}${path}`,
        {
            ...requestOptions,
            headers: requestHeaders,
            credentials: 'include',
        },
    )

    if (!response.ok) {
        throw await createApiError(response)
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

    return new ApiError(
        response.status,
        message,
        code,
        payload?.details ?? null,
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
