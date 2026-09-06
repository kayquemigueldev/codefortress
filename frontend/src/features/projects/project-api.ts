import {
    authenticatedApiRequest,
} from '../../lib/api/api-client'

import type {
    CreateProjectRequest,
    Project,
    UpdateProjectRequest,
} from './project-types'

export function listProjects() {
    return authenticatedApiRequest<Project[]>(
        '/projects',
    )
}

export function getProject(
    projectId: string,
) {
    return authenticatedApiRequest<Project>(
        `/projects/${projectId}`,
    )
}

export function createProject(
    request: CreateProjectRequest,
) {
    return authenticatedApiRequest<Project>(
        '/projects',
        {
            method: 'POST',
            headers: {
                'Content-Type':
                    'application/json',
            },
            body: JSON.stringify(request),
        },
    )
}

export function updateProject(
    projectId: string,
    request: UpdateProjectRequest,
) {
    return authenticatedApiRequest<Project>(
        `/projects/${projectId}`,
        {
            method: 'PUT',
            headers: {
                'Content-Type':
                    'application/json',
            },
            body: JSON.stringify(request),
        },
    )
}

export function archiveProject(
    projectId: string,
) {
    return authenticatedApiRequest<void>(
        `/projects/${projectId}`,
        {
            method: 'DELETE',
        },
    )
}