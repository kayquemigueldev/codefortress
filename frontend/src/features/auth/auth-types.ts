export type AuthUser = {
    id: string
    displayName: string
    email: string
}

export type LoginRequest = {
    email: string
    password: string
}

export type LoginResponse = {
    accessToken: string
    tokenType: 'Bearer'
    expiresAt: string
    user: AuthUser
}

export type RegisterRequest = {
    displayName: string
    email: string
    password: string
}

export type RegisterResponse = {
    id: string
    displayName: string
    email: string
}