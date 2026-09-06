type RefreshHandler =
    () => Promise<string | null>

let accessToken: string | null = null

let refreshHandler:
    RefreshHandler | null = null

export function getAccessToken() {
    return accessToken
}

export function setAccessToken(
    value: string | null,
) {
    accessToken = value
}

export function setRefreshHandler(
    handler: RefreshHandler | null,
) {
    refreshHandler = handler
}

export async function refreshAccessToken() {
    if (!refreshHandler) {
        return null
    }

    return refreshHandler()
}