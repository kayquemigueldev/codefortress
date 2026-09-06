export class ApiError extends Error {
    readonly status: number
    readonly code: string | null
    readonly details: unknown

    constructor(
        status: number,
        message: string,
        code: string | null = null,
        details: unknown = null,
    ) {
        super(message)

        this.name = 'ApiError'
        this.status = status
        this.code = code
        this.details = details
    }
}