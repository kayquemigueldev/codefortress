export type ProjectStatus =
    | 'ACTIVE'
    | 'ARCHIVED'

export type Project = {
    id: string
    name: string
    description: string | null
    status: ProjectStatus
    createdAt: string
    updatedAt: string
}

export type CreateProjectRequest = {
    name: string
    description?: string | null
}

export type UpdateProjectRequest = {
    name: string
    description?: string | null
}
