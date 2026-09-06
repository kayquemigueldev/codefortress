import {
    authenticatedApiRequest,
} from '../../lib/api/api-client'

import type {
    Analysis,
    UploadedAnalysis,
} from './analysis-types'

export function listAnalyses(
    projectId: string,
) {
    return authenticatedApiRequest<Analysis[]>(
        `/projects/${projectId}/analyses`,
    )
}

export function uploadAnalysis(
    projectId: string,
    file: File,
) {
    const formData = new FormData()

    formData.append(
        'file',
        file,
    )

    return authenticatedApiRequest<UploadedAnalysis>(
        `/projects/${projectId}/analyses`,
        {
            method: 'POST',
            body: formData,
        },
    )
}